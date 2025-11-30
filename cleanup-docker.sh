#!/bin/bash

# ==============================================
# Docker Cleanup Script
# Stops all containers and removes all images
# ==============================================

set -e

echo "=========================================="
echo "Docker Cleanup Script"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Step 1: Stop all running containers
print_info "Stopping all running containers..."
if [ "$(docker ps -q)" ]; then
    docker stop $(docker ps -q)
    print_info "All containers stopped"
else
    print_info "No running containers found"
fi

# Step 2: Remove all containers
print_info "Removing all containers..."
if [ "$(docker ps -aq)" ]; then
    docker rm $(docker ps -aq)
    print_info "All containers removed"
else
    print_info "No containers found"
fi

# Step 3: Remove all images
print_info "Removing all Docker images..."
if [ "$(docker images -q)" ]; then
    docker rmi $(docker images -q) -f 2>/dev/null || true
    print_info "All images removed"
else
    print_info "No images found"
fi

# Step 4: Optional - Clean up volumes (commented out by default)
# Uncomment the following lines if you also want to remove volumes
# print_warning "Removing all unused volumes..."
# docker volume prune -f

# Step 5: Optional - Clean up networks (commented out by default)
# Uncomment the following lines if you also want to remove custom networks
# print_warning "Removing unused networks..."
# docker network prune -f

# Step 6: Optional - System prune (commented out by default)
# Uncomment the following line for a complete cleanup (removes everything unused)
# print_warning "Running docker system prune -a..."
# docker system prune -a -f --volumes

echo ""
echo "=========================================="
print_info "Cleanup completed!"
echo "=========================================="
echo ""
print_info "Remaining Docker resources:"
echo "  Containers: $(docker ps -aq | wc -l | tr -d ' ')"
echo "  Images: $(docker images -q | wc -l | tr -d ' ')"
echo "  Volumes: $(docker volume ls -q | wc -l | tr -d ' ')"
echo ""

