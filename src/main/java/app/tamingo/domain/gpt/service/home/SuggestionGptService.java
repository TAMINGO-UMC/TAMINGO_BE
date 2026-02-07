package app.tamingo.domain.gpt.service.home;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.gpt.exception.GptErrorCode;
import app.tamingo.domain.gpt.dto.GptRequest;
import app.tamingo.domain.gpt.prompt.common.GeneralSystemPrompt;
import app.tamingo.domain.gpt.prompt.common.PromptTemplate;
import app.tamingo.domain.gpt.service.common.GptService;
import app.tamingo.domain.home.dto.TodoScheduleLinkGptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuggestionGptService {

    private final GeneralSystemPrompt generalSystemPrompt = new GeneralSystemPrompt();
    private final GptService gptService;

    /**
     * 틈새시간 추천 GPT 호출
     * @param domainPrompt 도메인 프롬프트
     * @param dataPrompt 학습할 데이터
     * @param maxTokens 최대 토큰 수
     * @return GPT 응답 객체
     */
    public TodoScheduleLinkGptResponse getGptResponse(
            PromptTemplate domainPrompt,
            PromptTemplate dataPrompt,
            Integer maxTokens
    ) {
        // 프롬프트 조합
        PromptTemplate composedPrompt =
                generalSystemPrompt.compose(domainPrompt, dataPrompt);
        String systemMessage = composedPrompt.render();

        // GptRequest 생성
        GptRequest request = new GptRequest(
                systemMessage,
                maxTokens != null ? maxTokens : 500
        );

        // GPT 호출
        String rawContent = gptService.callGpt(request);

        if (rawContent == null) {
            throw new CustomException(GptErrorCode.GPT_RESPONSE_FORMAT_ERROR);
        }

        // 파싱/검증
        return gptService.parseJson(rawContent, TodoScheduleLinkGptResponse.class);
    }

}
