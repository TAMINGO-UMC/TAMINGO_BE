package app.tamingo.domain.schedule.dto;

public record AiInferenceResponse(
        AiPlaceInfo aiInference,
        RecommendTodoResponse context
) {}
