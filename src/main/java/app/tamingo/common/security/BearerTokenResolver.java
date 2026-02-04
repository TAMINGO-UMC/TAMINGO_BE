package app.tamingo.common.security;

import jakarta.servlet.http.HttpServletRequest;

public final class BearerTokenResolver {

    private BearerTokenResolver() {}

    public static String resolve(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) return null;
        if (!header.startsWith("Bearer ")) return null;

        String token = header.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}