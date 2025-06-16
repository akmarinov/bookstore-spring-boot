#!/bin/bash

# Environment Validation Script
# Validates that all required tools and dependencies are installed

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}\n"
}

# Validation functions
check_command() {
    local cmd="$1"
    local name="$2"
    local min_version="$3"
    
    if command -v "$cmd" &> /dev/null; then
        local version=$($cmd --version 2>&1 | head -n1 | grep -oE '[0-9]+\.[0-9]+(\.[0-9]+)?' | head -n1)
        if [[ -n "$version" ]]; then
            print_success "$name is installed (version: $version)"
            if [[ -n "$min_version" ]]; then
                if [[ "$version" != "$min_version" ]] && [[ "$version" < "$min_version" ]]; then
                    print_warning "$name version $version is below recommended minimum $min_version"
                fi
            fi
        else
            print_success "$name is installed"
        fi
        return 0
    else
        print_error "$name is not installed or not in PATH"
        return 1
    fi
}

check_java() {
    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1-2)
        local java_major=$(echo $java_version | cut -d'.' -f1)
        
        # Handle Java 9+ version format
        if [[ $java_major -ge 9 ]]; then
            java_version=$java_major
        fi
        
        print_success "Java is installed (version: $java_version)"
        
        # Check if Java 17 or higher
        if [[ $java_version -ge 17 ]]; then
            print_success "Java version meets requirements (17+)"
        else
            print_warning "Java version $java_version is below recommended minimum 17"
        fi
        
        # Check JAVA_HOME
        if [[ -n "$JAVA_HOME" ]]; then
            print_success "JAVA_HOME is set: $JAVA_HOME"
        else
            print_warning "JAVA_HOME environment variable is not set"
        fi
        
        return 0
    else
        print_error "Java is not installed or not in PATH"
        return 1
    fi
}

check_maven() {
    if command -v mvn &> /dev/null; then
        local mvn_version=$(mvn -version 2>&1 | head -n1 | awk '{print $3}')
        print_success "Maven is installed (version: $mvn_version)"
        
        # Check if Maven 3.6+ for better performance
        if [[ "$mvn_version" > "3.6" || "$mvn_version" == "3.6"* ]]; then
            print_success "Maven version meets recommendations (3.6+)"
        else
            print_warning "Maven version $mvn_version is below recommended 3.6+"
        fi
        
        # Check Maven settings
        local maven_home=$(mvn -version 2>&1 | grep "Maven home:" | cut -d' ' -f3)
        if [[ -n "$maven_home" ]]; then
            print_info "Maven home: $maven_home"
        fi
        
        return 0
    else
        print_error "Maven is not installed or not in PATH"
        return 1
    fi
}

check_node() {
    if command -v node &> /dev/null; then
        local node_version=$(node --version | sed 's/v//')
        local node_major=$(echo $node_version | cut -d'.' -f1)
        
        print_success "Node.js is installed (version: $node_version)"
        
        # Check if Node 18+
        if [[ $node_major -ge 18 ]]; then
            print_success "Node.js version meets requirements (18+)"
        else
            print_warning "Node.js version $node_version is below recommended minimum 18"
        fi
        
        return 0
    else
        print_error "Node.js is not installed or not in PATH"
        return 1
    fi
}

check_npm() {
    if command -v npm &> /dev/null; then
        local npm_version=$(npm --version)
        local npm_major=$(echo $npm_version | cut -d'.' -f1)
        
        print_success "npm is installed (version: $npm_version)"
        
        # Check if npm 8+
        if [[ $npm_major -ge 8 ]]; then
            print_success "npm version meets requirements (8+)"
        else
            print_warning "npm version $npm_version is below recommended minimum 8"
        fi
        
        return 0
    else
        print_error "npm is not installed or not in PATH"
        return 1
    fi
}

check_git() {
    if command -v git &> /dev/null; then
        local git_version=$(git --version | awk '{print $3}')
        print_success "Git is installed (version: $git_version)"
        return 0
    else
        print_warning "Git is not installed (recommended for development)"
        return 1
    fi
}

