#!/bin/bash

# Docker Deployment Script for Production Environment
# This script deploys the application using Docker Compose for production

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${BLUE}[DEPLOY-PROD]${NC} $1"
}

success() {
    echo -e "${GREEN}[DEPLOY-PROD]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[DEPLOY-PROD]${NC} $1"
}

error() {
    echo -e "${RED}[DEPLOY-PROD]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    log "Checking prerequisites for production deployment..."
    
    # Check if running as root (not recommended)
    if [ "$EUID" -eq 0 ]; then
        warn "Running as root is not recommended for production deployment."
        read -p "Do you want to continue? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
    
    # Check if Docker is running
    if ! docker info >/dev/null 2>&1; then
        error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    # Check if Docker Compose is available
    if ! command -v docker-compose >/dev/null 2>&1 && ! docker compose version >/dev/null 2>&1; then
        error "Docker Compose is not available. Please install Docker Compose."
        exit 1
    fi
    
    # Check if environment file exists
    if [ ! -f "$PROJECT_ROOT/$ENV_FILE" ]; then
        error "Production environment file $ENV_FILE not found."
        error "Please create $ENV_FILE with production configuration."
        error "You can start with: cp .env.example $ENV_FILE"
        exit 1
    fi
    
    # Check if critical environment variables are set
    source "$PROJECT_ROOT/$ENV_FILE"
    
    if [ "$MYSQL_ROOT_PASSWORD" = "CHANGE_THIS_IN_PRODUCTION" ] || [ "$MYSQL_PASSWORD" = "CHANGE_THIS_IN_PRODUCTION" ]; then
        error "Default passwords detected in $ENV_FILE"
        error "Please update all passwords before production deployment."
        exit 1
    fi
    
    if [ "$JWT_SECRET" = "GENERATE_SECURE_JWT_SECRET_FOR_PRODUCTION_32_CHARS_MIN" ]; then
        error "Default JWT secret detected in $ENV_FILE"
        error "Please generate a secure JWT secret before production deployment."
        exit 1
    fi
    
    success "Prerequisites check completed!"
}

# Function to create necessary directories with proper permissions
create_directories() {
    log "Creating necessary directories for production..."
    
    local data_path="${DATA_PATH:-./data}"
    local logs_path="${LOGS_PATH:-./logs}"
    
    # Create data directories
    sudo mkdir -p "$data_path/mysql"
    sudo mkdir -p "$data_path/redis"
    
    # Create logs directories
    sudo mkdir -p "$logs_path/backend"
    sudo mkdir -p "$logs_path/nginx"
    
    # Set proper permissions
    sudo chown -R 999:999 "$data_path/mysql"  # MySQL user ID
    sudo chown -R 1001:1001 "$logs_path/backend"  # App user ID
    sudo chown -R 101:101 "$logs_path/nginx"  # Nginx user ID
    
    success "Directories created with proper permissions!"
}

# Function to perform pre-deployment health checks
pre_deployment_checks() {
    log "Performing pre-deployment health checks..."
    
    # Check available disk space
    local available_space=$(df -h "$PROJECT_ROOT" | awk 'NR==2 {print $4}' | sed 's/G//')
    if [ "${available_space%.*}" -lt 5 ]; then
        warn "Less than 5GB of disk space available. Consider freeing up space."
    fi
    
    # Check available memory
    local available_memory=$(free -m | awk 'NR==2{printf "%.0f", $7}')
    if [ "$available_memory" -lt 2048 ]; then
        warn "Less than 2GB of memory available. Performance may be affected."
    fi
    
    # Check if ports are available
    local ports=(80 443 8080 8081 3306)
    for port in "${ports[@]}"; do
        if netstat -tuln | grep -q ":$port "; then
            warn "Port $port is already in use. This may cause conflicts."
        fi
    done
    
    success "Pre-deployment checks completed!"
}

# Function to backup existing data
backup_data() {
    log "Creating backup of existing data..."
    
    local backup_dir="./backups/$(date +%Y%m%d_%H%M%S)"
    local data_path="${DATA_PATH:-./data}"
    
    if [ -d "$data_path" ]; then
        mkdir -p "$backup_dir"
        cp -r "$data_path" "$backup_dir/"
        success "Data backed up to $backup_dir"
    else
        log "No existing data to backup."
    fi
}

# Function to pull latest images
pull_images() {
    log "Pulling latest Docker images..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull
    
    success "Latest images pulled!"
}

# Function to start services with rolling deployment
start_services() {
    log "Starting production services..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    # Start database first
    log "Starting database..."
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d mysql redis
    
    # Wait for database to be ready
    log "Waiting for database to be ready..."
    sleep 30
    
    # Start backend
    log "Starting backend..."
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d backend
    
    # Wait for backend to be ready
    log "Waiting for backend to be ready..."
    sleep 60
    
    # Start frontend and proxy
    log "Starting frontend and proxy..."
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d frontend nginx-proxy
    
    success "Production services started!"
}

