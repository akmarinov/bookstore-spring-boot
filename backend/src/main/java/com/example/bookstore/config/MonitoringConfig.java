package com.example.bookstore.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuration for monitoring and metrics collection
 */
@Configuration
public class MonitoringConfig {

    @Autowired
    private MeterRegistry meterRegistry;


    /**
     * Custom filter to track HTTP request metrics
     */
    @Bean
    public OncePerRequestFilter httpRequestMetricsFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain filterChain) throws ServletException, IOException {
                
                String requestURI = request.getRequestURI();
                String method = request.getMethod();
                
                // Skip actuator endpoints from custom metrics
                if (requestURI.startsWith("/actuator")) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Timer.Sample sample = Timer.start(meterRegistry);
                
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    // Record the request duration
                    sample.stop(Timer.builder("http.server.requests.custom")
                            .description("Custom HTTP request duration")
                            .tag("method", method)
                            .tag("uri", simplifyURI(requestURI))
                            .tag("status", String.valueOf(response.getStatus()))
                            .register(meterRegistry));
                    
                    // Count requests by endpoint
                    meterRegistry.counter("bookstore.http.requests.total",
                            "method", method,
                            "endpoint", simplifyURI(requestURI),
                            "status", String.valueOf(response.getStatus()))
                            .increment();
                }
            }
            
            private String simplifyURI(String uri) {
                // Simplify URIs with IDs to avoid metric explosion
                if (uri.matches("/api/books/\\d+")) {
                    return "/api/books/{id}";
                }
                return uri;
            }
        };
    }
}