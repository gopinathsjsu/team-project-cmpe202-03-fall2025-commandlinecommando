#!/bin/bash
# ==============================================
# Campus Marketplace EC2 Deployment Script
# ==============================================
# This script helps deploy the application to AWS EC2
# with Docker and ALB support.
#
# Prerequisites:
# - Docker and Docker Compose installed on EC2
# - .env file with all required environment variables (defaults are set in env.example)
#
# Usage:
#   ./aws-deploy-ec2.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}===============================================${NC}"
echo -e "${GREEN}Campus Marketplace Deployment Script${NC}"
echo -e "${GREEN}===============================================${NC}"
echo ""

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}Error: .env file not found!${NC}"
    echo -e "${YELLOW}Copy env.example to .env and configure your environment variables.${NC}"
    exit 1
fi

# Load environment variables
export $(grep -v '^#' .env | xargs)

# Function to check required environment variables
check_env_vars() {
    local required_vars=("DB_APP_USER" "DB_APP_PASSWORD" "JWT_SECRET")
    local missing_vars=()

    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            missing_vars+=("$var")
        fi
    done

    if [ ${#missing_vars[@]} -ne 0 ]; then
        echo -e "${RED}Error: Missing required environment variables:${NC}"
        for var in "${missing_vars[@]}"; do
            echo -e "${RED}  - $var${NC}"
        done
        exit 1
    fi
}

# Function to stop existing containers
stop_containers() {
    echo -e "${YELLOW}Stopping existing containers...${NC}"
    docker-compose -f docker-compose.prod.yml down 2>/dev/null || true
}

# Function to pull/build images
build_images() {
    echo -e "${YELLOW}Building Docker images...${NC}"
    
    # Build all services (AI service included by default)
    docker-compose -f docker-compose.prod.yml build
}

# Function to start containers
start_containers() {
    echo -e "${YELLOW}Starting containers...${NC}"
    
    # Start all services (AI service included by default)
    docker-compose -f docker-compose.prod.yml up -d
}

# Function to wait for services to be healthy
wait_for_health() {
    echo -e "${YELLOW}Waiting for services to become healthy...${NC}"
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        echo -e "  Attempt $attempt/$max_attempts..."
        
        # Check backend health
        if curl -sf http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}Backend is healthy!${NC}"
            break
        fi
        
        sleep 5
        ((attempt++))
    done
    
    if [ $attempt -gt $max_attempts ]; then
        echo -e "${RED}Services did not become healthy in time.${NC}"
        echo -e "${YELLOW}Check logs with: docker-compose -f docker-compose.prod.yml logs${NC}"
        exit 1
    fi
}

# Function to show status
show_status() {
    echo ""
    echo -e "${GREEN}===============================================${NC}"
    echo -e "${GREEN}Deployment Complete!${NC}"
    echo -e "${GREEN}===============================================${NC}"
    echo ""
    echo -e "${YELLOW}Services:${NC}"
    docker-compose -f docker-compose.prod.yml ps
    echo ""
    echo -e "${YELLOW}Endpoints:${NC}"
    echo -e "  Frontend:  http://localhost:80"
    echo -e "  Backend:   http://localhost:8080/api"
    echo -e "  Health:    http://localhost:8080/api/actuator/health"
    echo -e "  AI Service: http://localhost:3001/api"
    echo ""
    echo -e "${YELLOW}Useful Commands:${NC}"
    echo -e "  View logs:     docker-compose -f docker-compose.prod.yml logs -f"
    echo -e "  Stop services: docker-compose -f docker-compose.prod.yml down"
    echo -e "  Restart:       docker-compose -f docker-compose.prod.yml restart"
    echo ""
}

# Main execution
echo -e "${YELLOW}Starting all services (AI service included by default)${NC}"
echo ""

check_env_vars
stop_containers
build_images
start_containers
wait_for_health
show_status