check_docker() {
    if command -v docker &> /dev/null; then
        local docker_version=$(docker --version | awk '{print $3}' | sed 's/,//')
        print_success "Docker is installed (version: $docker_version)"
        
        # Check if Docker is running
        if docker info &> /dev/null; then
            print_success "Docker daemon is running"
        else
            print_warning "Docker is installed but daemon is not running"
        fi
        
        return 0
    else
        print_info "Docker is not installed (optional for containerized deployment)"
        return 0
    fi
}

check_database_tools() {
    print_header "Database Tools"
    
    # Check MySQL client
    if command -v mysql &> /dev/null; then
        local mysql_version=$(mysql --version | awk '{print $5}' | sed 's/,//')
        print_success "MySQL client is installed (version: $mysql_version)"
    else
        print_info "MySQL client is not installed (optional)"
    fi
    
    # Check if MySQL server is running (if installed)
    if command -v mysqladmin &> /dev/null; then
        if mysqladmin ping -h localhost --silent 2>/dev/null; then
            print_success "MySQL server is running on localhost"
        else
            print_info "MySQL server is not running on localhost (or not accessible)"
        fi
    fi
}

check_ports() {
    print_header "Port Availability"
    
    local ports=(8080 5173 3306)
    local port_names=("Backend (Spring Boot)" "Frontend (Vite)" "MySQL")
    
    for i in "${!ports[@]}"; do
        local port="${ports[$i]}"
        local name="${port_names[$i]}"
        
        if netstat -an 2>/dev/null | grep -q ":$port.*LISTEN" || lsof -i ":$port" 2>/dev/null | grep -q LISTEN; then
            print_warning "Port $port is in use ($name)"
        else
            print_success "Port $port is available ($name)"
        fi
    done
}

check_project_structure() {
    print_header "Project Structure"
    
    local required_files=(
        "backend/pom.xml"
        "backend/src/main/java"
        "frontend/package.json"
        "frontend/src"
        "build.sh"
    )
    
    for file in "${required_files[@]}"; do
        if [[ -e "$file" ]]; then
            print_success "$file exists"
        else
            print_error "$file is missing"
        fi
    done
}

check_environment_files() {
    print_header "Environment Configuration"
    
    # Check backend application properties
    if [[ -f "backend/src/main/resources/application.properties" ]]; then
        print_success "Backend application.properties exists"
    else
        print_warning "Backend application.properties not found"
    fi
    
    if [[ -f "backend/src/main/resources/application-dev.properties" ]]; then
        print_success "Backend dev profile properties exist"
    else
        print_warning "Backend dev profile properties not found"
    fi
    
    if [[ -f "backend/src/main/resources/application-prod.yml" ]]; then
        print_success "Backend prod profile properties exist"
    else
        print_warning "Backend prod profile properties not found"
    fi
    
    # Check frontend environment files
    local frontend_env_files=(".env" ".env.development" ".env.production" ".env.test")
    
    for env_file in "${frontend_env_files[@]}"; do
        if [[ -f "frontend/$env_file" ]]; then
            print_success "Frontend $env_file exists"
        else
            print_warning "Frontend $env_file not found"
        fi
    done
}

# Main validation function
main() {
    print_header "Environment Validation for Bookstore Application"
    
    local errors=0
    
    # Core development tools
    print_header "Core Development Tools"
    check_java || ((errors++))
    check_maven || ((errors++))
    check_node || ((errors++))
    check_npm || ((errors++))
    check_git || true  # Git is recommended but not required
    
    # Optional tools
    print_header "Optional Tools"
    check_docker || true
    
    # Database tools
    check_database_tools || true
    
    # Port availability
    check_ports || true
    
    # Project structure
    check_project_structure || ((errors++))
    
    # Environment configuration
    check_environment_files || true
    
    # Summary
    print_header "Validation Summary"
    
    if [[ $errors -eq 0 ]]; then
        print_success "Environment validation completed successfully!"
        print_info "Your environment is ready for development and building"
    else
        print_error "Environment validation completed with $errors error(s)"
        print_info "Please install missing dependencies before proceeding"
        exit 1
    fi
    
    print_info "To get started:"
    echo "  • Development: npm run dev"
    echo "  • Build production: npm run build"
    echo "  • Run tests: npm run test"
    echo "  • View all commands: npm run"
}

# Run validation
main "$@"