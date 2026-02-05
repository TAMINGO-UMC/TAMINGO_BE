package app.tamingo.domain.gpt.service.todo;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.gpt.GptErrorCode;
import app.tamingo.domain.gpt.dto.GptRequest;
import app.tamingo.domain.gpt.prompt.common.DataPrompt;
import app.tamingo.domain.gpt.prompt.common.GeneralSystemPrompt;
import app.tamingo.domain.gpt.prompt.common.PromptTemplate;
import app.tamingo.domain.gpt.prompt.todo.TodoInferencePrompt;
import app.tamingo.domain.gpt.service.common.GptService;
import app.tamingo.domain.kakao.dto.KakaoPlaceDto;
import app.tamingo.domain.kakao.service.KakaoSearchService;
import app.tamingo.domain.todo.dto.AiTodoInferenceResponse;
import app.tamingo.domain.todo.dto.AiTodoInfo;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.todo.repository.TodoCategoryRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiTodoService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final TodoCategoryRepository todoCategoryRepository;
    private final GptService gptService;
    private final TodoInferencePrompt todoInferencePrompt;
    private final KakaoSearchService kakaoSearchService;

    public AiTodoInferenceResponse inferTodo(Long userId, String inputTitle) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 데이터 준비 (카테고리 + 과거 할 일 기록)
        String contextDataString = prepareContextData(user, inputTitle);
        GeneralSystemPrompt generalSystemPrompt = new GeneralSystemPrompt();
        DataPrompt dataPrompt = new DataPrompt("Context Data", contextDataString);

        // 프롬프트 조합
        PromptTemplate finalSystemPrompt = generalSystemPrompt.compose(todoInferencePrompt, dataPrompt);

        // 사용자 입력
        String userMessage = String.format("입력 할 일 제목: \"%s\", 현재 시간: \"%s\"",
                inputTitle, LocalDateTime.now());

        String fullPromptContent = finalSystemPrompt.render() + "\n\n[User Input]\n" + userMessage;
        GptRequest request = new GptRequest(fullPromptContent, 1000);

        String rawResponse = gptService.callGpt(request);

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new CustomException(GptErrorCode.GPT_UNKNOWN_ERROR);
        }

        // JSON 파싱
        String cleanJson = rawResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();

        log.info("Cleaned GPT JSON Response (Todo): {}", cleanJson);

        AiTodoInfo aiTodoInfo = gptService.parseJson(cleanJson, AiTodoInfo.class);

        // 주소, 좌표 보정
        if (aiTodoInfo.placeName() != null && !aiTodoInfo.placeName().isBlank()) {
            KakaoPlaceDto correctPlace = kakaoSearchService.search(aiTodoInfo.placeName());

            if (correctPlace != null) {
                log.info("Place Correction: GPT({}) -> Kakao({})", aiTodoInfo.placeName(), correctPlace.placeName());

                aiTodoInfo = new AiTodoInfo(
                        aiTodoInfo.category(),
                        correctPlace.placeName(),
                        correctPlace.address(),
                        correctPlace.latitude(),
                        correctPlace.longitude(),
                        aiTodoInfo.duration()
                );
            }
        }

        return new AiTodoInferenceResponse(aiTodoInfo);
    }

    // 카테고리 목록 & 과거 기록을 하나의 문자열로 포맷팅
    private String prepareContextData(User user, String inputTitle) {

        //카테고리 목록
        String categoryList = todoCategoryRepository.findAllByUser(user).stream()
                .map(TodoCategory::getName)
                .collect(Collectors.joining(", "));

        // 키워드 매칭
        List<Todo> keywordTodos = todoRepository.findTop20ByUserAndTitleContainingOrderByIdDesc(user, inputTitle);

        // 최근 전체 기록
        List<Todo> recentTodos = todoRepository.findTop20ByUserOrderByIdDesc(user);

        // 합치기
        Set<Todo> contextTodos = new LinkedHashSet<>();
        contextTodos.addAll(keywordTodos);
        contextTodos.addAll(recentTodos);

        String historyData;
        if (contextTodos.isEmpty()) {
            historyData = "관련된 과거 기록 없음.";
        } else {
            historyData = contextTodos.stream()
                    .map(t -> String.format("- \"%s\" -> 장소: \"%s\", 소요시간: %s분, 카테고리: %s",
                            t.getTitle(),
                            t.getPlaceName() != null ? t.getPlaceName() : "없음",
                            t.getDuration() != null ? t.getDuration() : "미정",
                            t.getTodoCategory() != null ? t.getTodoCategory().getName() : "없음"))
                    .collect(Collectors.joining("\n"));
        }

        return String.format("""
                [Todo Category List]
                %s
                
                [Reference Data (Past Todo History)]
                %s
                """, categoryList, historyData);
    }
}
