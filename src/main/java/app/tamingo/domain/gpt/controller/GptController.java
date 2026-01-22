package app.tamingo.domain.gpt.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.gpt.dto.ExampleGptResponse;
import app.tamingo.domain.gpt.prompt.common.DataPrompt;
import app.tamingo.domain.gpt.prompt.common.ExamplePrompt;
import app.tamingo.domain.gpt.service.common.GptService;
import app.tamingo.domain.terms.dto.TermsSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GptController {

    private final GptService gptService;


    @PostMapping("/api/gpt")
    public ApiResponse<ExampleGptResponse> gptExample() {
        ExamplePrompt ex = new ExamplePrompt();
        DataPrompt data = new DataPrompt("title","data");
        return ApiResponse.onSuccess(
                gptService.getAssistantMsg(ex,data,300),
                SuccessCode.OK);
    }

}
