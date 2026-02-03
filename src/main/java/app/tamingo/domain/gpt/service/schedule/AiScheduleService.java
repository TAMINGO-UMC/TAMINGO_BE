package app.tamingo.domain.gpt.service.schedule;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.gpt.GptErrorCode;
import app.tamingo.domain.gpt.dto.GptRequest;
import app.tamingo.domain.gpt.prompt.common.DataPrompt;
import app.tamingo.domain.gpt.prompt.common.GeneralSystemPrompt;
import app.tamingo.domain.gpt.prompt.common.PromptTemplate;
import app.tamingo.domain.gpt.prompt.schedule.ScheduleInferencePrompt;
import app.tamingo.domain.gpt.service.common.GptService;
import app.tamingo.domain.kakao.dto.KakaoPlaceDto;
import app.tamingo.domain.kakao.service.KakaoSearchService;
import app.tamingo.domain.schedule.dto.AiInferenceResponse;
import app.tamingo.domain.schedule.dto.AiPlaceInfo;
import app.tamingo.domain.schedule.dto.RecommendTodoResponse;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.schedule.service.PlaceContextService;
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
public class AiScheduleService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;
    private final PlaceContextService placeContextService;
    private final GptService gptService;
    private final ScheduleInferencePrompt scheduleInferencePrompt;
    private final KakaoSearchService kakaoSearchService;

    public AiInferenceResponse inferSchedule(Long userId, String inputTitle){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 프롬프트에 넣을 데이터 (카테고리 + 과거 기록)
        String contextDataString = prepareContextData(user, inputTitle);
        GeneralSystemPrompt generalSystemPrompt = new GeneralSystemPrompt();
        DataPrompt dataPrompt = new DataPrompt("Context Data", contextDataString);

        // 최종 프롬프트(Rules + Data)
        PromptTemplate finalSystemPrompt = generalSystemPrompt.compose(scheduleInferencePrompt, dataPrompt);

        // 사용자 입력
        String userMessage = String.format("입력 제목: \"%s\", 현재 시간: \"%s\"",
                inputTitle, LocalDateTime.now());

        // 합성된 프롬프트 요청
        String fullPromptContent = finalSystemPrompt.render() + "\n\n[User Input]\n" + userMessage;
        GptRequest request = new GptRequest(fullPromptContent, 1000);

        String rawResponse = gptService.callGpt(request);

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new CustomException(GptErrorCode.GPT_UNKNOWN_ERROR);
        }

        String cleanJson = rawResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();

        log.info("Cleaned GPT JSON Response: {}", cleanJson);

        AiPlaceInfo aiPlaceInfo =
                gptService.parseJson(cleanJson, AiPlaceInfo.class);

        // 주소, 좌표 보정
        if (aiPlaceInfo.placeName() != null && !aiPlaceInfo.placeName().isBlank()) {

            KakaoPlaceDto correctPlace = kakaoSearchService.search(aiPlaceInfo.placeName());
            if (correctPlace != null) {
                log.info("Place Correction: GPT({}) -> Kakao({})", aiPlaceInfo.placeName(), correctPlace.placeName());

                aiPlaceInfo = new AiPlaceInfo(
                        correctPlace.placeName(), // 카카오 공식 명칭
                        correctPlace.address(),   // 카카오 도로명 주소
                        correctPlace.latitude(),  // 정확한 위도
                        correctPlace.longitude(), // 정확한 경도
                        aiPlaceInfo.category()    // 카테고리 유지
                );
            }
        }

        // 장소 추론 후 주변 정보(PlaceContext) 조회, 결합
        RecommendTodoResponse contextResponse = null;
        if(aiPlaceInfo.placeName() !=null && !aiPlaceInfo.placeName().isBlank()){
            contextResponse = placeContextService.getPlaceContext(
                    userId,
                    aiPlaceInfo.placeName(),
                    aiPlaceInfo.latitude(),
                    aiPlaceInfo.longitude()
            );
        }
        return new AiInferenceResponse(aiPlaceInfo, contextResponse);
    }

    // 카테고리 목록 & 과거 기록을 하나의 문자열로 포맷팅
    private String prepareContextData(User user, String inputTitle){
        // 카테고리 목록
        String categoryList = scheduleCategoryRepository.findAllByUser(user).stream()
                .map(ScheduleCategory::getName)
                .collect(Collectors.joining(", "));

        // 키워드 매칭
        List<Schedule> keywordSchedules = scheduleRepository
                .findTop20ByUserAndTitleContainingOrderByStartTimeDesc(user, inputTitle);

        // 최근 전체 기록
        List<Schedule> recentSchedules = scheduleRepository
                .findTop20ByUserOrderByStartTimeDesc(user);

        // 합치기
        Set<Schedule> contextSchedules = new LinkedHashSet<>();
        contextSchedules.addAll(keywordSchedules); // 키워드 매칭 우선
        contextSchedules.addAll(recentSchedules);

        String historyData;
        if (contextSchedules.isEmpty()) {
            historyData = "관련된 과거 기록 없음.";
        } else {
            historyData = contextSchedules.stream()
                    .map(s -> String.format("- \"%s\" -> 장소: \"%s\", 주소: \"%s\", 위도: %f, 경도: %f, 카테고리: %s",
                            s.getTitle(),
                            s.getPlaceName(),
                            s.getAddress() != null ? s.getAddress() : "정보없음",
                            s.getLatitude(),
                            s.getLongitude(),
                            s.getScheduleCategory() != null ? s.getScheduleCategory().getName() : "없음"))
                    .collect(Collectors.joining("\n"));
        }
        // DataPrompt에 들어갈 최종 문자열
        return String.format("""
            [Category List]
            %s
            
            [Reference Data (Past History)]
            %s
            """, categoryList, historyData);

    }
}