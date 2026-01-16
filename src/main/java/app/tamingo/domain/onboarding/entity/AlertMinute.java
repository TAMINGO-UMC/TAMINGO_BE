package app.tamingo.domain.onboarding.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlertMinute {

    MIN_10(10),
    MIN_15(15),
    MIN_30(30);

    private final int minutes;
}