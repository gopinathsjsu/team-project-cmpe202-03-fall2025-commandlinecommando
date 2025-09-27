#!/bin/bash
# ==============================================
# Campus Marketplace Database Backup Script
# ==============================================
# This script performs automated backups of the PostgreSQL database
# with configurable retention policies and optional remote storage.

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BACKUP_DIR="$PROJECT_ROOT/db/backups"

# Load environment variables
if [ -f "$PROJECT_ROOT/.env" ]; then
    export $(grep -v '^#' "$PROJECT_ROOT/.env" | xargs)
fi

# Default configuration
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-campus_marketplace}
DB_APP_USER=${DB_APP_USER:-cm_app_user}
DB_APP_PASSWORD=${DB_APP_PASSWORD}
BACKUP_RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-7}
REMOTE_BACKUP_RETENTION_DAYS=${REMOTE_BACKUP_RETENTION_DAYS:-30}

# Backup configuration
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DATE_FOLDER=$(date +"%Y/%m/%d")
BACKUP_NAME="campus_marketplace_${TIMESTAMP}"
LOCAL_BACKUP_DIR="$BACKUP_DIR/local/$DATE_FOLDER"
REMOTE_BACKUP_DIR="$BACKUP_DIR/remote"

# Logging
LOG_FILE="$BACKUP_DIR/backup.log"
ERROR_LOG="$BACKUP_DIR/backup_errors.log"

# Functions
log() {
    local level=$1
    shift
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [$level] $*" | tee -a "$LOG_FILE"
}

error() {
    log "ERROR" "$*" | tee -a "$ERROR_LOG"
}

info() {
    log "INFO" "$*"
}

warn() {
    log "WARN" "$*"
}

# Check dependencies
check_dependencies() {
    info "Checking dependencies..."
    
    if ! command -v pg_dump &> /dev/null; then
        error "pg_dump could not be found. Please install PostgreSQL client tools."
        exit 1
    fi
    
    if ! command -v gzip &> /dev/null; then
        error "gzip could not be found."
        exit 1
    fi
    
    info "Dependencies check passed."
}

# Test database connection
test_connection() {
    info "Testing database connection..."
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    if ! pg_isready -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" &> /dev/null; then
        error "Cannot connect to database $DB_NAME on $DB_HOST:$DB_PORT"
        exit 1
    fi
    
    info "Database connection test passed."
}

# Create backup directories
create_backup_dirs() {
    info "Creating backup directories..."
    mkdir -p "$LOCAL_BACKUP_DIR"
    mkdir -p "$REMOTE_BACKUP_DIR"
    mkdir -p "$(dirname "$LOG_FILE")"
}

# Perform database backup
backup_database() {
    info "Starting database backup..."
    
    local sql_file="$LOCAL_BACKUP_DIR/${BACKUP_NAME}.sql"
    local compressed_file="$LOCAL_BACKUP_DIR/${BACKUP_NAME}.sql.gz"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    # Create SQL dump
    if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" \
               --verbose --clean --no-owner --no-privileges \
               --format=plain > "$sql_file" 2>> "$ERROR_LOG"; then
        info "SQL dump created successfully: $sql_file"
    else
        error "Failed to create SQL dump"
        return 1
    fi
    
    # Compress the backup
    if gzip "$sql_file"; then
        info "Backup compressed successfully: $compressed_file"
        
        # Get file size for logging
        local file_size=$(du -h "$compressed_file" | cut -f1)
        info "Backup size: $file_size"
        
        # Generate checksum for integrity verification
        local checksum=$(md5 -q "$compressed_file")
        echo "$checksum  $(basename "$compressed_file")" > "$compressed_file.md5"
        info "Checksum generated: $checksum"
        
    else
        error "Failed to compress backup"
        return 1
    fi
    
    info "Database backup completed successfully."
    return 0
}

# Backup metadata (schema only)
backup_schema() {
    info "Creating schema-only backup..."
    
    local schema_file="$LOCAL_BACKUP_DIR/${BACKUP_NAME}_schema.sql"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" \
               --schema-only --verbose --clean --no-owner --no-privileges \
               --format=plain > "$schema_file" 2>> "$ERROR_LOG"; then
        info "Schema backup created successfully: $schema_file"
        gzip "$schema_file"
        info "Schema backup compressed: ${schema_file}.gz"
    else
        error "Failed to create schema backup"
        return 1
    fi
    
    return 0
}

# Clean old local backups
cleanup_local_backups() {
    info "Cleaning up old local backups (older than $BACKUP_RETENTION_DAYS days)..."
    
    local deleted_count=0
    
    # Find and delete old backup files
    if find "$BACKUP_DIR/local" -name "*.sql.gz" -mtime +$BACKUP_RETENTION_DAYS -type f | while read -r file; do
        if [ -f "$file" ]; then
            rm -f "$file" "$file.md5"
            info "Deleted old backup: $(basename "$file")"
            ((deleted_count++))
        fi
    done; then
        info "Cleanup completed. Deleted $deleted_count old backup files."
    else
        warn "No old backups found to clean up."
    fi
    
    # Clean up empty directories
    find "$BACKUP_DIR/local" -type d -empty -delete 2>/dev/null || true
}

