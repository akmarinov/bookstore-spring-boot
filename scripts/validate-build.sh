#!/bin/bash

# Build Validation Script
# Validates build artifacts and ensures they meet production requirements

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

# Configuration
DIST_DIR="${1:-dist}"
MIN_JAR_SIZE_MB=20
MAX_JAR_SIZE_MB=200
MIN_FRONTEND_FILES=10

# Validation functions
validate_directory_structure() {
    print_header "Validating Directory Structure"
    
    local required_dirs=(
        "$DIST_DIR"
        "$DIST_DIR/backend"
        "$DIST_DIR/frontend"
        "$DIST_DIR/scripts"
    )
    
    local required_files=(
        "$DIST_DIR/build-info.json"
        "$DIST_DIR/scripts/start-backend.sh"
        "$DIST_DIR/scripts/stop-backend.sh"
        "$DIST_DIR/scripts/health-check.sh"
    )
    
    local errors=0
    
    # Check directories
    for dir in "${required_dirs[@]}"; do
        if [[ -d "$dir" ]]; then
            print_success "Directory exists: $dir"
        else
            print_error "Missing directory: $dir"
            ((errors++))
        fi
    done
    
    # Check files
    for file in "${required_files[@]}"; do
        if [[ -f "$file" ]]; then
            print_success "File exists: $file"
        else
            print_error "Missing file: $file"
            ((errors++))
        fi
    done
    
    return $errors
}

validate_backend_jar() {
    print_header "Validating Backend JAR"
    
    local jar_file=$(find "$DIST_DIR/backend" -name "*.jar" -type f | head -n1)
    local errors=0
    
    if [[ -z "$jar_file" ]]; then
        print_error "No JAR file found in $DIST_DIR/backend"
        return 1
    fi
    
    print_success "JAR file found: $(basename "$jar_file")"
    
    # Check file size
    local jar_size_bytes=$(stat -f%z "$jar_file" 2>/dev/null || stat -c%s "$jar_file" 2>/dev/null)
    local jar_size_mb=$((jar_size_bytes / 1024 / 1024))
    
    print_info "JAR size: ${jar_size_mb}MB"
    
    if [[ $jar_size_mb -lt $MIN_JAR_SIZE_MB ]]; then
        print_warning "JAR size (${jar_size_mb}MB) is smaller than expected minimum (${MIN_JAR_SIZE_MB}MB)"
        ((errors++))
    elif [[ $jar_size_mb -gt $MAX_JAR_SIZE_MB ]]; then
        print_warning "JAR size (${jar_size_mb}MB) is larger than expected maximum (${MAX_JAR_SIZE_MB}MB)"
    else
        print_success "JAR size is within acceptable range"
    fi
    
    # Check if JAR is executable
    if java -jar "$jar_file" --help &>/dev/null; then
        print_success "JAR is executable"
    else
        print_error "JAR is not executable or missing dependencies"
        ((errors++))
    fi
    
    # Check manifest
    if jar tf "$jar_file" | grep -q "META-INF/MANIFEST.MF"; then
        print_success "JAR manifest present"
        
        # Extract and check main class
        local main_class=$(jar xf "$jar_file" META-INF/MANIFEST.MF -O 2>/dev/null | grep "Main-Class:" | cut -d' ' -f2- | tr -d '\r\n')
        if [[ -n "$main_class" ]]; then
            print_success "Main class defined: $main_class"
        else
            print_warning "Main class not found in manifest"
        fi
    else
        print_error "JAR manifest missing"
        ((errors++))
    fi
    
    # Check if Spring Boot classes are present
    if jar tf "$jar_file" | grep -q "org/springframework/boot"; then
        print_success "Spring Boot classes found in JAR"
    else
        print_error "Spring Boot classes not found in JAR"
        ((errors++))
    fi
    
    # Check for application properties
    if jar tf "$jar_file" | grep -q "application"; then
        print_success "Application configuration files found"
    else
        print_warning "No application configuration files found in JAR"
    fi
    
    return $errors
}

validate_frontend_build() {
    print_header "Validating Frontend Build"
    
    local errors=0
    
    # Check if index.html exists
    if [[ -f "$DIST_DIR/frontend/index.html" ]]; then
        print_success "Frontend index.html found"
    else
        print_error "Frontend index.html not found"
        ((errors++))
        return $errors
    fi
    
    # Check file count
    local file_count=$(find "$DIST_DIR/frontend" -type f | wc -l)
    print_info "Frontend files count: $file_count"
    
    if [[ $file_count -lt $MIN_FRONTEND_FILES ]]; then
        print_warning "Frontend build has fewer files than expected (minimum: $MIN_FRONTEND_FILES)"
        ((errors++))
    else
        print_success "Frontend build has adequate number of files"
    fi
    
    # Check for assets directory
    if [[ -d "$DIST_DIR/frontend/assets" ]]; then
        print_success "Frontend assets directory found"
        
        # Check for CSS files
        local css_count=$(find "$DIST_DIR/frontend/assets" -name "*.css" | wc -l)
        if [[ $css_count -gt 0 ]]; then
            print_success "CSS files found ($css_count files)"
        else
            print_warning "No CSS files found in assets"
        fi
        
        # Check for JS files
        local js_count=$(find "$DIST_DIR/frontend/assets" -name "*.js" | wc -l)
        if [[ $js_count -gt 0 ]]; then
            print_success "JavaScript files found ($js_count files)"
        else
            print_error "No JavaScript files found in assets"
            ((errors++))
        fi
        
    else
        print_warning "Frontend assets directory not found"
    fi
    
    # Validate index.html content
    if grep -q "<!doctype html>" "$DIST_DIR/frontend/index.html" 2>/dev/null; then
        print_success "Valid HTML5 doctype found"
    else
        print_warning "HTML5 doctype not found"
    fi
    
    # Check for script tags
    if grep -q "<script" "$DIST_DIR/frontend/index.html"; then
        print_success "Script tags found in index.html"
    else
        print_warning "No script tags found in index.html"
    fi
    
    # Check for CSS links
    if grep -q "stylesheet" "$DIST_DIR/frontend/index.html"; then
        print_success "CSS links found in index.html"
    else
        print_warning "No CSS links found in index.html"
    fi
    
    return $errors
}

