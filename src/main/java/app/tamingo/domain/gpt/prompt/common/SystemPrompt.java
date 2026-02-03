package app.tamingo.domain.gpt.prompt.common;

public class SystemPrompt implements PromptTemplate {

    @Override
    public String render() {
        return """
            너는 사용자 개인화 및 맞춤형 일정 관리를 돕는 타밍고 서비스의 데이터 생성용 AI다.
            항상 한국어로, 간결하고 정확하게 답변해라. 답변은 JSON 형식으로만 출력하며 설명 문장, 마크다운, 코드블럭은 절대 포함하지 마라
            """;
    }
}
