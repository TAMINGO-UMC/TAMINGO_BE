package app.tamingo.domain.userlearning.dto;


public record ErrorLogResponse(
        String startPlace,
        String arrivePlace,
        int expectedDuration,
        int actualDuration,
        int errorMin
    ){}
