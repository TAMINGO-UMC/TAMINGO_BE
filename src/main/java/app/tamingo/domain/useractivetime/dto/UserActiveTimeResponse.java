package app.tamingo.domain.useractivetime.dto;

import app.tamingo.domain.useractivetime.entity.UserActiveTime;
import lombok.Builder;

import java.time.LocalTime;

@Builder
public record UserActiveTimeResponse (
        LocalTime startTime,
        LocalTime endTime,
        boolean mon,
        boolean tue,
        boolean wed,
        boolean thu,
        boolean fri,
        boolean weekend
){
    public static UserActiveTimeResponse from(UserActiveTime entity) {
        return UserActiveTimeResponse.builder()
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .mon(entity.isMon())
                .tue(entity.isTue())
                .wed(entity.isWed())
                .thu(entity.isThu())
                .fri(entity.isFri())
                .weekend(entity.isWeekend())
                .build();
    }

    public static UserActiveTimeResponse empty() {
        return new UserActiveTimeResponse(
                LocalTime.of(9,0), LocalTime.of(22,0),
                false, false,false,false,false,false
        );
    }
}
