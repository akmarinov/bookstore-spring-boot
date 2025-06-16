#!/bin/sh

# Health check script for React frontend served by nginx
# This script is used by Docker HEALTHCHECK instruction

set -e

# Configuration
HEALTH_URL="http://localhost/health"
INDEX_URL="http://localhost/"
TIMEOUT=10
MAX_RETRIES=3

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [FRONTEND-HEALTH] $1"
}

# Function to check nginx health endpoint
check_health_endpoint() {
    local retry_count=0
    
    while [ $retry_count -lt $MAX_RETRIES ]; do
        log "Checking health endpoint: $HEALTH_URL (attempt $((retry_count + 1))/$MAX_RETRIES)"
        
        if curl -f -s --max-time $TIMEOUT "$HEALTH_URL" | grep -q "healthy"; then
            log "${GREEN}Health endpoint check passed${NC}"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $MAX_RETRIES ]; then
                log "${YELLOW}Health endpoint check failed, retrying in 2 seconds...${NC}"
                sleep 2
            fi
        fi
    done
    
    log "${RED}Health endpoint check failed after $MAX_RETRIES attempts${NC}"
    return 1
}

# Function to check if the main application loads
check_app_loads() {
    local retry_count=0
    
    while [ $retry_count -lt $MAX_RETRIES ]; do
        log "Checking if app loads: $INDEX_URL (attempt $((retry_count + 1))/$MAX_RETRIES)"
        
        if curl -f -s --max-time $TIMEOUT "$INDEX_URL" | grep -q "<!DOCTYPE html>"; then
            log "${GREEN}App loading check passed${NC}"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $MAX_RETRIES ]; then
                log "${YELLOW}App loading check failed, retrying in 2 seconds...${NC}"
                sleep 2
            fi
        fi
    done
    
    log "${RED}App loading check failed after $MAX_RETRIES attempts${NC}"
    return 1
}

# Function to check if nginx process is running
check_nginx_process() {
    if pgrep nginx > /dev/null; then
        log "Nginx process is running"
        return 0
    else
        log "${RED}Nginx process is not running${NC}"
        return 1
    fi
}

# Function to check if nginx is listening on port 80
check_nginx_port() {
    if netstat -ln | grep -q ":80 "; then
        log "Nginx is listening on port 80"
        return 0
    else
        log "${RED}Nginx is not listening on port 80${NC}"
        return 1
    fi
}

# Main health check logic
main() {
    log "Starting frontend health check..."
    
    # Check if nginx process is running
    if ! check_nginx_process; then
        exit 1
    fi
    
    # Check if nginx is listening on the correct port
    if ! check_nginx_port; then
        exit 1
    fi
    
    # Check the health endpoint
    if ! check_health_endpoint; then
        exit 1
    fi
    
    # Check if the main app loads
    if ! check_app_loads; then
        exit 1
    fi
    
    log "${GREEN}All frontend health checks passed${NC}"
    exit 0
}

# Run the main function
main "$@"