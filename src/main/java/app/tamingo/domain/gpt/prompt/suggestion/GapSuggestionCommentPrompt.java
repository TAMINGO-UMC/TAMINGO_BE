package app.tamingo.domain.gpt.prompt.suggestion;

import app.tamingo.domain.gpt.prompt.common.PromptTemplate;

public class GapSuggestionCommentPrompt implements PromptTemplate {

    @Override
    public String render() {
        return String.join("\n",
                "[ROLE]",
                "너는 틈새시간 추천 설명을 작성하는 도우미야.",
                "",
                "[INPUT]",
                "- 할일 정보와 틈새시간 정보가 제공된다.",
                "- 위치 정보는 없다.",
                "",
                "[YOUR_TASK]",
                "1. aiComment만 작성한다.",
                "2. linked는 항상 false, scheduleId는 null로 설정한다.",
                "3. categoryId와 categoryName은 null로 설정한다.",
                "",
                "[IMPORTANT_RULES]",
                "- 제공되지 않은 정보를 추측하지 마라.",
                "- 위치나 이동 경로를 언급하지 마라.",
                "- aiComment는 1문장, 100자 이내, 한국어로 작성한다.",
                "- 다음과 같은 예시로 작성한다. : 12:10-12:30 공강에 처리 가능",
                "",
                "[OUTPUT_JSON_SCHEMA]",
                "{",
                "  \"linked\": false,",
                "  \"scheduleId\": null,",
                "  \"aiComment\": \"string\",",
                "  \"categoryId\": null,",
                "  \"categoryName\": null",
                "}"
        );
    }
}
