#!/bin/bash

# Docker Build Script for Production Environment
# This script builds optimized Docker images for production deployment

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
APP_VERSION="${APP_VERSION:-1.0.0}"
REGISTRY="${DOCKER_REGISTRY:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${BLUE}[BUILD-PROD]${NC} $1"
}

success() {
    echo -e "${GREEN}[BUILD-PROD]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[BUILD-PROD]${NC} $1"
}

error() {
    echo -e "${RED}[BUILD-PROD]${NC} $1"
}

# Function to run tests before building
run_tests() {
    log "Running tests before production build..."
    
    # Backend tests
    log "Running backend tests..."
    cd "$PROJECT_ROOT/backend"
    ./mvnw clean test -Pprod
    
    # Frontend tests
    log "Running frontend tests..."
    cd "$PROJECT_ROOT/frontend"
    npm ci
    npm run test:ci
    
    success "All tests passed!"
}

# Function to build backend
build_backend() {
    log "Building backend Docker image for production..."
    
    cd "$PROJECT_ROOT/backend"
    
    # Determine image tags
    local image_name="bookstore/backend"
    if [ -n "$REGISTRY" ]; then
        image_name="$REGISTRY/bookstore/backend"
    fi
    
    # Build the image with production optimizations
    docker build \
        --target runtime \
        --build-arg BUILD_DATE="$BUILD_DATE" \
        --build-arg VCS_REF="$VCS_REF" \
        --build-arg APP_VERSION="$APP_VERSION" \
        --tag "$image_name:$APP_VERSION" \
        --tag "$image_name:latest" \
        --label "org.opencontainers.image.created=$BUILD_DATE" \
        --label "org.opencontainers.image.revision=$VCS_REF" \
        --label "org.opencontainers.image.version=$APP_VERSION" \
        --label "org.opencontainers.image.title=Bookstore Backend" \
        --label "org.opencontainers.image.description=Spring Boot backend for Bookstore application" \
        .
    
    success "Backend Docker image built successfully!"
    
    # Show image details
    log "Backend image size: $(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$image_name:$APP_VERSION")"
}

# Function to build frontend
build_frontend() {
    log "Building frontend Docker image for production..."
    
    cd "$PROJECT_ROOT/frontend"
    
    # Determine image tags
    local image_name="bookstore/frontend"
    if [ -n "$REGISTRY" ]; then
        image_name="$REGISTRY/bookstore/frontend"
    fi
    
    # Build the image with production optimizations
    docker build \
        --target runtime \
        --build-arg BUILD_DATE="$BUILD_DATE" \
        --build-arg VCS_REF="$VCS_REF" \
        --build-arg APP_VERSION="$APP_VERSION" \
        --tag "$image_name:$APP_VERSION" \
        --tag "$image_name:latest" \
        --label "org.opencontainers.image.created=$BUILD_DATE" \
        --label "org.opencontainers.image.revision=$VCS_REF" \
        --label "org.opencontainers.image.version=$APP_VERSION" \
        --label "org.opencontainers.image.title=Bookstore Frontend" \
        --label "org.opencontainers.image.description=React frontend for Bookstore application" \
        .
    
    success "Frontend Docker image built successfully!"
    
    # Show image details
    log "Frontend image size: $(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$image_name:$APP_VERSION")"
}

# Function to scan images for vulnerabilities
security_scan() {
    log "Running security scans on built images..."
    
    # Check if trivy is available
    if command -v trivy >/dev/null 2>&1; then
        log "Scanning backend image with Trivy..."
        trivy image bookstore/backend:$APP_VERSION --severity HIGH,CRITICAL
        
        log "Scanning frontend image with Trivy..."
        trivy image bookstore/frontend:$APP_VERSION --severity HIGH,CRITICAL
    else
        warn "Trivy not installed. Skipping vulnerability scanning."
        warn "Install Trivy for security scanning: https://github.com/aquasecurity/trivy"
    fi
}

# Function to push images to registry
push_images() {
    if [ -z "$REGISTRY" ]; then
        warn "No registry specified. Skipping image push."
        return
    fi
    
    log "Pushing images to registry: $REGISTRY"
    
    docker push "$REGISTRY/bookstore/backend:$APP_VERSION"
    docker push "$REGISTRY/bookstore/backend:latest"
    docker push "$REGISTRY/bookstore/frontend:$APP_VERSION"
    docker push "$REGISTRY/bookstore/frontend:latest"
    
    success "Images pushed to registry successfully!"
}

