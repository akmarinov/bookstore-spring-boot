# Docker Containerization Guide

This document provides comprehensive instructions for building, deploying, and managing the Bookstore application using Docker containers.

## Overview

The application consists of three main services:
- **Backend**: Spring Boot application (Java 17)
- **Frontend**: React application served by nginx
- **Database**: MySQL 8.0 with persistent storage

## Quick Start

### Development Environment

1. **Copy environment configuration:**
   ```bash
   cp .env.example .env.dev
   # Edit .env.dev with your development settings
   ```

2. **Start development environment:**
   ```bash
   ./scripts/docker/deploy-dev.sh start
   ```

3. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - PhpMyAdmin: http://localhost:8082
   - Swagger UI: http://localhost:8080/swagger-ui.html

### Production Environment

1. **Prepare production configuration:**
   ```bash
   cp .env.example .env.prod
   # IMPORTANT: Update all passwords and secrets in .env.prod
   ```

2. **Build production images:**
   ```bash
   ./scripts/docker/build-prod.sh --security-scan
   ```

3. **Deploy to production:**
   ```bash
   ./scripts/docker/deploy-prod.sh deploy
   ```

## Directory Structure

```
├── backend/
│   ├── Dockerfile                 # Multi-stage backend build
│   ├── .dockerignore             # Backend build context exclusions
│   └── docker/
│       └── health-check.sh       # Backend health check script
├── frontend/
│   ├── Dockerfile                # Multi-stage frontend build
│   ├── .dockerignore             # Frontend build context exclusions
│   └── docker/
│       ├── nginx.conf            # Frontend nginx configuration
│       └── health-check.sh       # Frontend health check script
├── docker/
│   ├── mysql/
│   │   ├── init/                 # Database initialization scripts
│   │   └── conf.d/               # MySQL production configuration
│   └── nginx/
│       └── nginx.conf            # Production reverse proxy configuration
├── scripts/docker/
│   ├── build-dev.sh              # Development build script
│   ├── build-prod.sh             # Production build script
│   ├── deploy-dev.sh             # Development deployment script
│   └── deploy-prod.sh            # Production deployment script
├── docker-compose.yml            # Development composition
├── docker-compose.prod.yml       # Production composition
├── .env.example                  # Environment template
├── .env.dev                      # Development environment
└── .env.prod                     # Production environment
```

## Docker Images

### Backend Image

**Base**: Amazon Corretto 17 JRE Alpine  
**Features**:
- Multi-stage build with Maven build stage
- Layer extraction for optimal caching
- Non-root user execution
- JVM optimization for containers
- Health check integration
- Signal handling with tini

**Build**:
```bash
cd backend
docker build -t bookstore/backend:latest .
```

### Frontend Image

**Base**: Node.js 20 Alpine (build) + nginx 1.27 Alpine (runtime)  
**Features**:
- Multi-stage build with npm build stage
- Production nginx configuration
- Security headers and compression
- Non-root user execution
- Health check integration

**Build**:
```bash
cd frontend
docker build -t bookstore/frontend:latest .
```

## Environment Configuration

### Development (.env.dev)

```bash
# Database
MYSQL_ROOT_PASSWORD=devpassword
MYSQL_DATABASE=bookstore_dev_db
MYSQL_USER=bookstore
MYSQL_PASSWORD=bookstore123

# Application
BACKEND_PORT=8080
FRONTEND_PORT=3000
PHPMYADMIN_PORT=8082

# Debug
DEBUG_MODE=true
VERBOSE_LOGGING=true
```

### Production (.env.prod)

```bash
# Database - CHANGE THESE PASSWORDS!
MYSQL_ROOT_PASSWORD=SECURE_ROOT_PASSWORD
MYSQL_DATABASE=booksdb
MYSQL_USER=bookstore
MYSQL_PASSWORD=SECURE_PASSWORD

# Application
BACKEND_PORT=8080
FRONTEND_PORT=80

# Security - GENERATE SECURE VALUES!
JWT_SECRET=YOUR_32_CHAR_MINIMUM_JWT_SECRET
ENCRYPTION_KEY=YOUR_ENCRYPTION_KEY

# Paths
DATA_PATH=/opt/bookstore/data
LOGS_PATH=/opt/bookstore/logs
```

## Build Scripts

### Development Build

```bash
./scripts/docker/build-dev.sh [OPTIONS]

Options:
  --backend-only    Build only backend image
  --frontend-only   Build only frontend image
  --cleanup         Clean up old images after build
  --help           Show help
```

### Production Build

```bash
./scripts/docker/build-prod.sh [OPTIONS]

Options:
  --backend-only    Build only backend image
  --frontend-only   Build only frontend image
  --skip-tests      Skip running tests before build
  --security-scan   Run security vulnerability scanning
  --push           Push images to registry
  --registry URL    Docker registry URL
  --version VER     Application version tag
  --help           Show help
```

## Deployment Scripts

### Development Deployment