validate_scripts() {
    print_header "Validating Scripts"
    
    local scripts=(
        "$DIST_DIR/scripts/start-backend.sh"
        "$DIST_DIR/scripts/stop-backend.sh"
        "$DIST_DIR/scripts/health-check.sh"
    )
    
    local errors=0
    
    for script in "${scripts[@]}"; do
        if [[ -f "$script" ]]; then
            # Check if executable
            if [[ -x "$script" ]]; then
                print_success "Script is executable: $(basename "$script")"
            else
                print_warning "Script is not executable: $(basename "$script")"
                chmod +x "$script"
                print_info "Made script executable: $(basename "$script")"
            fi
            
            # Basic syntax check
            if bash -n "$script" 2>/dev/null; then
                print_success "Script syntax is valid: $(basename "$script")"
            else
                print_error "Script syntax error: $(basename "$script")"
                ((errors++))
            fi
        else
            print_error "Script not found: $(basename "$script")"
            ((errors++))
        fi
    done
    
    return $errors
}

validate_build_metadata() {
    print_header "Validating Build Metadata"
    
    local errors=0
    local build_info="$DIST_DIR/build-info.json"
    
    if [[ ! -f "$build_info" ]]; then
        print_error "Build info file not found: $build_info"
        return 1
    fi
    
    # Check if valid JSON
    if jq empty "$build_info" 2>/dev/null; then
        print_success "Build info is valid JSON"
    else
        print_error "Build info is not valid JSON"
        ((errors++))
        return $errors
    fi
    
    # Check required fields
    local required_fields=(
        "buildDate"
        "buildEnvironment"
        "gitCommit"
        "versions"
    )
    
    for field in "${required_fields[@]}"; do
        if jq -e ".$field" "$build_info" >/dev/null 2>&1; then
            local value=$(jq -r ".$field" "$build_info")
            print_success "Field '$field' present: $value"
        else
            print_warning "Field '$field' missing from build info"
        fi
    done
    
    # Validate build date format
    local build_date=$(jq -r ".buildDate" "$build_info" 2>/dev/null)
    if [[ "$build_date" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$ ]]; then
        print_success "Build date format is valid: $build_date"
    else
        print_warning "Build date format may be invalid: $build_date"
    fi
    
    return $errors
}

validate_security() {
    print_header "Security Validation"
    
    local errors=0
    
    # Check for sensitive files that shouldn't be in dist
    local sensitive_patterns=(
        "*.key"
        "*.pem"
        "*password*"
        "*secret*"
        "*.env"
    )
    
    for pattern in "${sensitive_patterns[@]}"; do
        local found_files=$(find "$DIST_DIR" -name "$pattern" -type f 2>/dev/null || true)
        if [[ -n "$found_files" ]]; then
            print_warning "Potentially sensitive files found: $found_files"
        fi
    done
    
    # Check file permissions
    local executable_files=$(find "$DIST_DIR" -type f -perm /111 | grep -v ".sh$" | grep -v ".jar$" || true)
    if [[ -n "$executable_files" ]]; then
        print_warning "Unexpected executable files found:"
        echo "$executable_files"
    else
        print_success "No unexpected executable files found"
    fi
    
    return $errors
}

# Main validation function
main() {
    print_header "Build Validation for Bookstore Application"
    
    if [[ ! -d "$DIST_DIR" ]]; then
        print_error "Distribution directory not found: $DIST_DIR"
        print_info "Run the build script first: ./build.sh"
        exit 1
    fi
    
    local total_errors=0
    
    # Run all validations
    validate_directory_structure || ((total_errors += $?))
    validate_backend_jar || ((total_errors += $?))
    validate_frontend_build || ((total_errors += $?))
    validate_scripts || ((total_errors += $?))
    validate_build_metadata || ((total_errors += $?))
    validate_security || ((total_errors += $?))
    
    # Summary
    print_header "Validation Summary"
    
    if [[ $total_errors -eq 0 ]]; then
        print_success "All validations passed successfully!"
        print_info "Build is ready for deployment"
        exit 0
    else
        print_error "Validation completed with $total_errors error(s)"
        print_info "Please fix the issues before deploying"
        exit 1
    fi
}

# Show help
show_help() {
    echo "Build Validation Script"
    echo ""
    echo "Usage: $0 [DIST_DIR]"
    echo ""
    echo "Arguments:"
    echo "  DIST_DIR    Path to distribution directory (default: dist)"
    echo ""
    echo "Examples:"
    echo "  $0          # Validate default dist directory"
    echo "  $0 build    # Validate build directory"
}

# Parse arguments
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_help
    exit 0
fi

# Run main function
main "$@"