# Function to generate image manifest
generate_manifest() {
    log "Generating image manifest..."
    
    cat > "$PROJECT_ROOT/docker-manifest.json" << EOF
{
  "build_info": {
    "build_date": "$BUILD_DATE",
    "vcs_ref": "$VCS_REF",
    "app_version": "$APP_VERSION",
    "registry": "$REGISTRY"
  },
  "images": {
    "backend": {
      "name": "${REGISTRY:+$REGISTRY/}bookstore/backend",
      "version": "$APP_VERSION",
      "size": "$(docker images --format "{{.Size}}" ${REGISTRY:+$REGISTRY/}bookstore/backend:$APP_VERSION)"
    },
    "frontend": {
      "name": "${REGISTRY:+$REGISTRY/}bookstore/frontend", 
      "version": "$APP_VERSION",
      "size": "$(docker images --format "{{.Size}}" ${REGISTRY:+$REGISTRY/}bookstore/frontend:$APP_VERSION)"
    }
  }
}
EOF
    
    success "Image manifest generated: docker-manifest.json"
}

# Function to verify builds
verify_builds() {
    log "Verifying built images..."
    
    echo "Docker images:"
    docker images | grep bookstore
    
    echo -e "\nImage inspection:"
    docker inspect bookstore/backend:$APP_VERSION --format='{{.Config.Labels}}'
    docker inspect bookstore/frontend:$APP_VERSION --format='{{.Config.Labels}}'
    
    success "Build verification completed!"
}

# Main function
main() {
    log "Starting production build process..."
    log "Build Date: $BUILD_DATE"
    log "VCS Ref: $VCS_REF"
    log "App Version: $APP_VERSION"
    log "Registry: ${REGISTRY:-'none (local only)'}"
    
    cd "$PROJECT_ROOT"
    
    # Parse command line arguments
    BUILD_BACKEND=true
    BUILD_FRONTEND=true
    RUN_TESTS=true
    SECURITY_SCAN=false
    PUSH_IMAGES=false
    
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
            --skip-tests)
                RUN_TESTS=false
                shift
                ;;
            --security-scan)
                SECURITY_SCAN=true
                shift
                ;;
            --push)
                PUSH_IMAGES=true
                shift
                ;;
            --registry)
                REGISTRY="$2"
                shift 2
                ;;
            --version)
                APP_VERSION="$2"
                shift 2
                ;;
            -h|--help)
                echo "Usage: $0 [OPTIONS]"
                echo "Options:"
                echo "  --backend-only    Build only the backend image"
                echo "  --frontend-only   Build only the frontend image"
                echo "  --skip-tests      Skip running tests before build"
                echo "  --security-scan   Run security vulnerability scanning"
                echo "  --push            Push images to registry after build"
                echo "  --registry URL    Docker registry URL"
                echo "  --version VER     Application version tag"
                echo "  -h, --help        Show this help message"
                echo ""
                echo "Environment variables:"
                echo "  APP_VERSION       Application version (default: 1.0.0)"
                echo "  DOCKER_REGISTRY   Docker registry URL"
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Run tests if not skipped
    if [ "$RUN_TESTS" = true ]; then
        run_tests
    fi
    
    # Build images
    if [ "$BUILD_BACKEND" = true ]; then
        build_backend
    fi
    
    if [ "$BUILD_FRONTEND" = true ]; then
        build_frontend
    fi
    
    # Security scanning
    if [ "$SECURITY_SCAN" = true ]; then
        security_scan
    fi
    
    # Generate manifest
    generate_manifest
    
    # Verify builds
    verify_builds
    
    # Push images if requested
    if [ "$PUSH_IMAGES" = true ]; then
        push_images
    fi
    
    success "Production build completed successfully!"
    
    if [ "$PUSH_IMAGES" = false ] && [ -n "$REGISTRY" ]; then
        log "To push images to registry, run:"
        log "  $0 --push"
    fi
    
    log "To deploy in production, run:"
    log "  docker-compose -f docker-compose.prod.yml up -d"
}

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Run main function with all arguments
main "$@"