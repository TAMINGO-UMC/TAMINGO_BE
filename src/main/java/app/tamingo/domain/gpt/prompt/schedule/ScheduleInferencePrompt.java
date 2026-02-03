package app.tamingo.domain.gpt.prompt.schedule;

import app.tamingo.domain.gpt.prompt.common.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScheduleInferencePrompt implements PromptTemplate {

    @Override
    public String render(){
        return """
                [Inference Rules]
                **아래 우선순위(1 -> 2 -> 3)를 엄격하게 적용하여 장소를 추론해라.**

                1. [키워드 매칭] (최우선):
                   - [Reference Data]에 사용자가 입력한 제목의 단어(예: "타밍고")가 포함된 기록이 있다면, 그 기록의 장소를 반환해라.
                   - 예시: 과거에 "타밍고 회의"를 "광운대학교"에서 했다면, 입력이 "타밍고"일 때 "광운대학교"를 반환.

                2. [생활 거점 기반 추론] (차상위 - 매우 중요):
                   - 키워드가 일치하는 과거 기록이 없더라도, [Reference Data] 전체를 분석하여 **사용자의 주 활동 지역(생활 거점)**을 파악해라. (예: 데이터의 다수가 '중랑구청' 근처임)
                   
                   - **Case A (일반 행위):** 입력값이 '회의', '작업', '미팅' 등 장소를 특정하지 않는 행위라면, **파악된 생활 거점 장소**를 그대로 반환해라.
                     (예: 거점이 '광운대' -> 입력 '동아리' -> 반환 '광운대학교')
                     
                   - **Case B (프랜차이즈/브랜드):** 입력값이 '맥도날드', '스타벅스', '다이소', '편의점' 등 **특정 브랜드나 체인점**이라면, **생활 거점 이름과 브랜드를 조합하여 가장 가까운 지점명**을 반환해라.
                     (예: 거점이 '중랑구청' -> 입력 '맥도날드 6시' -> 반환 **'맥도날드 중랑점'** 또는 **'맥도날드 중랑구청점'**)
                     (예: 거점이 '광운대' -> 입력 '스타벅스' -> 반환 **'스타벅스 광운대점'**)

                3. [명확한 지명 분석]:
                   - 위 1, 2번에 해당하지 않더라도, 입력값 자체가 명확한 지명(랜드마크, 지하철역, 학교 등)이라면 해당 위치를 반환해라.
                   - 예시: "강남역 11번 출구" -> 좌표 반환.

                4. [장소 없음]:
                   - 위 경우에 해당하지 않는 단순 일반 명사(예: 거점 파악 불가인데 그냥 '카페')는 **반드시 null을 반환해라.**
                   - **사용자의 생활 반경(Reference Data의 주소지들)과 전혀 뜬금없는 지역의 장소를 추천하지 마라.**

                5. 카테고리는 [Category List] 중 하나를 선택해라.
                
                [Output Format]
                            반드시 아래 JSON 형식으로만 응답해라.
                            {
                              "placeName": "장소명" (확실하지 않으면 null),
                              "address": "도로명 주소" (확실하지 않으면 null),
                              "latitude": 37.1234 (확실하지 않으면 null),
                              "longitude": 127.1234 (확실하지 않으면 null),
                              "category": "카테고리명" (없으면 null)
                            }
                """;
    }

}
