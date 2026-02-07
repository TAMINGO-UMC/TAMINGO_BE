package app.tamingo.domain.home.converter;

import app.tamingo.domain.home.dto.FindRouteResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RouteColorResolver {

    private static final String BUS_TRUNK_BLUE = "#0052A4";
    private static final String BUS_BRANCH_GREEN = "#00A651";
    private static final String BUS_EXPRESS_RED = "#D32F2F";
    private static final String BUS_LOOP_YELLOW = "#F4C20D";
    private static final String BUS_NIGHT_PURPLE = "#5A2D82";
    private static final String BUS_AIRPORT_ORANGE = "#F28C28";
    private static final String BUS_DEFAULT = BUS_TRUNK_BLUE;

    public String resolve(FindRouteResponse.TransportMode mode, String routeName) {
        if (mode == null || routeName == null || routeName.isBlank()) {
            return null;
        }
        return switch (mode) {
            case SUBWAY -> resolveSubway(routeName);
            case BUS -> resolveBus(routeName);
            default -> null;
        };
    }

    private String resolveSubway(String routeName) {
        String normalized = normalize(routeName);
        for (SubwayRule rule : RouteColorCatalog.SUBWAY_RULES) {
            if (normalized.contains(rule.keyword)) {
                return rule.color;
            }
        }
        return null;
    }

    private String resolveBus(String routeName) {
        String normalized = normalize(routeName);
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.startsWith("N")) {
            return BUS_NIGHT_PURPLE;
        }
        if (normalized.startsWith("M")) {
            return BUS_EXPRESS_RED;
        }
        if (normalized.startsWith("A")) {
            return BUS_AIRPORT_ORANGE;
        }

        if (normalized.matches("^6\\d{3}$")) {
            return BUS_AIRPORT_ORANGE;
        }
        if (normalized.matches("^9\\d{3}$")) {
            return BUS_EXPRESS_RED;
        }
        if (normalized.matches("^\\d{4}$")) {
            return BUS_BRANCH_GREEN;
        }
        if (normalized.matches("^\\d{3}$")) {
            return BUS_TRUNK_BLUE;
        }
        if (normalized.matches("^\\d{1,2}$")) {
            return BUS_LOOP_YELLOW;
        }
        if (containsHangul(normalized)) {
            return BUS_BRANCH_GREEN;
        }
        if (normalized.contains("-")) {
            return BUS_EXPRESS_RED;
        }
        return BUS_DEFAULT;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value
                .trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .toUpperCase(Locale.ROOT);
    }

    private boolean containsHangul(String value) {
        for (int i = 0; i < value.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(value.charAt(i));
            if (block == Character.UnicodeBlock.HANGUL_SYLLABLES
                    || block == Character.UnicodeBlock.HANGUL_JAMO
                    || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                return true;
            }
        }
        return false;
    }

    record SubwayRule(String keyword, String color) {}
}
