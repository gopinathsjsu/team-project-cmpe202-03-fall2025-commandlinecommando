#!/bin/bash
# ==============================================
# Campus Marketplace Database Restore Script
# ==============================================
# This script restores the PostgreSQL database from backup files

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

# Logging
LOG_FILE="$BACKUP_DIR/restore.log"

# Functions
log() {
    local level=$1
    shift
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [$level] $*" | tee -a "$LOG_FILE"
}

info() {
    log "INFO" "$*"
}

error() {
    log "ERROR" "$*"
}

warn() {
    log "WARN" "$*"
}

# List available backups
list_backups() {
    info "Available backup files:"
    echo
    find "$BACKUP_DIR/local" -name "*.sql.gz" -type f | sort -r | head -20 | while read -r file; do
        local size=$(du -h "$file" | cut -f1)
        local date=$(stat -c %y "$file" 2>/dev/null || stat -f %Sm "$file" 2>/dev/null || echo "Unknown")
        printf "  %s (%s) - %s\n" "$(basename "$file")" "$size" "$date"
    done
    echo
}

# Verify backup integrity
verify_backup() {
    local backup_file=$1
    local checksum_file="${backup_file}.md5"
    
    if [ ! -f "$checksum_file" ]; then
        warn "Checksum file not found: $checksum_file"
        return 1
    fi
    
    info "Verifying backup integrity..."
    
    local backup_dir=$(dirname "$backup_file")
    local expected_checksum=$(cat "$checksum_file" | cut -d' ' -f1)
    local actual_checksum=$(md5 -q "$backup_file")
    
    if [ "$expected_checksum" = "$actual_checksum" ]; then
        info "Backup integrity verified successfully"
        return 0
    else
        error "Backup integrity check failed! File may be corrupted."
        error "Expected: $expected_checksum"
        error "Actual: $actual_checksum"
        return 1
    fi
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

# Create database backup before restore
create_pre_restore_backup() {
    info "Creating pre-restore backup for safety..."
    
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local pre_restore_dir="$BACKUP_DIR/pre-restore"
    mkdir -p "$pre_restore_dir"
    
    local backup_file="$pre_restore_dir/pre_restore_${timestamp}.sql"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" \
               --verbose --clean --no-owner --no-privileges \
               --format=plain > "$backup_file" 2>> "$LOG_FILE"; then
        gzip "$backup_file"
        info "Pre-restore backup created: ${backup_file}.gz"
        return 0
    else
        error "Failed to create pre-restore backup"
        return 1
    fi
}

# Restore database
restore_database() {
    local backup_file=$1
    
    if [ ! -f "$backup_file" ]; then
        error "Backup file not found: $backup_file"
        return 1
    fi
    
    info "Restoring database from: $(basename "$backup_file")"
    
    # Verify backup integrity
    if ! verify_backup "$backup_file"; then
        error "Backup verification failed. Aborting restore."
        return 1
    fi
    
    # Create pre-restore backup
    if ! create_pre_restore_backup; then
        warn "Failed to create pre-restore backup, but continuing..."
    fi
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    # Restore from compressed backup
    if gunzip -c "$backup_file" | psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" \
                                       --quiet --single-transaction 2>> "$LOG_FILE"; then
        info "Database restore completed successfully"
        return 0
    else
        error "Database restore failed"
        return 1
    fi
}

# Interactive backup selection
select_backup_interactive() {
    list_backups
    
    echo "Please enter the backup filename (without path):"
    read -r backup_name
    
    local backup_file=$(find "$BACKUP_DIR/local" -name "$backup_name" -type f | head -1)
    
    if [ -z "$backup_file" ]; then
        error "Backup file not found: $backup_name"
        exit 1
    fi
    
    echo "$backup_file"
}

# Restore latest backup
restore_latest() {
    local latest_backup=$(find "$BACKUP_DIR/local" -name "*.sql.gz" -type f | sort -r | head -1)
    
    if [ -z "$latest_backup" ]; then
        error "No backup files found"
        exit 1
    fi
    
    info "Latest backup found: $(basename "$latest_backup")"
    
    restore_database "$latest_backup"
}

# Show usage
show_usage() {
    cat << EOF
Campus Marketplace Database Restore Script
==========================================

Usage: $0 [OPTIONS]

Options:
  --help, -h              Show this help message
  --list, -l              List available backup files
  --latest                Restore from the latest backup
  --file FILE             Restore from specific backup file
  --interactive, -i       Interactive backup selection
  --test                  Test database connection only

Examples:
  $0 --latest                                    # Restore latest backup
  $0 --file campus_marketplace_20231201_120000.sql.gz  # Restore specific file
  $0 --interactive                               # Choose backup interactively
  $0 --list                                      # List available backups

EOF
}

# Main execution
main() {
    local backup_file=""
    local mode=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help|-h)
                show_usage
                exit 0
                ;;
            --list|-l)
                list_backups
                exit 0
                ;;
            --latest)
                mode="latest"
                shift
                ;;
            --file)
                backup_file="$2"
                mode="file"
                shift 2
                ;;
            --interactive|-i)
                mode="interactive"
                shift
                ;;
            --test)
                test_connection
                info "Database connection test completed successfully"
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Default to interactive mode if no options provided
    if [ -z "$mode" ]; then
        mode="interactive"
    fi
    
    info "Starting database restore process..."
    
    test_connection
    
    case "$mode" in
        latest)
            restore_latest
            ;;
        file)
            # Handle relative and absolute paths
            if [[ "$backup_file" == /* ]]; then
                restore_database "$backup_file"
            else
                local full_path=$(find "$BACKUP_DIR/local" -name "$backup_file" -type f | head -1)
                if [ -z "$full_path" ]; then
                    error "Backup file not found: $backup_file"
                    exit 1
                fi
                restore_database "$full_path"
            fi
            ;;
        interactive)
            backup_file=$(select_backup_interactive)
            restore_database "$backup_file"
            ;;
    esac
    
    info "Database restore process completed!"
}

# Run main function with all arguments
main "$@"