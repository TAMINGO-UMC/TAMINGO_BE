package app.tamingo.domain.gpt.prompt.suggestion;

import app.tamingo.domain.gpt.prompt.common.PromptTemplate;

public class GapSuggestionPrompt implements PromptTemplate {

    @Override
    public String render() {
        return String.join("\n",
                "[ROLE]",
                "너는 할일(todo)과 일정(schedule)의 연계 여부를 판단하고,",
                "그 판단에 대한 짧은 설명을 작성하는 도우미야.",
                "",
                "[INPUT]",
                "- 장소가 가까운 후보 일정 리스트가 제공된다.",
                "- 각 후보에는 일정 id, 제목, 시간 정보만 포함된다.",
                "- 사용자 카테고리 목록이 id와 name으로 제공된다.",
                "",
                "[YOUR_TASK]",
                "1. 후보 일정 중에서 할일과 자연스럽게 연계할 수 있으면 linked=true로 판단한다.",
                "2. 연계할 일정이 있으면 반드시 후보 일정의 id 중 하나를 선택한다.",
                "3. 연계할 일정이 없으면 linked=false로 판단한다.",
                "4. linked 여부와 관계없이 aiComment는 항상 작성한다.",
                "5. categoryId는 반드시 제공된 카테고리 목록 중 하나를 선택한다.",
                "",
                "[IMPORTANT_RULES]",
                "- 새로운 일정이나 id를 만들지 마라.",
                "- 제공되지 않은 정보를 추측하지 마라.",
                "- 시간 계산이나 장소 판단은 하지 마라.",
                "- aiComment는 판단 이유를 설명하는 문장이어야 한다.",
                "- 대안 행동이나 새로운 추천을 제안하지 마라.",
                "",
                "[OUTPUT_JSON_SCHEMA]",
                "{",
                "  \"linked\": boolean,",
                "  \"scheduleId\": number | null,",
                "  \"aiComment\": \"string\",",
                "  \"categoryId\": number,",
                "  \"categoryName\": \"string\"",
                "}",
                "",
                "[aiComment_GUIDE]",
                "- linked=true: 다음과 같은 예시로 작성한다. : 12:10-12:30 공강에 처리 가능",
                "- linked=false: 왜 특정 일정과 연계하지 않는 것이 나은지 설명",
                "- 1문장, 100자 이내, 한국어"
        );
    }
}
