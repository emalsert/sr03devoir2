package com.example.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Custom entry point to handle unauthenticated access attempts
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String accept = request.getHeader("Accept");

        // Redirect to appropriate login page for HTML requests
        if (accept != null && accept.contains("text/html")) {
            response.sendRedirect("/admin/login");
        } else {
            // For API clients, return 401 without redirect
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}
