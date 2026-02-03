package app.tamingo.domain.gpt.prompt.todo;

import app.tamingo.domain.gpt.prompt.common.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class TodoInferencePrompt implements PromptTemplate {
    @Override
    public String render() {
        return """
                [Inference Rules]
                사용자의 입력(할 일 제목)을 분석하여 아래 규칙에 따라 장소, 카테고리, 소요시간을 추론해라.
               
                1. [카테고리 선택] (Strict):
                   - 반드시 [Todo Category List]에 존재하는 이름 중 하나만 선택해라.
                   - 목록에 적절한 것이 없다면 null을 반환해라. (새로운 카테고리를 창조하지 마라)

                2. [장소 추론] (우선순위 적용):
                   - A. 키워드/과거기록: [Reference Data]에 동일/유사한 제목의 기록이 있다면 그 장소를 반환해라.
                   - B. 생활 거점: 입력이 '맥도날드', '다이소' 등 프랜차이즈라면, [Reference Data]의 주소지들이 밀집된 곳(생활 거점) 근처의 지점으로 반환해라.
                   - C. 명확한 지명: 입력 자체가 '강남역' 처럼 장소라면 해당 위치를 반환해라.
                   - D. 장소 없음: 위 경우에 해당하지 않다면 반드시 null을 반환해라. 억지로 장소를 만들어내지 마라.

                3. [소요 시간(Duration) 강력 추론] (Mandatory):
                   - **'null' 반환을 최대한 피해라.** 행위를 수행하는 데 걸리는 시간을 **일반적인 상식**을 동원해서라도 추정해라.
                   - [Reference Data]가 없다면 아래 [Time Benchmark]를 참고하여 근사치를 할당해라.
                
                   [Time Benchmark]
                   * 간단한 처리 (반납, 인쇄, 송금, 구매, 픽업): 10 ~ 30 (분)
                   * 일상 활동 (식사, 이동, 짧은 미팅, 산책): 30 ~ 60 (분)
                   * 집중 활동 (공부, 업무, 운동, 영화, 진료): 60 ~ 120 (분)
                   * 긴 활동 (대청소, 여행, 프로젝트): 120 이상
                
                   - 예시: '회의' -> 60, '타밍고 회의' -> 90, '편의점' -> 10
                   - 정말로 물리적 시간이 소요되지 않는 추상적 개념이 아니라면, **과감하게 추정값을 넣어라.**

                [Output Format]
                반드시 아래 JSON 형식으로만 응답해라. (주석 금지)
                {
                  "category": "카테고리명" (없으면 null),
                  "placeName": "장소명" (없으면 null),
                  "address": "도로명 주소" (없으면 null),
                  "latitude": 37.1234 (없으면 null),
                  "longitude": 127.1234 (없으면 null),
                  "duration": 60 (분 단위 정수. 최대한 값을 채울 것)
                }
                """;
    }
}
