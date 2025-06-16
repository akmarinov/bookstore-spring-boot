#!/bin/bash

# Bookstore Spring Boot Application Build Script
# This script builds both backend and frontend for production deployment

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BUILD_ENV=${1:-prod}
SKIP_TESTS=${SKIP_TESTS:-false}
CLEAN_BUILD=${CLEAN_BUILD:-false}
BACKEND_DIR="backend"
FRONTEND_DIR="frontend"
DIST_DIR="dist"
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}\n"
}

# Function to validate environment
validate_environment() {
    print_header "Validating Build Environment"
    
    # Check if we're in the right directory
    if [[ ! -f "build.sh" ]]; then
        print_error "Build script must be run from the project root directory"
        exit 1
    fi
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    # Check if Node.js is installed
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed or not in PATH"
        exit 1
    fi
    
    # Check if npm is installed
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed or not in PATH"
        exit 1
    fi
    
    # Validate build environment
    case $BUILD_ENV in
        dev|development)
            BUILD_ENV="dev"
            print_info "Building for development environment"
            ;;
        test)
            BUILD_ENV="test"
            print_info "Building for test environment"
            ;;
        prod|production)
            BUILD_ENV="prod"
            print_info "Building for production environment"
            ;;
        *)
            print_error "Invalid build environment: $BUILD_ENV. Use 'dev', 'test', or 'prod'"
            exit 1
            ;;
    esac
    
    print_success "Environment validation completed"
}

# Function to clean previous builds
clean_builds() {
    if [[ "$CLEAN_BUILD" == "true" ]]; then
        print_header "Cleaning Previous Builds"
        
        # Clean backend
        if [[ -d "$BACKEND_DIR" ]]; then
            print_info "Cleaning backend build artifacts"
            cd "$BACKEND_DIR"
            mvn clean -q
            cd ..
        fi
        
        # Clean frontend
        if [[ -d "$FRONTEND_DIR" ]]; then
            print_info "Cleaning frontend build artifacts"
            cd "$FRONTEND_DIR"
            npm run clean
            cd ..
        fi
        
        # Clean root dist directory
        if [[ -d "$DIST_DIR" ]]; then
            print_info "Cleaning distribution directory"
            rm -rf "$DIST_DIR"
        fi
        
        print_success "Clean completed"
    fi
}

# Function to create distribution directory
create_dist_directory() {
    print_info "Creating distribution directory structure"
    mkdir -p "$DIST_DIR"/{backend,frontend,docs,scripts}
}

# Function to build backend
build_backend() {
    print_header "Building Backend (Spring Boot)"
    
    if [[ ! -d "$BACKEND_DIR" ]]; then
        print_error "Backend directory not found: $BACKEND_DIR"
        exit 1
    fi
    
    cd "$BACKEND_DIR"
    
    # Set Maven profile based on build environment
    MAVEN_PROFILE=""
    case $BUILD_ENV in
        dev)
            MAVEN_PROFILE="-Pdev"
            ;;
        test)
            MAVEN_PROFILE="-Ptest"
            ;;
        prod)
            MAVEN_PROFILE="-Pprod"
            ;;
    esac
    
    # Build command
    BUILD_CMD="mvn clean package $MAVEN_PROFILE -Denv=$BUILD_ENV"
    
    if [[ "$SKIP_TESTS" == "true" ]]; then
        BUILD_CMD="$BUILD_CMD -DskipTests"
        print_warning "Skipping backend tests"
    else
        print_info "Running backend tests"
    fi
    
    print_info "Building backend with command: $BUILD_CMD"
    
    if $BUILD_CMD; then
        print_success "Backend build completed successfully"
        
        # Copy artifacts to dist directory
        if [[ -f "target/bookstore-backend-0.0.1-SNAPSHOT.jar" ]]; then
            cp target/bookstore-backend-0.0.1-SNAPSHOT.jar "../$DIST_DIR/backend/"
            print_info "Backend JAR copied to distribution directory"
        fi
        
        # Copy application properties
        cp src/main/resources/application-${BUILD_ENV}.* "../$DIST_DIR/backend/" 2>/dev/null || true
        
    else
        print_error "Backend build failed"
        cd ..
        exit 1
    fi
    
    cd ..
}

