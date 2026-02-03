package app.tamingo.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

        @Bean
        WebClient gptWebClient(
                        @Value("${chatgpt.url}") String baseUrl,
                        @Value("${chatgpt.api-key}") String apiKey) {
                return WebClient.builder()
                                .baseUrl(baseUrl)
                                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .build();
        }

        @Bean(name = "kakaoWebClient")
        WebClient kakaoWebClient(
                        @Value("${kakao.api.key}") String apiKey,
                        @Value("${kakao.api.url}") String baseUrl) {
                return WebClient.builder()
                                .baseUrl(baseUrl) // [변경] 직접 입력 -> 변수 사용
                                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey)
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .build();
        }

        @Bean(name = "tmapWebClient")
        WebClient tmapWebClient(
                        @Value("${tmap.url}") String baseUrl,
                        @Value("${tmap.app-key}") String appKey) {
                return WebClient.builder()
                                .baseUrl(baseUrl)
                                .defaultHeader("appKey", appKey)
                                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .build();
        }

        @Bean(name = "kakaoMapGeoWebClient")
        WebClient kakaoMapGeoClient(
                        @Value("${kakao.api.key}") String apiKey,
                        @Value("${kakao.api.url}") String baseUrl) {
                return WebClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
        }
}
