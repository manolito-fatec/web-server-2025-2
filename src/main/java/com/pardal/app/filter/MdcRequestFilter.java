package com.pardal.app.filter;

import java.io.IOException;

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

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove(HTTP_METHOD_KEY);
            MDC.remove(REQUEST_URI_KEY);
        }
    }
}
