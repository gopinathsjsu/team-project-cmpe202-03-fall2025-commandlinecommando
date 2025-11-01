# Campus Marketplace Database Security Guide

This document outlines security best practices and configurations for the Campus Marketplace PostgreSQL database.

## Table of Contents

1. [Security Overview](#security-overview)
2. [Authentication & Authorization](#authentication--authorization)
3. [Network Security](#network-security)
4. [Data Encryption](#data-encryption)
5. [Audit & Monitoring](#audit--monitoring)
6. [Backup Security](#backup-security)
7. [Security Maintenance](#security-maintenance)
8. [Compliance & Privacy](#compliance--privacy)

## Security Overview

### Security Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Application   │    │   Network Layer  │    │    Database     │
│                 │    │                  │    │                 │
│ • JWT Auth      │◄──►│ • SSL/TLS       │◄──►│ • User Roles    │
│ • Input Valid   │    │ • Firewall      │    │ • Permissions   │
│ • Conn Pool     │    │ • VPN (prod)    │    │ • Encryption    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Security Principles

1. **Least Privilege**: Users have minimal necessary permissions
2. **Defense in Depth**: Multiple security layers
3. **Zero Trust**: Verify all connections and requests
4. **Data Privacy**: Encrypt sensitive data at rest and in transit
5. **Audit Trail**: Log all security-relevant events

## Authentication & Authorization

### User Account Security

#### 1. Database Users

The system uses three types of database users:

```sql
-- 1. Admin user (postgres) - Database administration only
-- 2. Application user (cm_app_user) - Full application access
-- 3. Read-only user (cm_readonly) - Analytics and reporting
```

#### 2. Password Policy

**Requirements:**
- Minimum 12 characters
- Include uppercase, lowercase, numbers, and symbols
- No dictionary words or personal information
- Change every 90 days in production

**Password Generation Example:**
```bash
# Generate strong password
openssl rand -base64 32 | tr -d "=+/" | cut -c1-25
```

#### 3. User Permissions

```sql
-- Application User Permissions (cm_app_user)
GRANT CONNECT ON DATABASE campus_marketplace TO cm_app_user;
GRANT USAGE ON SCHEMA public TO cm_app_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cm_app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cm_app_user;

-- Read-only User Permissions (cm_readonly)
GRANT CONNECT ON DATABASE campus_marketplace TO cm_readonly;
GRANT USAGE ON SCHEMA public TO cm_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO cm_readonly;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO cm_readonly;
```

#### 4. Role-Based Access Control

```sql
-- Create roles for different access levels
CREATE ROLE app_read;
CREATE ROLE app_write;
CREATE ROLE app_admin;

-- Grant permissions to roles
GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_read;
GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_write;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_admin;

-- Assign roles to users
GRANT app_read TO cm_readonly;
GRANT app_read, app_write TO cm_app_user;
```

### Authentication Configuration

#### 1. PostgreSQL Authentication (pg_hba.conf)

```bash
# Recommended pg_hba.conf settings
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# Local connections (development only)
local   all             postgres                                peer
local   campus_marketplace  cm_app_user                        md5

# Remote connections (production)
hostssl campus_marketplace  cm_app_user    0.0.0.0/0           md5
hostssl campus_marketplace  cm_readonly    0.0.0.0/0           md5

# Reject all other connections
host    all             all            0.0.0.0/0               reject
```

#### 2. Connection Security

```yaml
# Application connection security
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/campus_marketplace?sslmode=require&sslcert=client.crt&sslkey=client.key&sslrootcert=ca.crt
    username: ${DB_APP_USER}
    password: ${DB_APP_PASSWORD}
    hikari:
      connection-test-query: SELECT 1
      validation-timeout: 3000
```

## Network Security

### 1. SSL/TLS Configuration

#### Generate SSL Certificates

```bash
# Create Certificate Authority (CA)
openssl genrsa -out ca-key.pem 4096
openssl req -new -x509 -days 365 -key ca-key.pem -out ca.pem \
  -subj "/C=US/ST=CA/L=San Jose/O=SJSU/CN=Campus Marketplace CA"

# Generate server certificate
openssl genrsa -out server-key.pem 4096
openssl req -new -key server-key.pem -out server.csr \
  -subj "/C=US/ST=CA/L=San Jose/O=SJSU/CN=campus-marketplace-db"
openssl x509 -req -days 365 -in server.csr -CA ca.pem -CAkey ca-key.pem \
  -CAcreateserial -out server-cert.pem

# Generate client certificate
openssl genrsa -out client-key.pem 4096
openssl req -new -key client-key.pem -out client.csr \
  -subj "/C=US/ST=CA/L=San Jose/O=SJSU/CN=campus-marketplace-client"
openssl x509 -req -days 365 -in client.csr -CA ca.pem -CAkey ca-key.pem \
  -CAcreateserial -out client-cert.pem

# Set proper permissions
chmod 600 *-key.pem
chmod 644 *-cert.pem ca.pem
```

#### PostgreSQL SSL Configuration

```bash
# postgresql.conf
ssl = on
ssl_cert_file = '/var/lib/postgresql/server-cert.pem'
ssl_key_file = '/var/lib/postgresql/server-key.pem'
ssl_ca_file = '/var/lib/postgresql/ca.pem'
ssl_ciphers = 'HIGH:MEDIUM:+3DES:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!PSK:!SRP:!DSS'
ssl_prefer_server_ciphers = on
```

### 2. Firewall Configuration

#### iptables Rules (Linux)

```bash
# Allow PostgreSQL only from application servers
sudo iptables -A INPUT -p tcp --dport 5432 -s 10.0.1.0/24 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 5432 -j DROP

# Allow pgAdmin only from admin networks
sudo iptables -A INPUT -p tcp --dport 8080 -s 192.168.1.0/24 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8080 -j DROP

# Save rules
sudo iptables-save > /etc/iptables/rules.v4
```

#### UFW (Ubuntu)

```bash
# Allow specific IPs for database access
sudo ufw allow from 10.0.1.0/24 to any port 5432
sudo ufw allow from 192.168.1.0/24 to any port 8080

# Deny all other access
sudo ufw deny 5432
sudo ufw deny 8080
```

### 3. Network Isolation

#### Docker Network Security

```yaml
# docker-compose.yml
networks:
  campus_marketplace_network:
    driver: bridge
    internal: true  # No external access
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

#### VPN Access (Production)

```bash
# Example OpenVPN client configuration
client
dev tun
proto udp
remote vpn.campusmarketplace.com 1194
resolv-retry infinite
nobind
ca ca.crt
cert client.crt
key client.key
tls-auth ta.key 1
cipher AES-256-CBC
comp-lzo
```

## Data Encryption

### 1. Encryption at Rest

#### PostgreSQL Data Encryption

```sql
-- Install pgcrypto extension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Encrypt sensitive columns
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    encrypted_phone TEXT,  -- Encrypted phone numbers
    encrypted_ssn TEXT,    -- Encrypted SSN (if required)
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Insert encrypted data
INSERT INTO users (email, encrypted_phone, password_hash)
VALUES (
    'user@example.com',
    pgp_sym_encrypt('555-123-4567', 'encryption_key'),
    '$2a$10$hash...'  -- Use bcrypt in application
);

-- Query encrypted data
SELECT 
    email,
    pgp_sym_decrypt(encrypted_phone::bytea, 'encryption_key') as phone
FROM users 
WHERE id = 1;
```

#### Application-Level Encryption

```java
@Entity
public class User {
    @Id
    private Long id;
    
    private String email;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;  // Automatically encrypted/decrypted
    
    // ... other fields
}

// Custom converter for automatic encryption
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        return decrypt(dbData);
    }
}
```

### 2. Encryption in Transit

#### Application Configuration

```yaml
# Force SSL connections
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/campus_marketplace?sslmode=require&sslcert=client.crt&sslkey=client.key
    
# HTTPS configuration (production)
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### 3. Key Management

#### Environment-Based Key Storage

```bash
# .env file (never commit to version control)
ENCRYPTION_KEY=base64_encoded_32_byte_key
DATABASE_ENCRYPTION_KEY=another_secure_key
JWT_SIGNING_KEY=jwt_specific_key

# Use external key management in production
# AWS KMS, Azure Key Vault, or HashiCorp Vault
```

#### Key Rotation Strategy

```sql
-- Key rotation procedure
BEGIN;

-- 1. Add new encrypted column
ALTER TABLE users ADD COLUMN encrypted_phone_v2 TEXT;

-- 2. Migrate data with new key
UPDATE users SET encrypted_phone_v2 = pgp_sym_encrypt(
    pgp_sym_decrypt(encrypted_phone::bytea, 'old_key'),
    'new_key'
) WHERE encrypted_phone IS NOT NULL;

-- 3. Verify migration
-- 4. Drop old column
ALTER TABLE users DROP COLUMN encrypted_phone;
ALTER TABLE users RENAME COLUMN encrypted_phone_v2 TO encrypted_phone;

COMMIT;
```

## Audit & Monitoring

### 1. Database Audit Logging

#### Enable PostgreSQL Logging

```bash
# postgresql.conf
logging_collector = on
log_directory = '/var/log/postgresql'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_rotation_age = 1d
log_rotation_size = 100MB

# Security-related logging
log_connections = on
log_disconnections = on
log_duration = on
log_statement = 'mod'  # Log all modifications
log_min_duration_statement = 1000  # Log slow queries

# Failed connection attempts
log_hostname = on
log_invalid_auth_header = on
```

#### Audit Trigger Implementation

```sql
-- Create audit schema
CREATE SCHEMA audit;

-- Audit table
CREATE TABLE audit.security_events (
    id BIGSERIAL PRIMARY KEY,
    event_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    event_type VARCHAR(50) NOT NULL,  -- LOGIN, LOGOUT, FAILED_AUTH, DATA_ACCESS, etc.
    user_name VARCHAR(100),
    client_ip INET,
    table_name VARCHAR(100),
    operation VARCHAR(20),
    old_values JSONB,
    new_values JSONB,
    success BOOLEAN DEFAULT TRUE
);

-- Security audit function
CREATE OR REPLACE FUNCTION audit.log_security_event() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit.security_events (
        event_type, user_name, table_name, operation, old_values, new_values
    ) VALUES (
        'DATA_MODIFICATION',
        session_user,
        TG_TABLE_NAME,
        TG_OP,
        CASE WHEN TG_OP = 'DELETE' THEN to_jsonb(OLD) ELSE NULL END,
        CASE WHEN TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN to_jsonb(NEW) ELSE NULL END
    );
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Apply audit triggers to sensitive tables
CREATE TRIGGER users_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION audit.log_security_event();
```

### 2. Application Security Monitoring

#### Spring Boot Security Events

```java
@EventListener
public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
    String username = event.getAuthentication().getName();
    String clientIp = getClientIp();
    
    auditService.logSecurityEvent(
        SecurityEventType.LOGIN_SUCCESS,
        username,
        clientIp
    );
}

@EventListener
public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
    String username = event.getAuthentication().getName();
    String clientIp = getClientIp();
    
    auditService.logSecurityEvent(
        SecurityEventType.LOGIN_FAILED,
        username,
        clientIp
    );
    
    // Implement account lockout after multiple failures
    securityService.handleFailedLogin(username, clientIp);
}
```

### 3. Security Monitoring Dashboard

#### Key Security Metrics

```sql
-- Failed login attempts (last 24 hours)
SELECT 
    DATE_TRUNC('hour', event_time) as hour,
    COUNT(*) as failed_attempts
FROM audit.security_events 
WHERE event_type = 'LOGIN_FAILED' 
AND event_time > NOW() - INTERVAL '24 hours'
GROUP BY hour
ORDER BY hour;

-- Unusual data access patterns
SELECT 
    user_name,
    COUNT(*) as access_count,
    COUNT(DISTINCT table_name) as tables_accessed,
    MIN(event_time) as first_access,
    MAX(event_time) as last_access
FROM audit.security_events 
WHERE event_type = 'DATA_ACCESS'
AND event_time > NOW() - INTERVAL '1 hour'
GROUP BY user_name
HAVING COUNT(*) > 100  -- Threshold for unusual activity
ORDER BY access_count DESC;

-- Privilege escalation attempts
SELECT event_time, user_name, operation, table_name, new_values
FROM audit.security_events 
WHERE table_name = 'users' 
AND operation = 'UPDATE'
AND new_values->>'role' != old_values->>'role'
ORDER BY event_time DESC;
```

## Backup Security

### 1. Secure Backup Storage

#### Encrypted Backups

```bash
# Modify backup script to include encryption
create_encrypted_backup() {
    local backup_file=$1
    local encrypted_file="${backup_file}.gpg"
    
    # Encrypt backup
    gpg --symmetric --cipher-algo AES256 \
        --passphrase "$BACKUP_PASSPHRASE" \
        --output "$encrypted_file" \
        "$backup_file"
    
    # Remove unencrypted backup
    rm "$backup_file"
    
    # Generate checksum for encrypted file
    sha256sum "$encrypted_file" > "${encrypted_file}.sha256"
}
```

#### Secure Remote Storage

```bash
# AWS S3 with server-side encryption
aws s3 cp backup.sql.gz.gpg \
    s3://campus-marketplace-backups/ \
    --server-side-encryption AES256 \
    --storage-class STANDARD_IA

# Azure Blob Storage with encryption
az storage blob upload \
    --file backup.sql.gz.gpg \
    --container-name backups \
    --name "$(date +%Y/%m/%d)/backup.sql.gz.gpg" \
    --encryption-scope backup-encryption
```

### 2. Backup Access Control

#### IAM Policies (AWS Example)

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::ACCOUNT:user/backup-service"
            },
            "Action": [
                "s3:PutObject",
                "s3:PutObjectAcl"
            ],
            "Resource": "arn:aws:s3:::campus-marketplace-backups/*",
            "Condition": {
                "StringEquals": {
                    "s3:x-amz-server-side-encryption": "AES256"
                }
            }
        }
    ]
}
```

## Security Maintenance

### 1. Regular Security Tasks

#### Daily Tasks
```bash
# Check for failed login attempts
./db/scripts/monitor.sh --security-daily

# Review audit logs
tail -100 /var/log/postgresql/postgresql-$(date +%Y-%m-%d)*.log | grep "FATAL\|authentication failed"

# Check for unusual connection patterns
./db/scripts/monitor.sh --connections
```

#### Weekly Tasks
```bash
# Update passwords for non-production environments
./db/scripts/rotate-dev-passwords.sh

# Review user permissions
./db/scripts/audit-permissions.sh

# Check SSL certificate expiration
openssl x509 -in server-cert.pem -noout -dates
```

#### Monthly Tasks
```bash
# Review and rotate encryption keys
./db/scripts/rotate-encryption-keys.sh

# Security patch updates
docker-compose pull
sudo apt update && sudo apt upgrade

# Access review
./db/scripts/user-access-review.sh
```

### 2. Security Incident Response

#### Incident Response Plan

1. **Detection**: Monitor alerts, logs, and user reports
2. **Containment**: Isolate affected systems
3. **Eradication**: Remove threats and vulnerabilities
4. **Recovery**: Restore normal operations
5. **Lessons Learned**: Update security measures

#### Emergency Procedures

```bash
# Emergency user account lockout
./db/scripts/emergency-lockout.sh username

# Database connection shutdown
docker-compose exec postgres psql -U postgres -c "
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE usename != 'postgres';
"

# Backup current state
./db/scripts/emergency-backup.sh

# Network isolation
sudo iptables -A INPUT -p tcp --dport 5432 -j DROP
```

### 3. Security Updates

#### PostgreSQL Security Updates

```bash
# Check current version
docker-compose exec postgres psql -U postgres -c "SELECT version();"

# Update PostgreSQL (Docker)
docker-compose pull postgres
docker-compose up -d postgres

# Verify update
docker-compose logs postgres | grep "database system is ready"
```

#### Application Security Updates

```bash
# Check for vulnerable dependencies
cd backend
./mvnw dependency-check:check

# Update dependencies
./mvnw versions:use-latest-versions
./mvnw clean test  # Ensure tests pass

# Deploy security updates
./deploy-security-update.sh
```

## Compliance & Privacy

### 1. Data Privacy Regulations

#### FERPA Compliance (Educational Records)
- Implement role-based access control
- Log all access to student data
- Provide data export/deletion capabilities
- Secure data transmission and storage

#### GDPR Considerations (if applicable)
- Right to be forgotten implementation
- Data portability features
- Consent management
- Breach notification procedures

### 2. Privacy Implementation

#### Data Masking for Development

```sql
-- Create anonymized view for development
CREATE VIEW dev_users AS
SELECT 
    id,
    CONCAT('user', id, '@example.com') as email,
    'DEV_USER_' || id as name,
    role,
    created_at,
    updated_at
FROM users;

-- Grant access to development users
GRANT SELECT ON dev_users TO dev_team_role;
REVOKE SELECT ON users FROM dev_team_role;
```

#### Data Retention Policies

```sql
-- Automated data cleanup
CREATE OR REPLACE FUNCTION cleanup_old_data() RETURNS void AS $$
BEGIN
    -- Delete old audit logs (keep 2 years)
    DELETE FROM audit.security_events 
    WHERE event_time < NOW() - INTERVAL '2 years';
    
    -- Delete old session tokens (keep 30 days)
    DELETE FROM refresh_tokens 
    WHERE created_at < NOW() - INTERVAL '30 days';
    
    -- Archive old inactive listings (after 1 year)
    INSERT INTO archived_listings 
    SELECT * FROM listings 
    WHERE status = 'INACTIVE' 
    AND updated_at < NOW() - INTERVAL '1 year';
    
    DELETE FROM listings 
    WHERE status = 'INACTIVE' 
    AND updated_at < NOW() - INTERVAL '1 year';
END;
$$ LANGUAGE plpgsql;

-- Schedule cleanup job
SELECT cron.schedule('cleanup-old-data', '0 2 * * 0', 'SELECT cleanup_old_data();');
```

### 3. Security Documentation

#### Security Policy Documentation
- Password policies
- Access control procedures
- Incident response procedures
- Data classification guidelines
- Backup and recovery procedures

#### Training Materials
- Security awareness training
- Secure coding guidelines
- Database security best practices
- Incident reporting procedures

---

**Important**: This security guide should be reviewed and updated regularly. Security is an ongoing process, not a one-time setup.

**Last Updated**: December 2024  
**Next Review**: March 2025  
**Maintainer**: Campus Marketplace Security Team