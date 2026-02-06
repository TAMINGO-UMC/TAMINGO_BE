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

        //유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        //유저의 애플 연동 조회(없으면 생성)
        CalendarIntegration integration = calendarIntegrationRepository.findByUserId(userId)
                .orElseGet(() -> calendarIntegrationRepository.save(CalendarIntegration.of(user)));

        //동기화 OFF면 그냥 결과만 리턴(정책에 따라 예외로 막아도 됨)
        if (!integration.isSyncFromApple()) {
            return new AppleCalendarSyncResponse(0, 0, 0, 0, LocalDateTime.now());
        }

        integration.markSyncing();

        //응답용 카운터
        int createdSchedules = 0;
        int updatedSchedules = 0;
        int skippedSchedules = 0;
        int upsertedEvents = 0;

        for (AppleCalendarSyncRequest.AppleCalendarEventItem item : request.events()) {

            //삭제 이벤트 여부
            boolean deleted = Boolean.TRUE.equals(item.deleted());

            //iOS에서 ISO 문자열로 넘어온 시간 파싱
            LocalDateTime startAt = parseToLocalDateTime(item.startAt());
            LocalDateTime endAt = parseToLocalDateTime(item.endAt());
            LocalDateTime lastModified = (item.lastExternalModifiedAt() == null)
                    ? null
                    : parseToLocalDateTime(item.lastExternalModifiedAt());

            //CalendarEvent upsert
            CalendarEvent calendarEvent = calendarEventRepository
                    .findByIntegrationAndUid(integration.getId(), item.externalEventUid())
                    .orElseGet(() -> CalendarEvent.of(
                            integration,
                            user,
                            item.externalEventUid(),
                            item.calendarExternalId(),
                            item.calendarName(),
                            item.title(),
                            startAt,
                            endAt,
                            item.isAllDay(),
                            item.timezone(),
                            item.location(),
                            item.notes(),
                            lastModified
                    ));

            //기존이면 update, 삭제면 삭제 처리
            if (calendarEvent.getId() != null) {
                if (deleted) {
                    calendarEvent.markDeletedNow();
                } else {
                    calendarEvent.updateFromApple(
                            item.calendarExternalId(),
                            item.calendarName(),
                            item.title(),
                            startAt,
                            endAt,
                            item.isAllDay(),
                            item.timezone(),
                            item.location(),
                            item.notes(),
                            lastModified
                    );
                }
            } else {
                //신규인데 삭제로 오는 케이스 방어
                if (deleted) {
                    calendarEvent.markDeletedNow();
                }
            }

            calendarEventRepository.save(calendarEvent);
            upsertedEvents++;

            //ExternalTaskMapping 조회
            Optional<ExternalTaskMapping> mappingOpt =
                    externalTaskMappingRepository.findByIntegrationAndUid(integration.getId(), item.externalEventUid());

            //매핑 없음 -> (삭제면 스킵) 아니면 schedule 생성 + mapping 생성
            if (mappingOpt.isEmpty()) {
                if (deleted) {
                    skippedSchedules++;
                    continue;
                }

                Schedule schedule = Schedule.of(
                        user,
                        null, //카테고리 없으면 null or 기본값
                        safeTitle(item.title()),
                        startAt,
                        endAt,
                        item.location(), //placeName
                        null,            //address
                        null,            //latitude
                        null,            //longitude
                        RepeatType.NONE,
                        null,
                        item.notes()     //memo
                );

                scheduleRepository.save(schedule);

                ExternalTaskMapping mapping = ExternalTaskMapping.linked(integration, schedule, calendarEvent);
                externalTaskMappingRepository.save(mapping);

                createdSchedules++;
                continue;
            }

            ExternalTaskMapping mapping = mappingOpt.get();

            //UNLINKED면 schedule 업데이트 스킵(덮어쓰기 방지)
            if (mapping.getLinkStatus() == LinkStatus.UNLINKED) {
                mapping.markSyncedNow();
                skippedSchedules++;
                continue;
            }

            //LINKED면 schedule 덮어쓰기 업데이트
            if (deleted) {
                //삭제 정책은 선택(지금은 schedule 건드리지 않고 스킵)
                mapping.markSyncedNow();
                skippedSchedules++;
                continue;
            }

            Schedule schedule = mapping.getSchedule();

            //Schedule.update는 전체 파라미터가 필요하므로 기존 값 유지하면서 Apple 관련만 교체
            schedule.update(
                    schedule.getScheduleCategory(), //앱 카테고리 유지
                    safeTitle(item.title()),
                    startAt,
                    endAt,
                    item.location(),                 //placeName 덮어쓰기
                    schedule.getAddress(),            //앱 값 유지
                    schedule.getLatitude(),
                    schedule.getLongitude(),
                    schedule.getRepeatType(),         //반복 앱 값 유지
                    schedule.getRepeatEndDate(),
                    item.notes()                      //memo 덮어쓰기
            );

            mapping.markSyncedNow();
            updatedSchedules++;
        }

        integration.markSyncedNow();

        return new AppleCalendarSyncResponse(
                createdSchedules,
                updatedSchedules,
                skippedSchedules,
                upsertedEvents,
                LocalDateTime.now()
        );
    }

    //title null/blank 및 길이 20 제한 보정
    private String safeTitle(String title) {
        if (title == null || title.isBlank()) return "일정";
        return title.length() > 20 ? title.substring(0, 20) : title;
    }

    //오프셋 포함 ISO 문자열(권장)을 LocalDateTime으로 파싱
    private LocalDateTime parseToLocalDateTime(String iso) {
        try {
            return OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (Exception ignore) {
            return LocalDateTime.parse(iso);
        }
    }
}
