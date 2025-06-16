# Multi-stage Dockerfile for Spring Boot Backend
# Stage 1: Build stage with Maven and JDK
FROM maven:3.9.9-amazoncorretto-17 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files first (for better layer caching)
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application
# Use production profile and skip tests for faster builds (tests should run in CI/CD)
RUN mvn clean package -DskipTests -B && \
    # Verify the JAR was created successfully
    ls -la target/

# Stage 2: Runtime stage with JRE only
FROM eclipse-temurin:17-jre AS runtime

# Install required packages and create app user
RUN apt-get update && apt-get install -y --no-install-recommends \
        # For health checks and debugging
        curl \
        # For better signal handling
        tini \
        # Security updates
        tzdata && \
    # Clean up apt cache
    rm -rf /var/lib/apt/lists/* && \
    # Create a non-root user for security
    groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup appuser && \
    # Create application directory
    mkdir -p /app && \
    chown -R appuser:appgroup /app

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar /app/app.jar

# Copy health check script
COPY --chown=appuser:appgroup docker/health-check.sh /app/health-check.sh
RUN chmod +x /app/health-check.sh

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Configure JVM for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+PrintGCDetails \
               -XX:+PrintGCTimeStamps \
               -Xloggc:/app/logs/gc.log \
               -XX:+UseGCLogFileRotation \
               -XX:NumberOfGCLogFiles=3 \
               -XX:GCLogFileSize=100M \
               -Djava.security.egd=file:/dev/./urandom \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=UTC"

# Application configuration
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MANAGEMENT_SERVER_PORT=8081

# Create logs directory
RUN mkdir -p /app/logs

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD ["/app/health-check.sh"]

# Start the application
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]

# Add labels for better container management
LABEL maintainer="bookstore-team" \
      application="bookstore-backend" \
      version="1.0.0" \
      description="Spring Boot backend for Bookstore application" \
      org.opencontainers.image.source="https://github.com/your-org/bookstore-spring-boot" \
      org.opencontainers.image.description="Spring Boot backend for Bookstore application" \
      org.opencontainers.image.vendor="Your Organization"