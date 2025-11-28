# Scripts

Utility scripts for database setup, development, and maintenance.

## Available Scripts

### Database Setup
- **create-db-user.sh** - Creates PostgreSQL user `cm_app_user` (run if database user doesn't exist)
- **setup-database.sh** - Complete database setup with Docker Compose
- **start-dev-db.sh** - Quick start for development database

### Usage

```bash
# Create database user (if needed)
./scripts/create-db-user.sh

# Setup complete database environment
./scripts/setup-database.sh

# Start development database
./scripts/start-dev-db.sh
```

## Database Scripts

Additional database scripts are located in `db/scripts/`:
- `db/scripts/backup.sh` - Database backup
- `db/scripts/restore.sh` - Database restore
- `db/scripts/monitor.sh` - Database monitoring
- `db/scripts/test-connection.sh` - Connection testing

## Notes

- All scripts should be run from the project root directory
- Scripts use environment variables from `.env` or `docker-compose.yml`
- See [DEPLOYMENT_GUIDE.md](../docs/DEPLOYMENT_GUIDE.md) for detailed instructions

