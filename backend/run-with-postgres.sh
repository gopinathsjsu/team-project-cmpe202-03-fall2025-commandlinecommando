#!/bin/bash
# Start Campus Marketplace with PostgreSQL Database (Production Profile)
# The spring-dotenv library automatically loads environment variables from .env

# AWS S3 Configuration (set defaults if not already set)
export AWS_S3_BUCKET_NAME=${AWS_S3_BUCKET_NAME:-webapp-s3-bucket-2025}
export AWS_REGION=${AWS_REGION:-us-west-1}
export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID:-AKIAZPTIR2WZ33YQAD63}
export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY:-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}

# Check if .env file exists (optional now, we have defaults above)
if [ ! -f .env ]; then
    echo "========================================="
    echo "WARNING: .env file not found!"
    echo "========================================="
    echo "Using default environment variables."
    echo "For custom settings, create a .env file:"
    echo "  cp env.example .env"
    echo "========================================="
fi

# Display configuration info
echo "========================================="
echo "Campus Marketplace - PostgreSQL Mode"
echo "========================================="
echo "Profile: postgres"
echo "Environment: Loading from .env file (if exists)"
echo "Database: PostgreSQL"
echo "S3 Bucket: $AWS_S3_BUCKET_NAME"
echo "AWS Region: $AWS_REGION"
echo "========================================="
echo ""

# Start the application with postgres profile
# The spring-dotenv library will automatically load .env if present
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
