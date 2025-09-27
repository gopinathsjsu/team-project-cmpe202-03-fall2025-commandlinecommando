#!/bin/bash
# ==============================================
# Campus Marketplace Database Monitoring Script
# ==============================================
# This script provides database monitoring and health checks

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

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

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}=================================="
    echo -e "$1"
    echo -e "==================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Test database connection
test_connection() {
    print_header "Database Connection Test"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    if pg_isready -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" &> /dev/null; then
        print_success "Database connection successful"
        
        # Get database version
        local version=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" \
                           -t -c "SELECT version();" 2>/dev/null | head -1 | xargs)
        echo "Database Version: $version"
        
        return 0
    else
        print_error "Cannot connect to database $DB_NAME on $DB_HOST:$DB_PORT"
        return 1
    fi
}

# Check database size
check_database_size() {
    print_header "Database Size Information"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            pg_size_pretty(pg_database_size(current_database())) as \"Database Size\",
            (SELECT count(*) FROM pg_stat_user_tables) as \"User Tables\",
            (SELECT count(*) FROM pg_stat_user_indexes) as \"User Indexes\";
    " 2>/dev/null
}

# Show table sizes
show_table_sizes() {
    print_header "Table Sizes"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            schemaname||'.'||tablename AS \"Table\",
            pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS \"Size\",
            pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS \"Table Size\",
            pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) AS \"Index Size\"
        FROM pg_tables 
        WHERE schemaname NOT IN ('information_schema', 'pg_catalog')
        ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
        LIMIT 10;
    " 2>/dev/null
}

# Check connection statistics
check_connections() {
    print_header "Connection Statistics"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            datname as \"Database\",
            state as \"State\",
            COUNT(*) as \"Connections\"
        FROM pg_stat_activity 
        WHERE datname = current_database()
        GROUP BY datname, state
        ORDER BY \"Connections\" DESC;
    " 2>/dev/null
    
    echo
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            'Max Connections' as \"Setting\",
            setting as \"Value\"
        FROM pg_settings 
        WHERE name = 'max_connections'
        UNION ALL
        SELECT 
            'Active Connections' as \"Setting\",
            COUNT(*)::text as \"Value\"
        FROM pg_stat_activity 
        WHERE datname = current_database();
    " 2>/dev/null
}

# Check slow queries
check_slow_queries() {
    print_header "Long Running Queries"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            pid,
            usename as \"User\",
            application_name as \"App\",
            state,
            EXTRACT(EPOCH FROM (now() - query_start))::int as \"Duration (sec)\",
            LEFT(query, 100) as \"Query (first 100 chars)\"
        FROM pg_stat_activity 
        WHERE 
            datname = current_database()
            AND state != 'idle'
            AND query_start IS NOT NULL
            AND (now() - query_start) > interval '1 second'
        ORDER BY query_start;
    " 2>/dev/null
}

# Check index usage
check_index_usage() {
    print_header "Index Usage Statistics"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            schemaname||'.'||tablename as \"Table\",
            indexname as \"Index\",
            idx_scan as \"Times Used\",
            pg_size_pretty(pg_relation_size(schemaname||'.'||indexname)) as \"Size\"
        FROM pg_stat_user_indexes 
        ORDER BY idx_scan DESC, pg_relation_size(schemaname||'.'||indexname) DESC
        LIMIT 15;
    " 2>/dev/null
}

# Check table statistics
check_table_stats() {
    print_header "Table Activity Statistics"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            schemaname||'.'||tablename as \"Table\",
            n_tup_ins as \"Inserts\",
            n_tup_upd as \"Updates\",
            n_tup_del as \"Deletes\",
            seq_scan as \"Seq Scans\",
            idx_scan as \"Index Scans\",
            n_live_tup as \"Live Rows\"
        FROM pg_stat_user_tables 
        ORDER BY (n_tup_ins + n_tup_upd + n_tup_del) DESC
        LIMIT 10;
    " 2>/dev/null
}

# Check disk space
check_disk_space() {
    print_header "Disk Space Usage"
    
    # Check local disk space
    echo "Local Disk Usage:"
    df -h "$PROJECT_ROOT" 2>/dev/null || echo "Cannot determine disk usage"
    echo
    
    # Check database data directory (if accessible)
    if command -v docker &> /dev/null && docker ps | grep -q "campus_marketplace_db"; then
        echo "Docker Volume Usage:"
        docker exec campus_marketplace_db df -h /var/lib/postgresql/data 2>/dev/null || echo "Cannot access container disk usage"
    fi
}

# Check backup status
check_backup_status() {
    print_header "Backup Status"
    
    local backup_dir="$PROJECT_ROOT/db/backups/local"
    
    if [ -d "$backup_dir" ]; then
        local latest_backup=$(find "$backup_dir" -name "*.sql.gz" -type f | sort -r | head -1)
        
        if [ -n "$latest_backup" ]; then
            local backup_date=$(stat -c %y "$latest_backup" 2>/dev/null || stat -f %Sm "$latest_backup" 2>/dev/null)
            local backup_size=$(du -h "$latest_backup" | cut -f1)
            
            print_success "Latest backup: $(basename "$latest_backup")"
            echo "Date: $backup_date"
            echo "Size: $backup_size"
            
            # Check if backup is recent (within last 24 hours)
            local backup_timestamp=$(stat -c %Y "$latest_backup" 2>/dev/null || stat -f %m "$latest_backup" 2>/dev/null)
            local current_timestamp=$(date +%s)
            local age_hours=$(( (current_timestamp - backup_timestamp) / 3600 ))
            
            if [ "$age_hours" -gt 24 ]; then
                print_warning "Latest backup is $age_hours hours old"
            else
                print_success "Backup is recent ($age_hours hours old)"
            fi
        else
            print_warning "No backup files found"
        fi
        
        # Count backup files
        local backup_count=$(find "$backup_dir" -name "*.sql.gz" -type f | wc -l)
        echo "Total backups: $backup_count"
    else
        print_warning "Backup directory not found: $backup_dir"
    fi
}

