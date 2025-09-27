#!/bin/sh
# ==============================================
# Campus Marketplace Database Quick Start
# ==============================================
# This script sets up the database environment quickly

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}======================================"
    echo -e "$1"
    echo -e "======================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
    fi
    print_success "Docker is installed"
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
    fi
    print_success "Docker Compose is installed"
    
    # Check if Docker daemon is running
    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running. Please start Docker."
    fi
    print_success "Docker daemon is running"
}

# Setup environment
setup_environment() {
    print_header "Setting Up Environment"
    
    if [ ! -f ".env" ]; then
        if [ -f ".env.template" ]; then
            cp .env.template .env
            print_success "Created .env from template"
            print_warning "Please edit .env file and update passwords before continuing!"
            print_info "Required variables: DB_ROOT_PASSWORD, DB_APP_PASSWORD, DB_READONLY_PASSWORD, PGADMIN_PASSWORD"
            
            # Ask if user wants to continue
            read -p "Have you updated the .env file with secure passwords? (y/n): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                print_warning "Please update .env file and run this script again."
                exit 0
            fi
        else
            print_error ".env.template not found. Please ensure you're in the project root directory."
        fi
    else
        print_success "Environment file (.env) exists"
    fi
}

# Start database services
start_services() {
    print_header "Starting Database Services"
    
    print_info "Starting PostgreSQL, pgAdmin, and Redis..."
    if docker-compose up -d; then
        print_success "Services started successfully"
    else
        print_error "Failed to start services"
    fi
    
    # Wait for services to be healthy
    print_info "Waiting for services to be ready..."
    sleep 10
    
    # Check service status
    print_info "Checking service status..."
    docker-compose ps
}

# Verify setup
verify_setup() {
    print_header "Verifying Database Setup"
    
    # Test database connection
    print_info "Testing database connection..."
    if ./db/scripts/test-connection.sh; then
        print_success "Database connection test passed"
    else
        print_error "Database connection test failed"
    fi
    
    # Run health check
    print_info "Running health check..."
    ./db/scripts/monitor.sh --health
}

# Display access information
show_access_info() {
    print_header "Access Information"
    
    echo "🗄️  Database Access:"
    echo "   Host: localhost"
    echo "   Port: 5432"
    echo "   Database: campus_marketplace"
    echo "   App User: cm_app_user"
    echo "   Read-only User: cm_readonly"
    echo ""
    
    echo "🔧 pgAdmin Access:"
    echo "   URL: http://localhost:8080"
    echo "   Check your .env file for login credentials"
    echo ""
    
    echo "🚀 Application Profiles:"
    echo "   Development: ./mvnw spring-boot:run"
    echo "   Production:  ./mvnw spring-boot:run -Dspring.profiles.active=prod"
    echo ""
    
    echo "📊 Monitoring Commands:"
    echo "   Health Check: ./db/scripts/monitor.sh --health"
    echo "   Full Report:  ./db/scripts/monitor.sh --full"
    echo "   Connections:  ./db/scripts/monitor.sh --connections"
    echo ""
    
    echo "💾 Backup Commands:"
    echo "   Create Backup: ./db/scripts/backup.sh"
    echo "   Restore Latest: ./db/scripts/restore.sh --latest"
    echo "   List Backups: ./db/scripts/restore.sh --list"
}

# Show next steps
show_next_steps() {
    print_header "Next Steps"
    
    echo "1. 📖 Read the documentation:"
    echo "   - Complete setup guide: ./db/docs/DATABASE_SETUP.md"
    echo "   - Troubleshooting guide: ./db/docs/TROUBLESHOOTING.md"
    echo "   - Security guide: ./db/docs/SECURITY.md"
    echo ""
    
    echo "2. 🏃‍♂️ Start your Spring Boot application:"
    echo "   cd backend"
    echo "   ./mvnw spring-boot:run -Dspring.profiles.active=prod"
    echo ""
    
    echo "3. 🔧 Access pgAdmin for database management:"
    echo "   Open: http://localhost:8080"
    echo ""
    
    echo "4. ⚙️  Set up automated backups (optional):"
    echo "   cat ./db/scripts/crontab.example"
    echo "   crontab -e  # Add the cron jobs"
    echo ""
    
    echo "5. 🔒 Review security settings:"
    echo "   - Update default passwords"
    echo "   - Configure SSL certificates for production"
    echo "   - Review user permissions"
}

# Main execution
main() {
    print_header "Campus Marketplace Database Quick Start"
    
    case "${1:-setup}" in
        setup)
            check_prerequisites
            setup_environment
            start_services
            verify_setup
            show_access_info
            show_next_steps
            print_success "Database setup completed successfully! 🎉"
            ;;
        stop)
            print_info "Stopping database services..."
            docker-compose down
            print_success "Services stopped"
            ;;
        restart)
            print_info "Restarting database services..."
            docker-compose restart
            sleep 5
            verify_setup
            print_success "Services restarted"
            ;;
        status)
            print_info "Checking service status..."
            docker-compose ps
            echo ""
            ./db/scripts/monitor.sh --health
            ;;
        logs)
            print_info "Showing recent logs..."
            docker-compose logs --tail=50
            ;;
        cleanup)
            read -p "This will remove all database data. Are you sure? (yes/no): " -r
            if [[ $REPLY == "yes" ]]; then
                print_warning "Removing all database data..."
                docker-compose down -v
                print_success "Cleanup completed"
            else
                print_info "Cleanup cancelled"
            fi
            ;;
        help|--help|-h)
            echo "Campus Marketplace Database Quick Start"
            echo ""
            echo "Usage: $0 [COMMAND]"
            echo ""
            echo "Commands:"
            echo "  setup    (default) Set up and start database services"
            echo "  stop     Stop database services"
            echo "  restart  Restart database services"
            echo "  status   Show service status and health"
            echo "  logs     Show recent service logs"
            echo "  cleanup  Remove all database data (WARNING: destructive)"
            echo "  help     Show this help message"
            ;;
        *)
            print_error "Unknown command: $1. Use 'help' to see available commands."
            ;;
    esac
}

# Run main function with all arguments
main "$@"