package app.tamingo.domain.todo.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.todo.dto.TodoCategoryResponse;
import app.tamingo.domain.todo.dto.TodoCategoryUpsertRequest;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.todo.exception.TodoErrorCode;
import app.tamingo.domain.todo.repository.TodoCategoryRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoCategoryService {

    private final TodoCategoryRepository todoCategoryRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    /**
     * 할일 카테고리 목록 조회
     */
    public List<TodoCategoryResponse> list(Long userId) {
        User user = userRepository.getReferenceById(userId);

        return todoCategoryRepository.findAllByUser(user).stream()
                .map(TodoCategoryResponse::from)
                .toList();
    }

    /**
     * 할일 카테고리 생성
     */
    @Transactional
    public TodoCategoryResponse create(Long userId, TodoCategoryUpsertRequest req) {
        User user = userRepository.getReferenceById(userId);

        //비즈니스 검증 : 같은 유저 내 이름 중복 금지
        if (todoCategoryRepository.existsByUserAndName(user, req.name())) {
            throw new CustomException(TodoErrorCode.TODO_CATEGORY_DUPLICATED);
        }

        TodoCategory category = TodoCategory.of(
                req.name(),
                req.colorCode(),
                user
        );
        //카테고리 저장
        TodoCategory saved = todoCategoryRepository.save(category);

        return TodoCategoryResponse.from(saved);
    }

    /**
     * 할일 카테고리 수정
     */
    @Transactional
    public TodoCategoryResponse update(Long userId, Long categoryId, TodoCategoryUpsertRequest req) {
        User user = userRepository.getReferenceById(userId);

        //카테고리 존재 여부 검사
        TodoCategory category = todoCategoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_CATEGORY_NOT_FOUND));

        //이름 변경 시에만 중복 검사
        if (!category.getName().equals(req.name())
                && todoCategoryRepository.existsByUserAndName(user, req.name())) {
            throw new CustomException(TodoErrorCode.TODO_CATEGORY_DUPLICATED);
        }
        //카테고리 업데이트
        category.update(req.name(), req.colorCode());
        return TodoCategoryResponse.from(category);
    }

    /**
     * 할일 카테고리 삭제 (사용 중이면 삭제 불가)
     */
    @Transactional
    public void delete(Long userId, Long categoryId) {
        User user = userRepository.getReferenceById(userId);

        //카테고리 존재 여부 검사
        TodoCategory category = todoCategoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_CATEGORY_NOT_FOUND));

        //할일 존재 여부 검사
        if (todoRepository.existsByUserAndTodoCategory(user, category)) {
            throw new CustomException(TodoErrorCode.TODO_CATEGORY_IN_USE);
        }
        //카테고리 삭제
        todoCategoryRepository.delete(category);
    }
}
