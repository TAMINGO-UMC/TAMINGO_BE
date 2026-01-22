package app.tamingo.domain.gpt.prompt.common;

public class DataPrompt implements PromptTemplate {

    private final String title;
    private final String data;

    public DataPrompt(String title, String data) {
        this.title = title;
        this.data = data;
    }

    @Override
    public String render() {
        return """
            === %s ===
            %s
            """.formatted(title, data);
    }
}
