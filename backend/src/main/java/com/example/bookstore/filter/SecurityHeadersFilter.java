package com.example.bookstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security Headers Filter that adds various security-related HTTP headers
 * to protect against common web vulnerabilities.
 */
@Component
@Order(2)
public class SecurityHeadersFilter implements Filter {

    @Value("${app.security.content-security-policy:default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'}")
    private String contentSecurityPolicy;

    @Value("${app.security.referrer-policy:strict-origin-when-cross-origin}")
    private String referrerPolicy;

    @Value("${app.security.permissions-policy:geolocation=(), microphone=(), camera=()}")
    private String permissionsPolicy;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Add security headers
        addSecurityHeaders(httpResponse, (HttpServletRequest) request);

        chain.doFilter(request, response);
    }

    /**
     * Adds comprehensive security headers to the HTTP response.
     */
    private void addSecurityHeaders(HttpServletResponse response, HttpServletRequest request) {
        
        // Content Security Policy - Prevents XSS and data injection attacks
        response.setHeader("Content-Security-Policy", contentSecurityPolicy);

        // X-Frame-Options - Prevents clickjacking attacks
        response.setHeader("X-Frame-Options", "DENY");

        // X-Content-Type-Options - Prevents MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // X-XSS-Protection - Enables XSS filtering (legacy but still useful)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Referrer Policy - Controls how much referrer information is shared
        response.setHeader("Referrer-Policy", referrerPolicy);

        // Permissions Policy (formerly Feature Policy) - Controls browser features
        response.setHeader("Permissions-Policy", permissionsPolicy);

        // Strict-Transport-Security - Enforces HTTPS (only add if using HTTPS)
        // This is also configured in SecurityConfig but adding here as backup
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

        // X-Permitted-Cross-Domain-Policies - Restricts Adobe Flash and PDF cross-domain access
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");

        // Cache-Control for sensitive pages (can be overridden by specific endpoints)
        String requestURI = request.getRequestURI();
        
        if (isSensitiveEndpoint(requestURI)) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        // Cross-Origin-Embedder-Policy - Helps protect against Spectre-like attacks
        response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");

        // Cross-Origin-Opener-Policy - Helps protect against cross-origin attacks
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");

        // Cross-Origin-Resource-Policy - Controls cross-origin resource sharing
        response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
    }

    /**
     * Determines if an endpoint should be treated as sensitive and requires
     * stricter caching policies.
     */
    private boolean isSensitiveEndpoint(String requestURI) {
        // Add patterns for sensitive endpoints
        return requestURI != null && (
                requestURI.contains("/admin") ||
                requestURI.contains("/auth") ||
                requestURI.contains("/login") ||
                requestURI.contains("/user") ||
                requestURI.contains("/profile")
        );
    }
}