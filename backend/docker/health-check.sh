#!/bin/sh

# Health check script for Spring Boot backend
# This script is used by Docker HEALTHCHECK instruction

set -e

# Configuration
HEALTH_URL="http://localhost:${SERVER_PORT:-8080}/actuator/health"
TIMEOUT=10
MAX_RETRIES=3

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [HEALTH-CHECK] $1"
}

# Function to check application health
check_health() {
    local retry_count=0
    
    while [ $retry_count -lt $MAX_RETRIES ]; do
        log "Checking health endpoint: $HEALTH_URL (attempt $((retry_count + 1))/$MAX_RETRIES)"
        
        # Use curl to check the health endpoint
        if curl -f -s --max-time $TIMEOUT "$HEALTH_URL" > /dev/null 2>&1; then
            log "${GREEN}Health check passed${NC}"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $MAX_RETRIES ]; then
                log "${YELLOW}Health check failed, retrying in 2 seconds...${NC}"
                sleep 2
            fi
        fi
    done
    
    log "${RED}Health check failed after $MAX_RETRIES attempts${NC}"
    return 1
}

# Function to check if the application process is running
check_process() {
    if pgrep -f "java.*app.jar" > /dev/null; then
        log "Application process is running"
        return 0
    else
        log "${RED}Application process is not running${NC}"
        return 1
    fi
}

# Main health check logic
main() {
    log "Starting health check..."
    
    # First check if the process is running
    if ! check_process; then
        exit 1
    fi
    
    # Then check the health endpoint
    if check_health; then
        log "${GREEN}All health checks passed${NC}"
        exit 0
    else
        log "${RED}Health check failed${NC}"
        exit 1
    fi
}

# Run the main function
main "$@"