package com.pardal.app.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MdcRequestFilter extends OncePerRequestFilter
{
    private static final String HTTP_METHOD_KEY = "httpMethod";
    private static final String REQUEST_URI_KEY = "requestURI";
    private static final String USER_EMAIL_KEY = "userEmail";
    private static final String REMOTE_IP_KEY = "remoteIp";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String httpMethod = request.getMethod();
            String requestURI = request.getRequestURI();

            MDC.put(HTTP_METHOD_KEY, httpMethod);
            MDC.put(REQUEST_URI_KEY, requestURI);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                String userEmail = null;

                if (principal instanceof UserDetails) {
                    userEmail = ((UserDetails) principal).getUsername();
                } else if (principal instanceof String) {
                    userEmail = principal.toString();
                }

                if (userEmail != null) {
                    MDC.put(USER_EMAIL_KEY, userEmail);
                }
            }
            

            String remoteIp = extractClientIp(request);
            if (remoteIp != null) {
                MDC.put(REMOTE_IP_KEY, remoteIp);
            }


            filterChain.doFilter(request, response);

        } finally {
            MDC.remove(HTTP_METHOD_KEY);
            MDC.remove(REQUEST_URI_KEY);
            MDC.remove(USER_EMAIL_KEY);
            MDC.remove(REMOTE_IP_KEY);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] parts = xff.split(",");
            return parts[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
