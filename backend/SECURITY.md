# Security Configuration

This document outlines the security features implemented in the Bookstore Spring Boot application.

## Security Features

### 1. Spring Security Configuration (`SecurityConfig.java`)
- **HTTP Security**: Configured with proper CORS support
- **Authentication**: Disabled for demo purposes (can be easily enabled)
- **CSRF Protection**: Configurable via properties (disabled by default for REST API)
- **Session Management**: Stateless configuration for REST API
- **Security Headers**: Comprehensive security headers configuration

### 2. CORS Configuration (`CorsConfig.java`)
- **Specific Origins**: Configured with specific allowed origins (no wildcards)
- **Limited Methods**: Only allows necessary HTTP methods
- **Specific Headers**: Restricts allowed headers instead of using wildcards
- **Credentials Handling**: Properly configured for cookie-based authentication
- **Preflight Caching**: Optimized with appropriate max-age settings

### 3. Input Sanitization (`SecurityUtils.java`)
- **HTML Sanitization**: Removes dangerous HTML tags while preserving safe formatting
- **XSS Protection**: Validates input against XSS attack patterns
- **SQL Injection Prevention**: Basic pattern validation (works with parameterized queries)
- **Input Validation**: Comprehensive input validation and sanitization methods

### 4. XSS Protection Filter (`XssProtectionFilter.java`)
- **Request Parameter Sanitization**: Automatically sanitizes all request parameters
- **Header Sanitization**: Sanitizes HTTP headers (excluding safe headers)
- **XSS Detection**: Logs potential XSS attacks for monitoring
- **Response Headers**: Adds XSS protection headers

### 5. Security Headers Filter (`SecurityHeadersFilter.java`)
- **Content Security Policy (CSP)**: Prevents XSS and data injection attacks
- **X-Frame-Options**: Prevents clickjacking attacks
- **X-Content-Type-Options**: Prevents MIME type sniffing
- **Referrer Policy**: Controls referrer information sharing
- **Permissions Policy**: Restricts browser features
- **HSTS**: Enforces HTTPS connections
- **Cache Control**: Prevents caching of sensitive content

### 6. Data Transfer Object (DTO) Security
- **Automatic Sanitization**: All string setters automatically sanitize input
- **HTML Support**: Description fields allow safe HTML tags
- **Validation**: Comprehensive validation annotations

## Configuration Properties

### Development Environment (`application.properties`)
```properties
# CORS configuration
app.cors.allowed-origins=http://localhost:3000,http://localhost:3001
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=Origin,Content-Type,Accept,Authorization,X-Requested-With,Cache-Control

# Security configuration
app.security.enable-csrf=false
app.security.public-endpoints=/api/books/**,/api/health,/swagger-ui/**,/api-docs/**,/actuator/health
app.security.content-security-policy=default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'
```

### Production Environment (`application-prod.yml`)
```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: Origin,Content-Type,Accept,Authorization,X-Requested-With
  security:
    enable-csrf: false
    content-security-policy: "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' https:; font-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'; upgrade-insecure-requests"
```

## Security Headers Applied

| Header | Value | Purpose |
|--------|-------|---------|
| Content-Security-Policy | Configurable | Prevents XSS and data injection |
| X-Frame-Options | DENY | Prevents clickjacking |
| X-Content-Type-Options | nosniff | Prevents MIME type sniffing |
| X-XSS-Protection | 1; mode=block | Legacy XSS protection |
| Referrer-Policy | strict-origin-when-cross-origin | Controls referrer information |
| Strict-Transport-Security | max-age=31536000; includeSubDomains; preload | Enforces HTTPS |
| Permissions-Policy | Restrictive | Limits browser features |

## Best Practices Implemented

1. **Defense in Depth**: Multiple layers of security (filters, validation, sanitization)
2. **Least Privilege**: Restrictive CORS and CSP policies
3. **Input Validation**: Comprehensive validation and sanitization
4. **Secure Defaults**: Secure configuration out of the box
5. **Monitoring**: Logging of potential security violations
6. **Production Ready**: Stricter settings for production environment

## Development vs Production

- **Development**: More permissive CSP to allow development tools
- **Production**: Stricter CSP and security policies
- **Environment Variables**: Production origins configured via environment variables
- **Logging**: Different logging levels for development and production

## Enabling Authentication

To enable authentication, modify `SecurityConfig.java`:

```java
.authorizeHttpRequests(authz -> authz
    .requestMatchers(publicEndpoints).permitAll()
    .anyRequest().authenticated() // Change from permitAll() to authenticated()
)
```

## Monitoring and Logging

- XSS attacks are logged at WARN level
- Security violations are tracked for monitoring
- Production logging is configured to capture security events

## Testing Security

1. **XSS Testing**: Try submitting `<script>alert('xss')</script>` in form fields
2. **CORS Testing**: Test cross-origin requests from unauthorized domains
3. **Header Testing**: Verify security headers are present in responses
4. **Input Validation**: Test with malicious payloads and verify sanitization

## Dependencies

- `spring-boot-starter-security`: Core security framework
- `owasp-java-html-sanitizer`: HTML sanitization library
- `spring-boot-starter-validation`: Input validation