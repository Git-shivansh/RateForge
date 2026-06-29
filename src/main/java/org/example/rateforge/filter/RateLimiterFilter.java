package org.example.rateforge.filter;

import com.example.rateforge.model.RateLimitResult;
import com.example.rateforge.strategy.RateLimiterFactory;
import com.example.rateforge.strategy.RateLimiterStrategy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per incoming request, before it reaches any controller.
 * Identifies the caller, asks the currently active strategy whether
 * they're allowed through, and either forwards the request or returns
 * 429 Too Many Requests.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterFactory rateLimiterFactory;

    public RateLimitFilter(RateLimiterFactory rateLimiterFactory) {
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // don't rate limit health checks / metrics scraping
        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = resolveUserId(request);
        RateLimiterStrategy strategy = rateLimiterFactory.getStrategy();
        RateLimitResult result = strategy.isAllowed(userId);

        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));

        if (result.isAllowed()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded for user: "
                            + userId + "\"}"
            );
        }
    }

    /**
     * Identifies the caller. In a real system this would come from a
     * validated API key or JWT subject. For this project, a header is
     * used so k6 / Postman can simulate different users easily.
     */
    private String resolveUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            userId = request.getRemoteAddr();
        }
        return userId;
    }
}
