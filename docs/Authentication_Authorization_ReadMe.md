# Campus Marketplace - Authentication & Authorization Module

## Overview

This document provides comprehensive technical documentation for the Authentication and Authorization module of the Campus Marketplace project. The module implements JWT-based authentication with role-based access control (RBAC) supporting two user roles: **Student** and **Admin**.

## Architecture

### Core Components
- **JWT-based Authentication** with HS512 signing
- **Role-Based Access Control (RBAC)** with Student and Admin roles
- **Refresh Token Support** for long-lived sessions (7 days)
- **Spring Security Integration** with custom filters and aspects
- **Database Integration** with PostgreSQL

### Key Classes
- `User` (base class with inheritance for Student/Admin)
- `AuthController` - REST endpoints for authentication
- `AuthService` - Business logic for authentication operations
- `JwtUtil` - JWT token generation and validation
- `RoleAuthorizationAspect` - Method-level role enforcement
- `RefreshToken` - Persistent refresh token management

---

## 📋 Complete API Endpoints

### Authentication Endpoints

#### 1. **Login** 
```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
    "username": "student",
    "password": "password123",
    "deviceInfo": "Postman Test Device"
}
```

**Success Response (200):**
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "role": "STUDENT",
    "username": "student",
    "userId": 1,
    "email": "student@sjsu.edu",
    "firstName": "John",
    "lastName": "Student",
    "phone": "555-0101",
    "active": true
}
```

#### 2. **Register New Student**
```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
    "username": "newstudent",
    "email": "newstudent@sjsu.edu",
    "password": "password123",
    "role": "STUDENT",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "555-0123",
    "studentId": "STU999",
    "major": "Computer Science",
    "graduationYear": 2025,
    "campusLocation": "San Jose Main Campus"
}
```

**Success Response (200):**
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "role": "STUDENT",
    "username": "newstudent",
    "userId": 2,
    "email": "newstudent@sjsu.edu",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "555-0123",
    "active": true
}
```

#### 3. **Register New Admin**
```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
    "username": "newadmin",
    "email": "newadmin@sjsu.edu",
    "password": "password123",
    "role": "ADMIN",
    "firstName": "Jane",
    "lastName": "Admin",
    "phone": "555-0124",
    "adminLevel": "SUPER_ADMIN"
}
```

#### 4. **Token Refresh**
```http
POST /api/auth/refresh
Content-Type: application/json
```

