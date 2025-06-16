# Multi-stage Dockerfile for React Frontend
# Stage 1: Build stage with Node.js
FROM node:20.18-alpine3.20 AS builder

# Install build dependencies
RUN apk add --no-cache \
    git \
    python3 \
    make \
    g++ \
    && rm -rf /var/cache/apk/*

# Set working directory
WORKDIR /app

# Copy package files first (for better layer caching)
COPY package*.json ./

# Install dependencies
RUN npm install && \
    npm cache clean --force

# Copy source code
COPY . .

# Build the application for production
RUN npm run build:prod

# Verify build output
RUN ls -la dist/ && \
    echo "Build completed successfully"

# Stage 2: Production stage with nginx
FROM nginx:1.27-alpine AS runtime

# Install required packages and create app user
RUN apk add --no-cache \
        # For health checks and debugging
        curl \
        # Security updates
        tzdata \
        # For better process handling
        tini && \
    # Create a non-root user for security
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup && \
    # Remove default nginx content
    rm -rf /usr/share/nginx/html/* && \
    # Create necessary directories
    mkdir -p /var/cache/nginx /var/log/nginx && \
    # Fix permissions
    chown -R appuser:appgroup /var/cache/nginx /var/log/nginx /etc/nginx/conf.d

# Copy built application from builder stage
COPY --from=builder --chown=appuser:appgroup /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY --chown=appuser:appgroup docker/nginx.conf /etc/nginx/conf.d/default.conf

# Copy health check script
COPY --chown=appuser:appgroup docker/health-check.sh /usr/local/bin/health-check.sh
RUN chmod +x /usr/local/bin/health-check.sh

# Create custom nginx configuration for non-root user
RUN echo "pid /tmp/nginx.pid;" > /etc/nginx/nginx.conf && \
    echo "worker_processes auto;" >> /etc/nginx/nginx.conf && \
    echo "error_log /var/log/nginx/error.log warn;" >> /etc/nginx/nginx.conf && \
    echo "events {" >> /etc/nginx/nginx.conf && \
    echo "    worker_connections 1024;" >> /etc/nginx/nginx.conf && \
    echo "    use epoll;" >> /etc/nginx/nginx.conf && \
    echo "    multi_accept on;" >> /etc/nginx/nginx.conf && \
    echo "}" >> /etc/nginx/nginx.conf && \
    echo "http {" >> /etc/nginx/nginx.conf && \
    echo "    include /etc/nginx/mime.types;" >> /etc/nginx/nginx.conf && \
    echo "    default_type application/octet-stream;" >> /etc/nginx/nginx.conf && \
    echo "    sendfile on;" >> /etc/nginx/nginx.conf && \
    echo "    tcp_nopush on;" >> /etc/nginx/nginx.conf && \
    echo "    tcp_nodelay on;" >> /etc/nginx/nginx.conf && \
    echo "    keepalive_timeout 65;" >> /etc/nginx/nginx.conf && \
    echo "    types_hash_max_size 2048;" >> /etc/nginx/nginx.conf && \
    echo "    client_max_body_size 16M;" >> /etc/nginx/nginx.conf && \
    echo "    # Logging format" >> /etc/nginx/nginx.conf && \
    echo "    log_format combined '\$remote_addr - \$remote_user [\$time_local] \"' >> /etc/nginx/nginx.conf && \
    echo "                       '\$request\" \$status \$body_bytes_sent \"' >> /etc/nginx/nginx.conf && \
    echo "                       '\$http_referer\" \"\$http_user_agent\"';" >> /etc/nginx/nginx.conf && \
    echo "    # Include server configurations" >> /etc/nginx/nginx.conf && \
    echo "    include /etc/nginx/conf.d/*.conf;" >> /etc/nginx/nginx.conf && \
    echo "}" >> /etc/nginx/nginx.conf

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 80

# Environment configuration
ENV NODE_ENV=production
ENV NGINX_PORT=80

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD ["/usr/local/bin/health-check.sh"]

# Use tini for proper signal handling
ENTRYPOINT ["/sbin/tini", "--"]

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

# Add labels for better container management
LABEL maintainer="bookstore-team" \
      application="bookstore-frontend" \
      version="1.0.0" \
      description="React frontend for Bookstore application" \
      org.opencontainers.image.source="https://github.com/your-org/bookstore-spring-boot" \
      org.opencontainers.image.description="React frontend for Bookstore application served by nginx" \
      org.opencontainers.image.vendor="Your Organization"