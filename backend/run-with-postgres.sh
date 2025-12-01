#!/bin/bash
# Start Campus Marketplace with PostgreSQL Database (Production Profile)
# The spring-dotenv library automatically loads environment variables from .env

# Load .env file if it exists
if [ -f .env ]; then
    echo "Loading environment from .env file..."
    export $(grep -v '^#' .env | xargs)
fi

# Check required AWS S3 environment variables
missing_vars=()

if [ -z "$AWS_S3_BUCKET_NAME" ]; then
    missing_vars+=("AWS_S3_BUCKET_NAME")
fi

if [ -z "$AWS_REGION" ]; then
    missing_vars+=("AWS_REGION")
fi

if [ -z "$AWS_ACCESS_KEY_ID" ]; then
    missing_vars+=("AWS_ACCESS_KEY_ID")
fi

if [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    missing_vars+=("AWS_SECRET_ACCESS_KEY")
fi

# Exit if any required variables are missing
if [ ${#missing_vars[@]} -ne 0 ]; then
    echo "========================================="
    echo "ERROR: Missing required environment variables!"
    echo "========================================="
    for var in "${missing_vars[@]}"; do
        echo "  - $var"
    done
    echo ""
    echo "Please set these variables either:"
    echo "  1. Create a .env file with these values"
    echo "  2. Export them in your shell before running"
    echo ""
    echo "Example .env file:"
    echo "  AWS_S3_BUCKET_NAME=your-bucket-name"
    echo "  AWS_REGION=us-west-1"
    echo "  AWS_ACCESS_KEY_ID=your-access-key"
    echo "  AWS_SECRET_ACCESS_KEY=your-secret-key"
    echo "========================================="
    exit 1
fi

# Check email configuration (optional but recommended)
if [ -z "$SMTP_PASSWORD" ]; then
    email_status="⚠️  Not configured (set SMTP_PASSWORD for email notifications)"
else
    email_status="✅ Configured (SendGrid)"
fi

# Display configuration info
echo "========================================="
echo "Campus Marketplace - PostgreSQL Mode"
echo "========================================="
echo "Profile: postgres"
echo "Database: PostgreSQL"
echo "S3 Bucket: $AWS_S3_BUCKET_NAME"
echo "AWS Region: $AWS_REGION"
echo "Email: $email_status"
echo "========================================="
echo ""

# Start the application with postgres profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