**Request Body:**
```json
{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### 5. **Token Validation**
```http
GET /api/auth/validate
Authorization: Bearer <access_token> (optional)
```

**Success Response (200) - With Valid Token:**
```json
{
    "valid": true,
    "username": "student",
    "authorities": [
        {
            "authority": "ROLE_STUDENT"
        }
    ]
}
```

**Response (401) - Without Token:**
```json
{
    "valid": false,
    "message": "No valid token found"
}
```

#### 6. **Current User Info**
```http
GET /api/auth/me
Authorization: Bearer <access_token>
```

#### 7. **Logout**
```http
POST /api/auth/logout
Content-Type: application/json
```

**Request Body:**
```json
{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### 8. **Logout All Devices**
```http
POST /api/auth/logout-all
Authorization: Bearer <access_token>
```

### Student Endpoints (Requires Authentication)

#### 1. **Student Dashboard**
```http
GET /api/student/dashboard
Authorization: Bearer <student_access_token>
```

#### 2. **My Listings**
```http
GET /api/student/listings
Authorization: Bearer <student_access_token>
```

#### 3. **Create Listing**
```http
POST /api/student/listings
Authorization: Bearer <student_access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
    "title": "MacBook Pro 13-inch",
    "description": "Excellent condition MacBook Pro, barely used",
    "price": 1200.00,
    "category": "Electronics",
    "condition": "Like New",
    "location": "San Jose Campus"
}
```

### Admin Endpoints (Requires Authentication)

#### 1. **Admin Dashboard**
```http
GET /api/admin/dashboard
Authorization: Bearer <admin_access_token>
```

#### 2. **Users Management**
```http
GET /api/admin/users
Authorization: Bearer <admin_access_token>
```

#### 3. **Moderate Listing**
```http
POST /api/admin/moderate/123?action=approve
Authorization: Bearer <admin_access_token>
```

#### 4. **Delete User**
```http
DELETE /api/admin/users/456
Authorization: Bearer <admin_access_token>
```

### Public Endpoints

#### 1. **Home**
```http
GET /
```

#### 2. **Test Hello**
```http
GET /api/test/hello
```

---

## 🧪 Postman Testing Guide

### **Base URL**
```
http://localhost:8080
```

### **Environment Variables Setup**
Create these variables in Postman:
- `base_url`: `http://localhost:8080`
- `student_token`: (set after student login)
- `admin_token`: (set after admin login)
- `refresh_token`: (set after login)

### **Complete Postman Collection**

#### **1. Authentication Flow**

**Login as Student:**
```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "username": "student",
  "password": "password123",
  "deviceInfo": "Postman Test Device"
}
```
*Save `accessToken` and `refreshToken` from response*

**Login as Admin:**
```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123",
  "deviceInfo": "Postman Test Device"
}
```

**Register New Student:**
```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "username": "newstudent",
  "email": "newstudent@sjsu.edu",
  "password": "password123",
  "role": "STUDENT",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "555-0123",
  "studentId": "STU999",
  "major": "Computer Science",
  "graduationYear": 2025,
  "campusLocation": "San Jose Main Campus"
}
```

**Register New Admin:**
```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "username": "newadmin",
  "email": "newadmin@sjsu.edu",
  "password": "password123",
  "role": "ADMIN",
  "firstName": "Jane",
  "lastName": "Admin",
  "phone": "555-0124",
  "adminLevel": "SUPER_ADMIN"
}
```

#### **2. Token Management**

**Validate Token:**
```http
GET {{base_url}}/api/auth/validate
Authorization: Bearer {{student_token}}
```

**Get Current User:**
```http
GET {{base_url}}/api/auth/me
Authorization: Bearer {{student_token}}
```

**Refresh Token:**
```http
POST {{base_url}}/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "{{refresh_token}}"
}
```

**Logout:**
```http
POST {{base_url}}/api/auth/logout
Content-Type: application/json

{
  "refreshToken": "{{refresh_token}}"
}
```

**Logout All Devices:**
```http
POST {{base_url}}/api/auth/logout-all
Authorization: Bearer {{student_token}}
```

#### **3. Student Endpoints**

**Student Dashboard:**
```http
GET {{base_url}}/api/student/dashboard
Authorization: Bearer {{student_token}}
```

**Get My Listings:**
```http
GET {{base_url}}/api/student/listings
Authorization: Bearer {{student_token}}
```

**Create New Listing:**
```http
POST {{base_url}}/api/student/listings
Authorization: Bearer {{student_token}}
Content-Type: application/json

{
  "title": "MacBook Pro 13-inch",
  "description": "Excellent condition MacBook Pro, barely used",
  "price": 1200.00,
  "category": "Electronics",
  "condition": "Like New",
  "location": "San Jose Campus"
}
```

#### **4. Admin Endpoints**

**Admin Dashboard:**
```http
GET {{base_url}}/api/admin/dashboard
Authorization: Bearer {{admin_token}}
```

**Get All Users:**
```http
GET {{base_url}}/api/admin/users
Authorization: Bearer {{admin_token}}
```

**Moderate Listing:**
```http
POST {{base_url}}/api/admin/moderate/123?action=approve
Authorization: Bearer {{admin_token}}
```

**Delete User:**
```http
DELETE {{base_url}}/api/admin/users/456
Authorization: Bearer {{admin_token}}
```

#### **5. Public Endpoints**

**Home:**
```http
GET {{base_url}}/
```

**Test Hello:**
```http
GET {{base_url}}/api/test/hello
```

### **Testing Workflow**

1. **Start Application**: `mvn spring-boot:run`
2. **Test Public Endpoints**: Home, Test Hello
3. **Test Authentication**: Login as student and admin
4. **Test Registration**: Register new users
5. **Test Token Management**: Validate, refresh, logout
6. **Test Student Features**: Dashboard, listings, create listing
7. **Test Admin Features**: Dashboard, users, moderation
8. **Test Authorization**: Cross-role access (should fail)

### **Expected Responses**

**Successful Login:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "role": "STUDENT",
  "username": "student",
  "userId": 1,
  "email": "student@sjsu.edu",
  "firstName": "John",
  "lastName": "Student",
  "phone": "555-0101",
  "active": true
}
```

**Error Response:**
```json
{
  "error": "Authentication failed",
  "message": "Invalid username or password"
}
```

---

## 🔒 Security Implementation

### **JWT Configuration**
- **Algorithm:** HS512 (HMAC with SHA-512)
- **Access Token Expiration:** 1 hour (3600000 milliseconds)
- **Refresh Token Expiration:** 7 days (604800000 milliseconds)
- **Token Claims:** role, userId, email, username

### **Role-Based Access Control**
- **URL-Level Security:** Spring Security configuration
- **Method-Level Security:** Custom `@RequireRole` annotation
- **Password Security:** BCrypt hashing with salt
- **Token Management:** Refresh token storage and revocation

### **Security Configuration** (`application.properties`)
```properties
# JWT Secret (256-bit minimum for HS512)
jwt.secret=myVerySecureSecretKeyForJWTTokensThatShouldBeAtLeast256BitsLongForHS256Algorithm

# Token Expiration (in milliseconds)
jwt.access-token.expiration=3600000   # 1 hour
jwt.refresh-token.expiration=604800000 # 7 days

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/campus_marketplace
spring.datasource.username=vineetkia
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

---

## 🧪 Testing Implementation

### **Test Users**
| Role | Username | Password | Email | Details |
|------|----------|----------|--------|---------|
| **Student** | `student` | `password123` | `student@sjsu.edu` | John Student, CS Major, Graduation: 2025 |
| **Admin** | `admin` | `password123` | `admin@sjsu.edu` | Jane Admin, Full permissions |

### **Unit Testing**
```bash
# Run all authentication tests
mvn test

# Run specific test suites
mvn test -Dtest=JwtUtilTest        # JWT utility tests
mvn test -Dtest=AuthServiceTest    # Authentication service tests
mvn test -Dtest=AuthControllerTest # Controller integration tests
mvn test -Dtest=RoleBasedAccessTest # Role-based access tests
```

---

## 🏗️ Implementation Details

### **Key Features**
- **JWT Authentication** with HS512 signing
- **Refresh Token Management** with 7-day expiration
- **Role-Based Authorization** with Student/Admin separation
- **Spring Security Integration** with custom filters and aspects
- **Database Inheritance** with User/Student/Admin models
- **User Registration** with role-specific fields
- **Comprehensive Testing** with Postman collection
- **Production Configuration** for PostgreSQL deployment

### **Database Schema**
- **Single Table Inheritance** for User/Student/Admin models
- **Refresh Token Storage** with expiration management
- **Password Hashing** with BCrypt salt
- **Role-Based Access Control** with database-level constraints
