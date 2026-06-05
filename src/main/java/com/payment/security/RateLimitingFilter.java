package com.payment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, ClientRequestInfo> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        ClientRequestInfo info = requestCounts.computeIfAbsent(clientIp, k -> new ClientRequestInfo());
        
        synchronized (info) {
            long currentTime = System.currentTimeMillis();
            
            // Reset counter if minute has passed
            if (currentTime - info.windowStart > 60000) {
                info.requestCount = 0;
                info.windowStart = currentTime;
            }
            
            // Check if rate limit exceeded
            if (info.requestCount >= MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Please try again later.\",\"retryAfter\":60}");
                return;
            }
            
            info.requestCount++;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
    
    private static class ClientRequestInfo {
        int requestCount = 0;
        long windowStart = System.currentTimeMillis();
    }
}