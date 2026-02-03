package app.tamingo.domain.onboarding.dto;

public record OnboardingResponse(
        boolean onboardingCompleted
) {
    public static OnboardingResponse completed() {
        return new OnboardingResponse(true);
    }
}