package com.invoiceverification.invoice_verification_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestRateLimitFilter extends OncePerRequestFilter {
    private static final Duration PUBLIC_WINDOW = Duration.ofMinutes(10);
    private static final Duration ADMIN_WINDOW = Duration.ofMinutes(15);
    private static final int PUBLIC_LIMIT = 10;
    private static final int ADMIN_FAILURE_LIMIT = 10;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong requests = new AtomicLong();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Rule rule = ruleFor(request);
        if (rule == null) { chain.doFilter(request, response); return; }
        long now = System.currentTimeMillis();
        String key = rule.name + ':' + request.getRemoteAddr();
        Window window = windows.compute(key, (ignored, current) ->
                current == null || now - current.startedAt >= rule.duration.toMillis()
                        ? new Window(now, 1) : new Window(current.startedAt, current.count + 1));
        if (window.count > rule.limit) {
            long retryAfter = Math.max(1, (rule.duration.toMillis() - (now - window.startedAt)) / 1000);
            response.setStatus(429); response.setContentType("application/json");
            response.setHeader("Retry-After", Long.toString(retryAfter)); response.setHeader("Cache-Control", "no-store");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}"); return;
        }
        chain.doFilter(request, response);
        if (rule.name.equals("admin") && response.getStatus() < 400) windows.remove(key);
        if (requests.incrementAndGet() % 1000 == 0) windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt > ADMIN_WINDOW.toMillis());
    }

    private Rule ruleFor(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (request.getMethod().equals("POST") && path.matches("/api/public/brands/[^/]+/invoices"))
            return new Rule("public", PUBLIC_LIMIT, PUBLIC_WINDOW);
        if (path.startsWith("/api/admin/")) return new Rule("admin", ADMIN_FAILURE_LIMIT, ADMIN_WINDOW);
        return null;
    }
    private record Rule(String name, int limit, Duration duration) { }
    private record Window(long startedAt, int count) { }
}
