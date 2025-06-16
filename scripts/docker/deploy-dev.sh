#!/bin/bash

# Docker Deployment Script for Development Environment
# This script deploys the application using Docker Compose for development

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env.dev"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${BLUE}[DEPLOY-DEV]${NC} $1"
}

success() {
    echo -e "${GREEN}[DEPLOY-DEV]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[DEPLOY-DEV]${NC} $1"
}

error() {
    echo -e "${RED}[DEPLOY-DEV]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
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
        warn "Environment file $ENV_FILE not found. Creating from template..."
        cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/$ENV_FILE"
        warn "Please review and update $ENV_FILE with appropriate values."
    fi
    
    success "Prerequisites check completed!"
}

# Function to create necessary directories
create_directories() {
    log "Creating necessary directories..."
    
    mkdir -p "$PROJECT_ROOT/data/mysql"
    mkdir -p "$PROJECT_ROOT/logs/backend"
    mkdir -p "$PROJECT_ROOT/logs/nginx"
    
    success "Directories created!"
}

# Function to build images if needed
build_images() {
    log "Building Docker images..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build
    
    success "Images built successfully!"
}

# Function to start services
start_services() {
    log "Starting services..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d
    
    success "Services started successfully!"
}

# Function to wait for services to be healthy
wait_for_services() {
    log "Waiting for services to be healthy..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        log "Checking service health (attempt $attempt/$max_attempts)..."
        
        # Check MySQL
        if docker exec bookstore-mysql-dev mysqladmin ping -h localhost -u root -pdevpassword >/dev/null 2>&1; then
            success "MySQL is healthy!"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            error "Services did not become healthy within expected time."
            return 1
        fi
        
        sleep 10
        attempt=$((attempt + 1))
    done
    
    # Wait a bit more for backend to start
    log "Waiting for backend to start..."
    sleep 30
    
    # Check backend health
    attempt=1
    while [ $attempt -le 10 ]; do
        if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
            success "Backend is healthy!"
            break
        fi
        
        if [ $attempt -eq 10 ]; then
            warn "Backend health check failed, but continuing..."
        fi
        
        sleep 10
        attempt=$((attempt + 1))
    done
    
    # Check frontend
    if curl -f -s http://localhost:3000/health >/dev/null 2>&1; then
        success "Frontend is healthy!"
    else
        warn "Frontend health check failed, but continuing..."
    fi
    
    success "Service health checks completed!"
}

# Function to show service status
show_status() {
    log "Service status:"
    
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
    echo "  Frontend: http://localhost:3000"
    echo "  Backend API: http://localhost:8080/api"
    echo "  Backend Health: http://localhost:8080/actuator/health"
    echo "  Backend Metrics: http://localhost:8081/actuator/metrics"
    echo "  PhpMyAdmin: http://localhost:8082"
    echo "  Swagger UI: http://localhost:8080/swagger-ui.html"
}

# Function to show logs
show_logs() {
    log "Showing service logs..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f
}

# Function to stop services
stop_services() {
    log "Stopping services..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down
    
    success "Services stopped!"
}

# Function to clean up
cleanup() {
    log "Cleaning up..."
    
    cd "$PROJECT_ROOT"
    
    # Check if we should use docker compose or docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down -v --remove-orphans
    
    # Remove unused networks
    docker network prune -f
    
    success "Cleanup completed!"
}

# Main function
main() {
    cd "$PROJECT_ROOT"
    
    # Parse command line arguments
    ACTION="start"
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            start|up)
                ACTION="start"
                shift
                ;;
            stop|down)
                ACTION="stop"
                shift
                ;;
            restart)
                ACTION="restart"
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
            build)
                ACTION="build"
                shift
                ;;
            cleanup|clean)
                ACTION="cleanup"
                shift
                ;;
            -h|--help)
                echo "Usage: $0 [ACTION]"
                echo "Actions:"
                echo "  start, up      Start the development environment (default)"
                echo "  stop, down     Stop the development environment"
                echo "  restart        Restart the development environment"
                echo "  status         Show service status"
                echo "  logs           Show and follow service logs"
                echo "  build          Build Docker images"
                echo "  cleanup, clean Stop services and remove volumes"
                echo "  -h, --help     Show this help message"
                exit 0
                ;;
            *)
                error "Unknown action: $1"
                exit 1
                ;;
        esac
    done
    
    log "Starting development deployment..."
    log "Action: $ACTION"
    log "Environment: development"
    log "Compose file: $COMPOSE_FILE"
    log "Environment file: $ENV_FILE"
    
    case $ACTION in
        start)
            check_prerequisites
            create_directories
            build_images
            start_services
            wait_for_services
            show_status
            ;;
        stop)
            stop_services
            ;;
        restart)
            stop_services
            sleep 5
            build_images
            start_services
            wait_for_services
            show_status
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        build)
            check_prerequisites
            build_images
            ;;
        cleanup)
            cleanup
            ;;
        *)
            error "Invalid action: $ACTION"
            exit 1
            ;;
    esac
    
    success "Development deployment action '$ACTION' completed!"
}

# Run main function with all arguments
main "$@"