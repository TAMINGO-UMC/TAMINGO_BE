package app.tamingo.domain.gpt.service.monthlyreport;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.gpt.GptErrorCode;
import app.tamingo.domain.gpt.dto.GptRequest;
import app.tamingo.domain.gpt.prompt.common.GeneralSystemPrompt;
import app.tamingo.domain.gpt.prompt.common.PromptTemplate;
import app.tamingo.domain.gpt.service.common.GptService;
import app.tamingo.domain.monthlyreport.dto.MonthlyInsightGptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonthlyInsightGptService {

    private final GeneralSystemPrompt generalSystemPrompt = new GeneralSystemPrompt();
    private final GptService gptService;

    public MonthlyInsightGptResponse getGptResponse(
            PromptTemplate domainPrompt,
            PromptTemplate dataPrompt,
            Integer maxTokens
    ) {
        PromptTemplate composed = generalSystemPrompt.compose(domainPrompt, dataPrompt);
        String systemMessage = composed.render();

        GptRequest request = new GptRequest(systemMessage, maxTokens != null ? maxTokens : 700);

        String raw = gptService.callGpt(request);
        if (raw == null) throw new CustomException(GptErrorCode.GPT_RESPONSE_FORMAT_ERROR);

        MonthlyInsightGptResponse parsed = gptService.parseJson(raw, MonthlyInsightGptResponse.class);

        if (parsed.insights() == null || parsed.insights().size() != 3) {
            throw new CustomException(GptErrorCode.GPT_RESPONSE_FORMAT_ERROR);
        }
        if (parsed.modelVersion() == null || parsed.modelVersion().isBlank()) {
            throw new CustomException(GptErrorCode.GPT_RESPONSE_FORMAT_ERROR);
        }

        return parsed;
    }
}
