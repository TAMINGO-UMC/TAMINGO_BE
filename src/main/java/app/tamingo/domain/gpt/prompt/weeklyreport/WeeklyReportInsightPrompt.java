package app.tamingo.domain.gpt.prompt.weeklyreport;

import app.tamingo.domain.gpt.prompt.common.PromptTemplate;
import app.tamingo.domain.weeklyreport.enums.WeeklyInsightType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class WeeklyReportInsightPrompt implements PromptTemplate {

    private final String allowedTypes = Arrays.stream(WeeklyInsightType.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    @Override
    public String render() {
        return String.join("\n",
                "[ROLE]",
                "너는 사용자의 주간 기록을 분석해 주간리포트 인사이트 3개를 생성하는 도우미다.",
                "",
                "[INPUT]",
                "- WEEKLY_REPORT_DATA에 포함된 데이터만 근거로 사용한다.",
                "- 데이터에 없는 사실을 추측하지 않는다.",
                "",
                "[YOUR_TASK]",
                "1) 인사이트는 정확히 3개 생성한다.",
                "2) 각각은 type, title, content를 가진다.",
                "3) content는 한국어 1~2문장, 120자 이내.",
                "4) 사용자를 비난하지 말고, 실행 가능한 제안을 포함한다.",
                "",
                "[TYPE_RULES]",
                "- type은 반드시 아래 enum 중 하나만 사용한다.",
                "- allowed: " + allowedTypes,
                "- 3개 인사이트는 type이 서로 겹치지 않게(중복 금지).",
                "",
                "[OUTPUT_RULES]",
                "- 출력은 JSON만. 설명/마크다운/코드블럭 금지.",
                "",
                "[OUTPUT_JSON_SCHEMA]",
                "{",
                "  \"insights\": [",
                "    { \"type\": \"ENUM\", \"title\": \"string\", \"content\": \"string\" },",
                "    { \"type\": \"ENUM\", \"title\": \"string\", \"content\": \"string\" },",
                "    { \"type\": \"ENUM\", \"title\": \"string\", \"content\": \"string\" }",
                "  ],",
                "  \"modelVersion\": \"string\"",
                "}"
        );
    }
}
