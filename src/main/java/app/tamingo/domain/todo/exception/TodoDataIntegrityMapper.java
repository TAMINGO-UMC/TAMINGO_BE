package app.tamingo.domain.todo.exception;

import app.tamingo.common.exception.DataIntegrityMapper;
import app.tamingo.common.response.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class TodoDataIntegrityMapper implements DataIntegrityMapper {
    @Override
    public boolean supports(String key) {
        return "uk_todo_category_user_name".equals(key);
    }


    @Override
    public BaseCode errorCode() {
        return TodoErrorCode.TODO_CATEGORY_DUPLICATED;
    }
}