```bash
./scripts/docker/deploy-dev.sh [ACTION]

Actions:
  start, up        Start development environment (default)
  stop, down       Stop development environment
  restart          Restart development environment
  status           Show service status
  logs             Show and follow service logs
  build            Build Docker images
  cleanup, clean   Stop services and remove volumes
```

### Production Deployment

```bash
./scripts/docker/deploy-prod.sh [ACTION] [OPTIONS]

Actions:
  deploy, start    Deploy to production (default)
  stop, down       Stop production services
  status           Show service status
  logs             Show and follow service logs
  smoke-test       Run production smoke tests

Options:
  --skip-checks    Skip pre-deployment checks
  --skip-backup    Skip data backup
```

## Service Configuration

### Health Checks

All services include comprehensive health checks:

- **Backend**: Application health endpoint + process check
- **Frontend**: nginx status + application loading check
- **Database**: MySQL ping check

### Resource Limits

Production deployment includes resource constraints:

```yaml
backend:
  deploy:
    resources:
      limits:
        memory: 1.5G
        cpus: '2.0'
      reservations:
        memory: 512M
        cpus: '0.5'

frontend:
  deploy:
    resources:
      limits:
        memory: 128M
        cpus: '0.5'
      reservations:
        memory: 64M
        cpus: '0.1'
```

### Security Features

- Non-root user execution in all containers
- Security headers in nginx configuration
- Resource limits and constraints
- Network isolation
- Vulnerability scanning support (Trivy)

## Monitoring and Logging

### Application Logs

```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql

# View logs with timestamps
docker-compose logs -f -t
```

### Metrics and Health

- Backend metrics: http://localhost:8081/actuator/metrics
- Backend health: http://localhost:8080/actuator/health
- Prometheus metrics: http://localhost:8081/actuator/prometheus

### Log Files

Production logs are stored in mounted volumes:
- Backend logs: `${LOGS_PATH}/backend/`
- nginx logs: `${LOGS_PATH}/nginx/`
- MySQL logs: Container internal

## Backup and Recovery

### Database Backup

```bash
# Manual backup
docker exec bookstore-mysql-prod mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} > backup.sql

# Restore from backup
docker exec -i bookstore-mysql-prod mysql -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} < backup.sql
```

### Data Volumes

Production data is stored in:
- MySQL data: `${DATA_PATH}/mysql/`
- Redis data: `${DATA_PATH}/redis/`

## Troubleshooting

### Common Issues

1. **Port conflicts**:
   ```bash
   # Check port usage
   netstat -tuln | grep :8080
   
   # Change ports in .env file
   BACKEND_PORT=8081
   ```

2. **Memory issues**:
   ```bash
   # Check container resource usage
   docker stats
   
   # Adjust JVM settings in docker-compose
   JAVA_OPTS: "-Xms256m -Xmx512m"
   ```

3. **Database connection issues**:
   ```bash
   # Check database health
   docker exec bookstore-mysql-dev mysqladmin ping -h localhost
   
   # Check database logs
   docker-compose logs mysql
   ```

4. **Build failures**:
   ```bash
   # Clean Docker cache
   docker system prune -a
   
   # Rebuild without cache
   docker-compose build --no-cache
   ```

### Debug Commands

```bash
# Enter container shell
docker exec -it bookstore-backend-dev /bin/sh
docker exec -it bookstore-frontend-dev /bin/sh
docker exec -it bookstore-mysql-dev /bin/bash

# Check container logs
docker logs bookstore-backend-dev
docker logs bookstore-frontend-dev

# Inspect container configuration
docker inspect bookstore-backend-dev
```

## Performance Optimization

### Build Optimization

- Multi-stage builds reduce image size
- Layer caching improves build speed
- .dockerignore files minimize build context

### Runtime Optimization

- JVM settings optimized for containers
- nginx compression and caching
- Connection pooling and keepalives
- Resource limits prevent resource exhaustion

### Network Optimization

- Bridge network with custom subnet
- Service discovery via container names
- Keepalive connections between services

## Security Considerations

1. **Passwords**: Change all default passwords in production
2. **Secrets**: Use secure random values for JWT secrets
3. **Network**: Restrict external access to necessary ports only
4. **Updates**: Regularly update base images for security patches
5. **Scanning**: Use `--security-scan` option to check for vulnerabilities
6. **SSL**: Configure SSL certificates for production (nginx.conf)

## CI/CD Integration

The Docker setup is designed for CI/CD pipelines:

```yaml
# Example CI/CD pipeline steps
- name: Build images
  run: ./scripts/docker/build-prod.sh --skip-tests

- name: Run tests
  run: docker-compose -f docker-compose.test.yml run --rm test

- name: Security scan
  run: ./scripts/docker/build-prod.sh --security-scan

- name: Push to registry
  run: ./scripts/docker/build-prod.sh --push --registry $REGISTRY

- name: Deploy
  run: ./scripts/docker/deploy-prod.sh deploy
```

## Additional Resources

- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [nginx Configuration Reference](https://nginx.org/en/docs/)

For additional help or questions, please refer to the main project documentation or create an issue in the project repository.