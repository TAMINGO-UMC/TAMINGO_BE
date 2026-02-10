package app.tamingo.common.security;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.BaseCode;
import app.tamingo.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth/")
                || path.startsWith("/api/terms/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = BearerTokenResolver.resolve(request);

            if (token != null) {
                jwtTokenProvider.validateOrThrow(token);

                Long userId = jwtTokenProvider.getUserId(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (CustomException e) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, e.getErrorCode());
        } catch (Exception e) {
            log.error("Unexpected error in JwtAuthFilter", e);
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, BaseCode code)
            throws IOException {

        response.setStatus(code.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> body = ApiResponse.onFailure(code, null);
        response.getWriter().write(
                objectMapper.writeValueAsString(body)
        );
    }
}