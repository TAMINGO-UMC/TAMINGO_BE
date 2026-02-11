package app.tamingo.domain.calendar.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.calendar.dto.AppleCalendarSyncRequest;
import app.tamingo.domain.calendar.dto.AppleCalendarSyncResponse;
import app.tamingo.domain.calendar.entity.CalendarEvent;
import app.tamingo.domain.calendar.entity.CalendarIntegration;
import app.tamingo.domain.calendar.entity.ExternalTaskMapping;
import app.tamingo.domain.calendar.enums.LinkStatus;
import app.tamingo.domain.calendar.repository.CalendarEventRepository;
import app.tamingo.domain.calendar.repository.CalendarIntegrationRepository;
import app.tamingo.domain.calendar.repository.ExternalTaskMappingRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppleCalendarSyncService {

    private final CalendarIntegrationRepository calendarIntegrationRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final ExternalTaskMappingRepository externalTaskMappingRepository;

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public AppleCalendarSyncResponse syncFromApple(Long userId, AppleCalendarSyncRequest request) {

        // 설명: 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 설명: 유저의 애플 연동 조회(없으면 생성)
        CalendarIntegration integration = calendarIntegrationRepository.findByUserId(userId)
                .orElseGet(() -> calendarIntegrationRepository.save(CalendarIntegration.of(user)));


        // 설명: 동기화 OFF면 아무것도 하지 않음(iOS는 원래 ACTIVE일 때만 호출하는 게 목표)
        if (!integration.isSyncFromApple()) {
            return new AppleCalendarSyncResponse(0, 0, 0, 0, LocalDateTime.now());
        }

        // 설명: 이벤트가 없으면 동기화 성공 시간만 기록하고 종료
        if (request.events().isEmpty()) {
            integration.markSyncedNow();
            return new AppleCalendarSyncResponse(0, 0, 0, 0, LocalDateTime.now());
        }

        // 설명: 동기화 진행 상태 표시
        integration.markSyncing();

        // 설명: 응답용 카운터
        int createdSchedules = 0;
        int updatedSchedules = 0;
        int skippedSchedules = 0;
        int upsertedEvents = 0;

        for (AppleCalendarSyncRequest.AppleCalendarEventItem item : request.events()) {

            // 설명: iOS에서 ISO 문자열로 넘어온 시간 파싱(+09:00 포함)
            LocalDateTime startAt = parseToLocalDateTime(item.startAt());
            LocalDateTime endAt = parseToLocalDateTime(item.endAt());

            // 1) CalendarEvent upsert (원본 스냅샷 저장)
            CalendarEvent calendarEvent = calendarEventRepository
                    .findByIntegrationAndUid(integration.getId(), item.externalEventUid())
                    .orElseGet(() -> CalendarEvent.of(
                            integration,
                            user,
                            item.externalEventUid(),
                            null,
                            null,
                            safeTitle(item.title()),
                            startAt,
                            endAt,
                            item.isAllDay(),
                            null,
                            item.location(),
                            null,
                            null
                    ));

            calendarEvent.updateFromApple(
                    null,
                    null,
                    safeTitle(item.title()),
                    startAt,
                    endAt,
                    item.isAllDay(),
                    null,
                    item.location(),
                    null,
                    null
            );

            calendarEventRepository.save(calendarEvent);
            upsertedEvents++;

            // 2) ExternalTaskMapping 조회 (이 Apple 이벤트가 어떤 schedule과 연결됐는지)
            Optional<ExternalTaskMapping> mappingOpt =
                    externalTaskMappingRepository.findByIntegrationAndUid(integration.getId(), item.externalEventUid());

            // 3) 매핑이 없으면 schedule 생성 + mapping 생성
            if (mappingOpt.isEmpty()) {

                Schedule schedule = Schedule.of(
                        user,
                        null,                      // 설명: 카테고리 없으면 null
                        safeTitle(item.title()),
                        startAt,
                        endAt,
                        item.location(),           // 설명: placeName
                        null,                      // address
                        null,                      // latitude
                        null,                      // longitude
                        RepeatType.NONE,
                        null,
                        null                       // 설명: memo (최소 DTO에서는 notes 없음)
                );

                scheduleRepository.save(schedule);

                ExternalTaskMapping mapping = ExternalTaskMapping.linked(integration, schedule, calendarEvent);
                externalTaskMappingRepository.save(mapping);

                createdSchedules++;
                continue;
            }

            ExternalTaskMapping mapping = mappingOpt.get();

            // 4) UNLINKED면 schedule 덮어쓰기 스킵(앱에서 수정한 일정 보호)
            if (mapping.getLinkStatus() == LinkStatus.UNLINKED) {
                mapping.markSyncedNow();
                skippedSchedules++;
                continue;
            }

            //여기부터는 LINKED만
            Long scheduleId = mapping.getSchedule().getId();

            //삭제 포함 조회로 schedule 상태 확인
            Schedule schedule = scheduleRepository.findByIdIncludingDeleted(scheduleId).orElse(null);

            if (schedule == null) {
                // (보험) schedule row 자체가 없는 경우(과거 하드딜리트/데이터 꼬임)
                mapping.unlink();      // 이후 덮어쓰기 방지
                mapping.markSyncedNow();
                skippedSchedules++;
                continue;
            }

            if (schedule.getDeletedAt() != null) {
                //앱에서 이미 삭제(soft delete)된 일정이면 다시 살리지 않기
                if (mapping.getLinkStatus() == LinkStatus.LINKED) {
                    mapping.unlink(); // LINKED였다면 UNLINKED로 전환(보험)
                }
                mapping.markSyncedNow();
                skippedSchedules++;
                continue;
            }

            schedule.update(
                    schedule.getScheduleCategory(), // 설명: 앱 카테고리 유지
                    safeTitle(item.title()),
                    startAt,
                    endAt,
                    item.location(),                // 설명: placeName 덮어쓰기
                    schedule.getAddress(),          // 설명: 앱 값 유지
                    schedule.getLatitude(),
                    schedule.getLongitude(),
                    schedule.getRepeatType(),       // 설명: 반복 앱 값 유지
                    schedule.getRepeatEndDate(),
                    schedule.getMemo()              // 설명: memo 유지(최소 DTO에서 notes가 없으니 덮어쓰지 않음)
            );

            mapping.markSyncedNow();
            updatedSchedules++;
        }

        // 설명: 동기화 성공 처리(ACTIVE + lastSyncedAt 갱신)
        integration.markSyncedNow();

        return new AppleCalendarSyncResponse(
                createdSchedules,
                updatedSchedules,
                skippedSchedules,
                upsertedEvents,
                LocalDateTime.now()
        );
    }

    // 설명: title null/blank 및 길이 20 제한 보정(Schedule.title 제약)
    private String safeTitle(String title) {
        if (title == null || title.isBlank()) return "일정";
        return title.length() > 20 ? title.substring(0, 20) : title;
    }

    // 설명: 오프셋 포함 ISO 문자열(권장)을 LocalDateTime으로 파싱
    private LocalDateTime parseToLocalDateTime(String iso) {
        try {
            return OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (Exception ignore) {
            return LocalDateTime.parse(iso);
        }
    }
}