# Performance metrics
check_performance() {
    print_header "Performance Metrics"
    
    export PGPASSWORD="$DB_APP_PASSWORD"
    
    # Check if pg_stat_statements is available
    local has_pg_stat_statements=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" \
                                      -t -c "SELECT COUNT(*) FROM pg_extension WHERE extname = 'pg_stat_statements';" 2>/dev/null | xargs)
    
    if [ "$has_pg_stat_statements" -gt 0 ]; then
        echo "Top queries by total time:"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
            SELECT 
                calls,
                ROUND(total_exec_time::numeric, 2) as \"Total Time (ms)\",
                ROUND(mean_exec_time::numeric, 2) as \"Avg Time (ms)\",
                LEFT(query, 80) as \"Query\"
            FROM pg_stat_statements 
            ORDER BY total_exec_time DESC 
            LIMIT 10;
        " 2>/dev/null
    else
        echo "pg_stat_statements extension not available for detailed query statistics"
    fi
    
    # Cache hit ratio
    echo
    echo "Cache Hit Ratios:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            'Buffer Cache Hit Ratio' as \"Metric\",
            ROUND(
                100.0 * sum(blks_hit) / (sum(blks_hit) + sum(blks_read)), 2
            )::text || '%' as \"Value\"
        FROM pg_stat_database 
        WHERE datname = current_database()
        AND (blks_read + blks_hit) > 0;
    " 2>/dev/null
}

# Health check summary
health_check() {
    print_header "Database Health Check Summary"
    
    local status=0
    
    # Test connection
    if test_connection; then
        print_success "Connection: OK"
    else
        print_error "Connection: FAILED"
        status=1
    fi
    
    # Check backup age
    local backup_dir="$PROJECT_ROOT/db/backups/local"
    if [ -d "$backup_dir" ]; then
        local latest_backup=$(find "$backup_dir" -name "*.sql.gz" -type f | sort -r | head -1)
        if [ -n "$latest_backup" ]; then
            local backup_timestamp=$(stat -c %Y "$latest_backup" 2>/dev/null || stat -f %m "$latest_backup" 2>/dev/null)
            local current_timestamp=$(date +%s)
            local age_hours=$(( (current_timestamp - backup_timestamp) / 3600 ))
            
            if [ "$age_hours" -le 24 ]; then
                print_success "Backup: Recent (${age_hours}h old)"
            else
                print_warning "Backup: Old (${age_hours}h old)"
            fi
        else
            print_warning "Backup: No backups found"
        fi
    fi
    
    # Check disk space
    local disk_usage=$(df "$PROJECT_ROOT" 2>/dev/null | tail -1 | awk '{print $5}' | sed 's/%//')
    if [ -n "$disk_usage" ] && [ "$disk_usage" -lt 90 ]; then
        print_success "Disk Space: OK (${disk_usage}% used)"
    else
        print_warning "Disk Space: High usage (${disk_usage}% used)"
    fi
    
    echo
    echo "Overall Status: $([ $status -eq 0 ] && echo -e "${GREEN}HEALTHY${NC}" || echo -e "${RED}ISSUES DETECTED${NC}")"
    
    return $status
}

# Show usage
show_usage() {
    cat << EOF
Campus Marketplace Database Monitor
==================================

Usage: $0 [OPTION]

Options:
  --health            Quick health check summary
  --full              Full monitoring report
  --connections       Show connection statistics
  --size              Show database and table sizes
  --performance       Show performance metrics
  --queries           Show long-running queries
  --indexes           Show index usage statistics
  --tables            Show table activity statistics
  --backups           Show backup status
  --disk              Show disk space usage
  --help              Show this help message

Examples:
  $0 --health         # Quick health check
  $0 --full           # Complete monitoring report
  $0 --performance    # Performance metrics only

EOF
}

# Main execution
main() {
    local option="${1:---health}"
    
    case "$option" in
        --health)
            health_check
            ;;
        --full)
            test_connection && echo
            check_database_size && echo
            show_table_sizes && echo
            check_connections && echo
            check_slow_queries && echo
            check_index_usage && echo
            check_table_stats && echo
            check_performance && echo
            check_backup_status && echo
            check_disk_space && echo
            health_check
            ;;
        --connections)
            check_connections
            ;;
        --size)
            check_database_size
            echo
            show_table_sizes
            ;;
        --performance)
            check_performance
            ;;
        --queries)
            check_slow_queries
            ;;
        --indexes)
            check_index_usage
            ;;
        --tables)
            check_table_stats
            ;;
        --backups)
            check_backup_status
            ;;
        --disk)
            check_disk_space
            ;;
        --help|-h)
            show_usage
            ;;
        *)
            echo "Unknown option: $option"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"