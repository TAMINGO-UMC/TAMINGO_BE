package app.tamingo.domain.schedule.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.schedule.dto.ScheduleCategoryResponse;
import app.tamingo.domain.schedule.dto.ScheduleCategoryUpsertRequest;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleCategoryService {

    private final ScheduleCategoryRepository scheduleCategoryRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * 스케줄 카테고리 목록 조회
     */
    public List<ScheduleCategoryResponse> list(Long userId) {
        User user = userRepository.getReferenceById(userId);

        return scheduleCategoryRepository.findAllByUser(user).stream()
                .map(ScheduleCategoryResponse::from)
                .toList();
    }

    /**
     * 스케줄 카테고리 생성
     */
    @Transactional
    public ScheduleCategoryResponse create(Long userId, ScheduleCategoryUpsertRequest req) {
        User user = userRepository.getReferenceById(userId);

        // 비즈니스 검증: 같은 유저 내 이름 중복 금지
        if (scheduleCategoryRepository.existsByUserAndName(user, req.name())) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_DUPLICATED);
        }
        ScheduleCategory category = ScheduleCategory.of(
                req.name(),
                req.iconCode(),
                req.colorCode(),
                user
        );

        ScheduleCategory saved = scheduleCategoryRepository.save(category);
        return ScheduleCategoryResponse.from(saved);
    }

    /**
     * 스케줄 카테고리 수정
     */
    @Transactional
    public ScheduleCategoryResponse update(Long userId, Long categoryId, ScheduleCategoryUpsertRequest req) {
        User user = userRepository.getReferenceById(userId);

        ScheduleCategory category = scheduleCategoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_NOT_FOUND));

        // 이름 변경 시에만 중복 검사
        if (!category.getName().equals(req.name())
                && scheduleCategoryRepository.existsByUserAndName(user, req.name())) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_DUPLICATED);
        }

        // 카테고리 업데이트
        category.update(req.name(), req.iconCode(), req.colorCode());

        return ScheduleCategoryResponse.from(category);
    }

    /**
     * 스케줄 카테고리 삭제
     */
    @Transactional
    public void delete(Long userId, Long categoryId) {
        User user = userRepository.getReferenceById(userId);

        ScheduleCategory category = scheduleCategoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_NOT_FOUND));

        if (scheduleRepository.existsByUserAndScheduleCategory(user, category)) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_IN_USE);
        }

        scheduleCategoryRepository.delete(category);
    }

}
