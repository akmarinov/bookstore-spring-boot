#!/bin/bash

# Docker Build Script for Development Environment
# This script builds Docker images for development

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
APP_VERSION="dev"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${BLUE}[BUILD-DEV]${NC} $1"
}

success() {
    echo -e "${GREEN}[BUILD-DEV]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[BUILD-DEV]${NC} $1"
}

error() {
    echo -e "${RED}[BUILD-DEV]${NC} $1"
}

# Function to build backend
build_backend() {
    log "Building backend Docker image for development..."
    
    cd "$PROJECT_ROOT/backend"
    
    # Build the image
    docker build \
        --target runtime \
        --build-arg BUILD_DATE="$BUILD_DATE" \
        --build-arg VCS_REF="$VCS_REF" \
        --tag bookstore/backend:dev \
        --tag bookstore/backend:latest \
        .
    
    success "Backend Docker image built successfully!"
}

# Function to build frontend
build_frontend() {
    log "Building frontend Docker image for development..."
    
    cd "$PROJECT_ROOT/frontend"
    
    # Build the image
    docker build \
        --target runtime \
        --build-arg BUILD_DATE="$BUILD_DATE" \
        --build-arg VCS_REF="$VCS_REF" \
        --tag bookstore/frontend:dev \
        --tag bookstore/frontend:latest \
        .
    
    success "Frontend Docker image built successfully!"
}

# Function to clean up old images
cleanup() {
    log "Cleaning up old Docker images..."
    
    # Remove dangling images
    docker image prune -f
    
    success "Cleanup completed!"
}

# Function to verify builds
verify_builds() {
    log "Verifying built images..."
    
    echo "Docker images:"
    docker images | grep bookstore
    
    echo -e "\nBackend image details:"
    docker inspect bookstore/backend:dev --format='{{.Config.Labels}}'
    
    echo -e "\nFrontend image details:"
    docker inspect bookstore/frontend:dev --format='{{.Config.Labels}}'
    
    success "Build verification completed!"
}

# Main function
main() {
    log "Starting development build process..."
    log "Build Date: $BUILD_DATE"
    log "VCS Ref: $VCS_REF"
    log "App Version: $APP_VERSION"
    
    cd "$PROJECT_ROOT"
    
    # Parse command line arguments
    BUILD_BACKEND=true
    BUILD_FRONTEND=true
    CLEANUP=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --backend-only)
                BUILD_FRONTEND=false
                shift
                ;;
            --frontend-only)
                BUILD_BACKEND=false
                shift
                ;;
            --cleanup)
                CLEANUP=true
                shift
                ;;
            --no-cleanup)
                CLEANUP=false
                shift
                ;;
            -h|--help)
                echo "Usage: $0 [OPTIONS]"
                echo "Options:"
                echo "  --backend-only   Build only the backend image"
                echo "  --frontend-only  Build only the frontend image"
                echo "  --cleanup        Clean up old Docker images after build"
                echo "  --no-cleanup     Skip cleanup (default)"
                echo "  -h, --help       Show this help message"
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Build images
    if [ "$BUILD_BACKEND" = true ]; then
        build_backend
    fi
    
    if [ "$BUILD_FRONTEND" = true ]; then
        build_frontend
    fi
    
    # Cleanup if requested
    if [ "$CLEANUP" = true ]; then
        cleanup
    fi
    
    # Verify builds
    verify_builds
    
    success "Development build completed successfully!"
    
    log "To start the application, run:"
    log "  docker-compose up -d"
    log ""
    log "To view logs, run:"
    log "  docker-compose logs -f"
}

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Run main function with all arguments
main "$@"