# Function to wait for services to be healthy
wait_for_services() {
    log "Waiting for services to be healthy..."
    
    local max_attempts=60
    local attempt=1
    
    # Check MySQL
    while [ $attempt -le $max_attempts ]; do
        if docker exec bookstore-mysql-prod mysqladmin ping -h localhost >/dev/null 2>&1; then
            success "MySQL is healthy!"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            error "MySQL did not become healthy within expected time."
            return 1
        fi
        
        log "Waiting for MySQL... (attempt $attempt/$max_attempts)"
        sleep 10
        attempt=$((attempt + 1))
    done
    
    # Check backend
    attempt=1
    while [ $attempt -le 30 ]; do
        if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
            success "Backend is healthy!"
            break
        fi
        
        if [ $attempt -eq 30 ]; then
            error "Backend did not become healthy within expected time."
            return 1
        fi
        
        log "Waiting for backend... (attempt $attempt/30)"
        sleep 10
        attempt=$((attempt + 1))
    done
    
    # Check frontend
    attempt=1
    while [ $attempt -le 10 ]; do
        if curl -f -s http://localhost/health >/dev/null 2>&1; then
            success "Frontend is healthy!"
            break
        fi
        
        if [ $attempt -eq 10 ]; then
            warn "Frontend health check timeout, but continuing..."
            break
        fi
        
        log "Waiting for frontend... (attempt $attempt/10)"
        sleep 5
        attempt=$((attempt + 1))
    done
    
    success "Service health checks completed!"
}

# Function to run smoke tests
run_smoke_tests() {
    log "Running production smoke tests..."
    
    # Test backend API
    if curl -f -s http://localhost:8080/api/books >/dev/null 2>&1; then
        success "Backend API test passed!"
    else
        error "Backend API test failed!"
        return 1
    fi
    
    # Test frontend
    if curl -f -s http://localhost/ | grep -q "<!DOCTYPE html>"; then
        success "Frontend test passed!"
    else
        error "Frontend test failed!"
        return 1
    fi
    
    # Test database connectivity (through backend)
    if curl -f -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
        success "Database connectivity test passed!"
    else
        error "Database connectivity test failed!"
        return 1
    fi
    
    success "All smoke tests passed!"
}

# Function to show service status
show_status() {
    log "Production service status:"
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
    
    echo ""
    log "Service URLs:"
    echo "  Application: http://localhost/"
    echo "  API: http://localhost/api/"
    echo "  Health: http://localhost:8080/actuator/health"
    echo "  Metrics: http://localhost:8081/actuator/metrics"
    
    echo ""
    log "Resource usage:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" | grep bookstore
}

# Function to stop services gracefully
stop_services() {
    log "Stopping production services gracefully..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    # Stop frontend first
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" stop frontend nginx-proxy
    
    # Stop backend
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" stop backend
    
    # Stop database last
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" stop mysql redis
    
    success "Services stopped gracefully!"
}

# Function to show logs
show_logs() {
    log "Showing production service logs..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f
}

# Main function
main() {
    cd "$PROJECT_ROOT"
    
    # Parse command line arguments
    ACTION="deploy"
    SKIP_CHECKS=false
    SKIP_BACKUP=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            deploy|start|up)
                ACTION="deploy"
                shift
                ;;
            stop|down)
                ACTION="stop"
                shift
                ;;
            status)
                ACTION="status"
                shift
                ;;
            logs)
                ACTION="logs"
                shift
                ;;
            smoke-test)
                ACTION="smoke-test"
                shift
                ;;
            --skip-checks)
                SKIP_CHECKS=true
                shift
                ;;
            --skip-backup)
                SKIP_BACKUP=true
                shift
                ;;
            -h|--help)
                echo "Usage: $0 [ACTION] [OPTIONS]"
                echo "Actions:"
                echo "  deploy, start, up  Deploy to production (default)"
                echo "  stop, down         Stop production services"
                echo "  status             Show service status"
                echo "  logs               Show and follow service logs"
                echo "  smoke-test         Run production smoke tests"
                echo "Options:"
                echo "  --skip-checks      Skip pre-deployment checks"
                echo "  --skip-backup      Skip data backup"
                echo "  -h, --help         Show this help message"
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    log "Starting production deployment..."
    log "Action: $ACTION"
    log "Environment: production"
    log "Compose file: $COMPOSE_FILE"
    log "Environment file: $ENV_FILE"
    
    case $ACTION in
        deploy)
            check_prerequisites
            if [ "$SKIP_CHECKS" = false ]; then
                pre_deployment_checks
            fi
            if [ "$SKIP_BACKUP" = false ]; then
                backup_data
            fi
            create_directories
            pull_images
            start_services
            wait_for_services
            run_smoke_tests
            show_status
            ;;
        stop)
            stop_services
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        smoke-test)
            run_smoke_tests
            ;;
        *)
            error "Invalid action: $ACTION"
            exit 1
            ;;
    esac
    
    success "Production deployment action '$ACTION' completed!"
    
    if [ "$ACTION" = "deploy" ]; then
        log ""
        log "Production deployment completed successfully!"
        log "Monitor the application at: http://localhost/"
        log "Check logs with: $0 logs"
        log "Check status with: $0 status"
    fi
}

# Confirmation for production deployment
if [ "$1" != "status" ] && [ "$1" != "logs" ] && [ "$1" != "smoke-test" ]; then
    warn "This script will deploy to PRODUCTION environment."
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log "Deployment cancelled."
        exit 0
    fi
fi

# Run main function with all arguments
main "$@"