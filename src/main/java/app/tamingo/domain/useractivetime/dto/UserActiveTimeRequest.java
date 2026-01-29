package app.tamingo.domain.useractivetime.dto;

import java.time.LocalTime;

public record UserActiveTimeRequest (
  LocalTime startTime,
  LocalTime endTime,
  boolean mon,
  boolean tue,
  boolean wed,
  boolean thu,
  boolean fri,
  boolean weekend
) {
}