# Upload to remote storage (if configured)
upload_to_remote() {
    if [ -z "$AWS_ACCESS_KEY_ID" ] || [ -z "$BACKUP_S3_BUCKET" ]; then
        info "Remote backup not configured. Skipping upload."
        return 0
    fi
    
    info "Uploading backup to remote storage..."
    
    local compressed_file="$LOCAL_BACKUP_DIR/${BACKUP_NAME}.sql.gz"
    local s3_path="s3://$BACKUP_S3_BUCKET/campus-marketplace/$DATE_FOLDER/"
    
    if command -v aws &> /dev/null; then
        if aws s3 cp "$compressed_file" "$s3_path" --region "$AWS_REGION" 2>> "$ERROR_LOG"; then
            info "Backup uploaded to S3 successfully"
            
            # Upload checksum file
            aws s3 cp "$compressed_file.md5" "$s3_path" --region "$AWS_REGION" 2>> "$ERROR_LOG"
            
            # Copy to remote backup directory for local tracking
            cp "$compressed_file" "$compressed_file.md5" "$REMOTE_BACKUP_DIR/"
            
        else
            error "Failed to upload backup to S3"
            return 1
        fi
    else
        warn "AWS CLI not installed. Remote backup skipped."
        return 0
    fi
}

# Clean old remote backups
cleanup_remote_backups() {
    if [ -z "$AWS_ACCESS_KEY_ID" ] || [ -z "$BACKUP_S3_BUCKET" ]; then
        return 0
    fi
    
    info "Cleaning up old remote backups (older than $REMOTE_BACKUP_RETENTION_DAYS days)..."
    
    if command -v aws &> /dev/null; then
        local cutoff_date=$(date -v-${REMOTE_BACKUP_RETENTION_DAYS}d +%Y-%m-%d 2>/dev/null || date -d "$REMOTE_BACKUP_RETENTION_DAYS days ago" +%Y-%m-%d)
        
        # Note: This is a simplified cleanup. In practice, you'd want to use S3 lifecycle policies
        info "Remote cleanup requires S3 lifecycle policies for optimal management."
        info "Consider setting up lifecycle rules in your S3 bucket for automatic cleanup."
    fi
}

# Generate backup report
generate_report() {
    info "Generating backup report..."
    
    local report_file="$BACKUP_DIR/backup_report_$(date +%Y%m%d).txt"
    
    cat > "$report_file" << EOF
Campus Marketplace Database Backup Report
==========================================
Date: $(date)
Backup Name: $BACKUP_NAME
Database: $DB_NAME
Host: $DB_HOST:$DB_PORT

Backup Details:
- Backup Directory: $LOCAL_BACKUP_DIR
- Full Backup: ${BACKUP_NAME}.sql.gz
- Schema Backup: ${BACKUP_NAME}_schema.sql.gz

Retention Policy:
- Local Retention: $BACKUP_RETENTION_DAYS days
- Remote Retention: $REMOTE_BACKUP_RETENTION_DAYS days

Status: SUCCESS
EOF

    info "Backup report generated: $report_file"
}

# Handle errors
handle_error() {
    local exit_code=$1
    error "Backup script failed with exit code: $exit_code"
    
    # Send notification (if configured)
    if command -v mail &> /dev/null && [ -n "$ADMIN_EMAIL" ]; then
        echo "Campus Marketplace database backup failed. Check logs at $LOG_FILE" | \
        mail -s "Database Backup Failed" "$ADMIN_EMAIL"
    fi
    
    exit $exit_code
}

# Main execution
main() {
    info "Starting Campus Marketplace database backup process..."
    
    # Trap errors
    trap 'handle_error $?' ERR
    
    check_dependencies
    test_connection
    create_backup_dirs
    
    # Perform backups
    if backup_database && backup_schema; then
        info "All backups completed successfully"
    else
        error "One or more backup operations failed"
        exit 1
    fi
    
    # Upload and cleanup
    upload_to_remote
    cleanup_local_backups
    cleanup_remote_backups
    
    # Generate report
    generate_report
    
    info "Backup process completed successfully!"
}

# Command line options
case "${1:-}" in
    --help|-h)
        echo "Usage: $0 [--help|--test|--cleanup-only]"
        echo "  --help        Show this help message"
        echo "  --test        Test database connection only"
        echo "  --cleanup-only Cleanup old backups only"
        exit 0
        ;;
    --test)
        check_dependencies
        test_connection
        info "Database connection test completed successfully"
        exit 0
        ;;
    --cleanup-only)
        cleanup_local_backups
        cleanup_remote_backups
        info "Cleanup completed"
        exit 0
        ;;
    *)
        main
        ;;
esac