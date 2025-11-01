#!/bin/bash
# Start Campus Marketplace with PostgreSQL Database (Production Profile)
# The spring-dotenv library automatically loads environment variables from .env

# Check if .env file exists
if [ ! -f .env ]; then
    echo "========================================="
    echo "ERROR: .env file not found!"
    echo "========================================="
    echo "Please create a .env file from the template:"
    echo "  cp env.example .env"
    echo "Then update the values with your credentials."
    echo "========================================="
    exit 1
fi

# Display configuration info
echo "========================================="
echo "Campus Marketplace - PostgreSQL Mode"
echo "========================================="
echo "Profile: prod"
echo "Environment: Loading from .env file"
echo "Database: PostgreSQL"
echo "========================================="
echo ""

# Start the application with production profile
# The spring-dotenv library will automatically load .env
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