# Function to build frontend
build_frontend() {
    print_header "Building Frontend (React + Vite)"
    
    if [[ ! -d "$FRONTEND_DIR" ]]; then
        print_error "Frontend directory not found: $FRONTEND_DIR"
        exit 1
    fi
    
    cd "$FRONTEND_DIR"
    
    # Install dependencies if node_modules doesn't exist
    if [[ ! -d "node_modules" ]]; then
        print_info "Installing frontend dependencies"
        npm ci
    fi
    
    # Set build environment
    export NODE_ENV=$BUILD_ENV
    
    # Build command based on environment
    BUILD_CMD=""
    case $BUILD_ENV in
        dev)
            BUILD_CMD="npm run build:dev"
            ;;
        test)
            BUILD_CMD="npm run build:test"
            ;;
        prod)
            BUILD_CMD="npm run build:prod"
            ;;
    esac
    
    if [[ "$SKIP_TESTS" != "true" ]]; then
        print_info "Running frontend tests"
        if ! npm run test:run; then
            print_error "Frontend tests failed"
            cd ..
            exit 1
        fi
    else
        print_warning "Skipping frontend tests"
    fi
    
    print_info "Building frontend with command: $BUILD_CMD"
    
    if $BUILD_CMD; then
        print_success "Frontend build completed successfully"
        
        # Copy build artifacts to dist directory
        if [[ -d "dist" ]]; then
            cp -r dist/* "../$DIST_DIR/frontend/"
            print_info "Frontend build copied to distribution directory"
        fi
        
    else
        print_error "Frontend build failed"
        cd ..
        exit 1
    fi
    
    cd ..
}

# Function to generate build metadata
generate_build_metadata() {
    print_header "Generating Build Metadata"
    
    # Create build info file
    cat > "$DIST_DIR/build-info.json" << EOF
{
  "buildDate": "$BUILD_DATE",
  "buildEnvironment": "$BUILD_ENV",
  "skipTests": $SKIP_TESTS,
  "cleanBuild": $CLEAN_BUILD,
  "gitCommit": "$(git rev-parse HEAD 2>/dev/null || echo 'unknown')",
  "gitBranch": "$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'unknown')",
  "buildHost": "$(hostname)",
  "buildUser": "$(whoami)",
  "versions": {
    "java": "$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)",
    "maven": "$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')",
    "node": "$(node --version)",
    "npm": "$(npm --version)"
  }
}
EOF
    
    print_success "Build metadata generated"
}

# Function to create deployment scripts
create_deployment_scripts() {
    print_header "Creating Deployment Scripts"
    
    # Create start script for backend
    cat > "$DIST_DIR/scripts/start-backend.sh" << 'EOF'
#!/bin/bash
# Backend startup script

JAR_FILE="backend/bookstore-backend-0.0.1-SNAPSHOT.jar"
SPRING_PROFILE="${SPRING_PROFILES_ACTIVE:-prod}"
JVM_OPTS="${JVM_OPTS:--Xms256m -Xmx512m}"

echo "Starting bookstore backend with profile: $SPRING_PROFILE"
java $JVM_OPTS -Dspring.profiles.active=$SPRING_PROFILE -jar $JAR_FILE
EOF
    
    # Create stop script for backend
    cat > "$DIST_DIR/scripts/stop-backend.sh" << 'EOF'
#!/bin/bash
# Backend stop script

PID_FILE="backend.pid"

if [[ -f "$PID_FILE" ]]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null; then
        echo "Stopping backend process $PID"
        kill $PID
        rm "$PID_FILE"
    else
        echo "Backend process $PID not found"
        rm "$PID_FILE"
    fi
else
    echo "No PID file found, looking for Java process"
    pkill -f "bookstore-backend"
fi
EOF
    
    # Create health check script
    cat > "$DIST_DIR/scripts/health-check.sh" << 'EOF'
#!/bin/bash
# Health check script

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
HEALTH_ENDPOINT="$BACKEND_URL/actuator/health"

echo "Checking backend health at $HEALTH_ENDPOINT"
if curl -f -s "$HEALTH_ENDPOINT" > /dev/null; then
    echo "Backend is healthy"
    exit 0
else
    echo "Backend health check failed"
    exit 1
fi
EOF
    
    # Make scripts executable
    chmod +x "$DIST_DIR/scripts/"*.sh
    
    print_success "Deployment scripts created"
}

# Function to validate build artifacts
validate_build_artifacts() {
    print_header "Validating Build Artifacts"
    
    # Check backend JAR
    if [[ -f "$DIST_DIR/backend/bookstore-backend-0.0.1-SNAPSHOT.jar" ]]; then
        print_success "Backend JAR found"
        
        # Check if JAR is executable
        if java -jar "$DIST_DIR/backend/bookstore-backend-0.0.1-SNAPSHOT.jar" --help > /dev/null 2>&1; then
            print_success "Backend JAR is executable"
        else
            print_warning "Backend JAR may not be properly configured"
        fi
    else
        print_error "Backend JAR not found in distribution directory"
        exit 1
    fi
    
    # Check frontend build
    if [[ -f "$DIST_DIR/frontend/index.html" ]]; then
        print_success "Frontend build found"
    else
        print_error "Frontend build not found in distribution directory"
        exit 1
    fi
    
    # Check build metadata
    if [[ -f "$DIST_DIR/build-info.json" ]]; then
        print_success "Build metadata found"
    else
        print_warning "Build metadata not found"
    fi
}

# Function to display build summary
display_build_summary() {
    print_header "Build Summary"
    
    echo "Build Environment: $BUILD_ENV"
    echo "Build Date: $BUILD_DATE"
    echo "Skip Tests: $SKIP_TESTS"
    echo "Clean Build: $CLEAN_BUILD"
    echo ""
    echo "Artifacts Location: $DIST_DIR/"
    echo "├── backend/"
    echo "│   ├── bookstore-backend-0.0.1-SNAPSHOT.jar"
    echo "│   └── application-${BUILD_ENV}.*"
    echo "├── frontend/"
    echo "│   ├── index.html"
    echo "│   └── assets/"
    echo "├── scripts/"
    echo "│   ├── start-backend.sh"
    echo "│   ├── stop-backend.sh"
    echo "│   └── health-check.sh"
    echo "└── build-info.json"
    echo ""
    
    if [[ -f "$DIST_DIR/build-info.json" ]]; then
        echo "Build Info:"
        cat "$DIST_DIR/build-info.json" | jq '.' 2>/dev/null || cat "$DIST_DIR/build-info.json"
    fi
    
    print_success "Build completed successfully!"
    print_info "To deploy, copy the contents of '$DIST_DIR/' to your target server"
}

# Function to show help
show_help() {
    echo "Bookstore Build Script"
    echo ""
    echo "Usage: $0 [ENVIRONMENT] [OPTIONS]"
    echo ""
    echo "Environments:"
    echo "  dev, development  - Build for development"
    echo "  test              - Build for testing"
    echo "  prod, production  - Build for production (default)"
    echo ""
    echo "Environment Variables:"
    echo "  SKIP_TESTS=true   - Skip running tests"
    echo "  CLEAN_BUILD=true  - Clean before building"
    echo ""
    echo "Examples:"
    echo "  $0                    # Build for production"
    echo "  $0 dev                # Build for development"
    echo "  SKIP_TESTS=true $0    # Build without tests"
    echo "  CLEAN_BUILD=true $0   # Clean build"
}

# Main execution
main() {
    if [[ "$1" == "--help" || "$1" == "-h" ]]; then
        show_help
        exit 0
    fi
    
    print_header "Bookstore Application Build"
    print_info "Starting build process at $(date)"
    
    validate_environment
    clean_builds
    create_dist_directory
    build_backend
    build_frontend
    generate_build_metadata
    create_deployment_scripts
    validate_build_artifacts
    display_build_summary
    
    print_info "Build process completed at $(date)"
}

# Run main function with all arguments
main "$@"