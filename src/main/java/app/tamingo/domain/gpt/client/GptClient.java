package app.tamingo.domain.gpt.client;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.gpt.GptErrorCode;
import app.tamingo.common.webclient.ApiClient;
import app.tamingo.domain.gpt.dto.GptRequest;
import app.tamingo.domain.gpt.dto.GptResponse;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class GptClient extends ApiClient {

    public GptClient(@Qualifier("gptWebClient")WebClient webClient){
        super(webClient);
    }

    public GptResponse requestCompletion(GptRequest request) {
        try {
            return post(
                    uri -> uri.path("/chat/completions").build(),
                    request,
                    GptResponse.class
            );
        } catch (WebClientResponseException.TooManyRequests e) {
            // 429 에러 : GPT Rate Limit
            throw new CustomException(GptErrorCode.GPT_TOO_MANY_REQUESTS);

        } catch (WebClientResponseException e) {
            // GPT 서버 응답 오류
            throw new CustomException(GptErrorCode.GPT_API_ERROR);

        } catch (ReadTimeoutException e) {
            // 네트워크/응답 지연
            throw new CustomException(GptErrorCode.GPT_TIMEOUT);

        } catch (Exception e) {
            // 그 외 오류 처리
            throw new CustomException(GptErrorCode.GPT_UNKNOWN_ERROR);
        }
    }

}
