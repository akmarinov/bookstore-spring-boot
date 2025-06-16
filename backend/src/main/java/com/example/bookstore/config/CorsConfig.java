package com.example.bookstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Security-focused CORS configuration for the Bookstore application.
 * Configures Cross-Origin Resource Sharing with specific allowed origins,
 * methods, and headers to prevent unauthorized cross-origin requests.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String[] allowedOrigins;
    
    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;
    
    @Value("${app.cors.allowed-headers:Origin,Content-Type,Accept,Authorization,X-Requested-With,Cache-Control}")
    private String[] allowedHeaders;
    
    @Value("${app.cors.exposed-headers:X-Total-Count,X-Page-Count}")
    private String[] exposedHeaders;
    
    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    @Value("${app.cors.max-age:3600}")
    private long maxAge;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .exposedHeaders(exposedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Set specific allowed origins (never use "*" with credentials)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        
        // Set specific allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        
        // Set specific allowed headers instead of "*"
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        
        // Set headers that can be exposed to the client
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(allowCredentials);
        
        // Cache preflight response (in seconds)
        configuration.setMaxAge(maxAge);
        
        // Apply CORS configuration to all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        // More restrictive CORS for actuator endpoints
        CorsConfiguration actuatorConfig = new CorsConfiguration();
        actuatorConfig.setAllowedOrigins(Arrays.asList(allowedOrigins));
        actuatorConfig.setAllowedMethods(Arrays.asList("GET", "OPTIONS"));
        actuatorConfig.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept"));
        actuatorConfig.setAllowCredentials(false);
        actuatorConfig.setMaxAge(maxAge);
        source.registerCorsConfiguration("/actuator/**", actuatorConfig);
        
        return source;
    }
}