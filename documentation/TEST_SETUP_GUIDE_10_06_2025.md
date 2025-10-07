# 🧪 Vineet's Test Cases Setup & Execution Guide

## 📋 **Overview**
This guide will walk you through setting up and running all of Vineet's test cases for the Campus Marketplace authentication and authorization module.

## 🎯 **Test Cases Available**

### **1. Unit Tests**
- `AuthServiceTest.java` - 13 test methods for authentication service
- `JwtUtilTest.java` - JWT utility tests
- `CampusmarketplaceApplicationTests.java` - Application context tests

### **2. Integration Tests**
- `AuthControllerTest.java` - 13 test methods for REST endpoints
- `RoleBasedAccessTest.java` - Role-based authorization tests

## 🚀 **Step-by-Step Setup**

### **Step 1: Prerequisites Check**

First, verify you have the required tools installed:

```bash
# Check Java version (should be 21+)
java -version

# Check Maven (if installed globally)
mvn -version

# Check if you're in the correct directory
pwd
# Should be: **/team-project-cmpe202-03-fall2025-commandlinecommando
```

### **Step 2: Navigate to Backend Directory**

```bash
cd backend
```

### **Step 3: Set Up Environment Variables (Optional for Tests)**

The tests should work with default values, but for completeness:

```bash
# Copy the environment template
cp env.example .env

# Edit with your values (optional for tests)
nano .env
```

**Minimal .env for testing:**
```bash
# Database (tests use H2 in-memory by default)
DB_USERNAME=test
DB_PASSWORD=test

# JWT (tests use default values)
JWT_SECRET=test-secret-key-for-testing-only

# Application
SPRING_PROFILES_ACTIVE=test
```

### **Step 4: Clean and Compile**

```bash
# Clean previous builds
./mvnw clean

# Compile the project
./mvnw compile
```

### **Step 5: Run All Tests**

```bash
# Run all tests with test profile (RECOMMENDED)
./mvnw test -Dspring.profiles.active=test

# Or run all tests (may have issues with rate limiting)
./mvnw test
```

## 🎯 **Running Specific Test Categories**

### **Option 1: Run All Tests (Recommended)**
```bash
./mvnw test -Dspring.profiles.active=test
```

### **Option 2: Run Specific Test Classes**

#### **Authentication Service Tests**
```bash
./mvnw test -Dtest=AuthServiceTest -Dspring.profiles.active=test
```

#### **Authentication Controller Tests**
```bash
./mvnw test -Dtest=AuthControllerTest -Dspring.profiles.active=test
```

#### **JWT Utility Tests**
```bash
./mvnw test -Dtest=JwtUtilTest -Dspring.profiles.active=test
```

#### **Role-Based Access Tests**
```bash
./mvnw test -Dtest=RoleBasedAccessTest -Dspring.profiles.active=test
```

#### **Application Context Tests**
```bash
./mvnw test -Dtest=CampusmarketplaceApplicationTests -Dspring.profiles.active=test
```

### **Option 3: Run Tests with Verbose Output**
```bash
./mvnw test -Dtest=AuthServiceTest -Dspring.profiles.active=test -X
```

### **Option 4: Run Tests with Specific Profile**
```bash
./mvnw test -Dspring.profiles.active=test
```

## 📊 **Expected Test Results**

### **AuthServiceTest (13 tests)**
- ✅ `testSuccessfulLogin()`
- ✅ `testFailedLogin()`
- ✅ `testLoginWithInactiveUser()`
- ✅ `testSuccessfulRefreshToken()`
- ✅ `testRefreshTokenWithInvalidToken()`
- ✅ `testRefreshTokenNotFound()`
- ✅ `testRefreshTokenExpired()`
- ✅ `testLogout()`
- ✅ `testLogoutAllDevices()`
- ✅ `testGetCurrentUser()`
- ✅ `testGetCurrentUserNotFound()`

### **AuthControllerTest (13 tests)**
- ✅ `testSuccessfulLogin()`
- ✅ `testFailedLogin()`
- ✅ `testLoginWithInvalidInput()`
- ✅ `testSuccessfulRefreshToken()`
- ✅ `testFailedRefreshToken()`
- ✅ `testLogout()`
- ✅ `testLogoutAllDevices()`
- ✅ `testLogoutAllDevicesWithoutAuth()`
- ✅ `testGetCurrentUser()`
- ✅ `testGetCurrentUserWithoutAuth()`
- ✅ `testValidateToken()`
- ✅ `testValidateTokenWithoutAuth()`

### **JwtUtilTest**
- JWT token generation and validation tests

### **RoleBasedAccessTest**
- Role-based authorization tests

## 🔧 **Troubleshooting Common Issues**

### **Issue 1: Maven Wrapper Not Executable**
```bash
# Fix permissions
chmod +x mvnw
```

### **Issue 2: Java Version Mismatch**
```bash
# Check Java version
java -version

# If not Java 21+, install correct version
# On macOS with Homebrew:
brew install openjdk@21

# Set JAVA_HOME
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

### **Issue 3: Test Failures Due to Dependencies**
```bash
# Clean and reinstall dependencies
./mvnw clean install -DskipTests
./mvnw test
```

### **Issue 4: Port Already in Use**
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process if needed
kill -9 <PID>
```

### **Issue 5: Database Connection Issues**
```bash
# Tests should use H2 in-memory database by default
# If you see database errors, check application-test.properties
cat src/test/resources/application-test.properties
```

## 📈 **Test Execution Examples**

### **Example 1: Run All Tests with Detailed Output**
```bash
./mvnw test -Dspring.profiles.active=test -X
```

**Expected Output:**
```
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< com.commandlinecommandos:campusmarketplace >--------------------
[INFO] Building campusmarketplace 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-surefire-plugin:3.2.5:test (default-test) @ campusmarketplace ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.commandlinecommandos.campusmarketplace.CampusmarketplaceApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.5 s - in com.commandlinecommandos.campusmarketplace.CampusmarketplaceApplicationTests
[INFO] Running com.commandlinecommandos.campusmarketplace.controller.AuthControllerTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.2 s - in com.commandlinecommandos.campusmarketplace.controller.AuthControllerTest
[INFO] Running com.commandlinecommandos.campusmarketplace.service.AuthServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.8 s - in com.commandlinecommandos.campusmarketplace.service.AuthServiceTest
[INFO] Running com.commandlinecommandos.campusmarketplace.security.JwtUtilTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.2 s - in com.commandlinecommandos.campusmarketplace.security.JwtUtilTest
[INFO] Running com.commandlinecommandos.campusmarketplace.controller.RoleBasedAccessTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.1 s - in com.commandlinecommandos.campusmarketplace.controller.RoleBasedAccessTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### **Example 2: Run Specific Test with Debug Output**
```bash
./mvnw test -Dtest=AuthServiceTest#testSuccessfulLogin -X
```

### **Example 3: Run Tests and Generate Report**
```bash
./mvnw test -Dmaven.test.failure.ignore=true
./mvnw surefire-report:report
```

## 🎯 **Test Coverage Analysis**

### **Run Tests with Coverage Report**
```bash
# Add JaCoCo plugin to pom.xml first, then:
./mvnw clean test jacoco:report
```

### **View Coverage Report**
```bash
# Open the coverage report
open target/site/jacoco/index.html
```

## 🚀 **Advanced Test Execution**

### **Run Tests in Parallel**
```bash
./mvnw test -T 4
```

### **Run Tests with Specific JVM Arguments**
```bash
./mvnw test -Dmaven.surefire.debug
```

### **Run Tests and Skip Integration Tests**
```bash
./mvnw test -DskipITs
```

## 📝 **Test Configuration Files**

### **application-test.properties**
```properties
# Test-specific configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

## 🎉 **Success Criteria**

Your test setup is successful when you see:

1. **All tests pass** (40+ tests)
2. **No compilation errors**
3. **Clean test output** with proper formatting
4. **Test coverage report** generated (if configured)

## 🔍 **Debugging Failed Tests**

### **1. Check Test Logs**
```bash
./mvnw test -Dtest=AuthServiceTest -X | grep -A 10 -B 10 "FAILED"
```

### **2. Run Single Test Method**
```bash
./mvnw test -Dtest=AuthServiceTest#testSuccessfulLogin
```

### **3. Enable Debug Logging**
```bash
./mvnw test -Dlogging.level.com.commandlinecommandos.campusmarketplace=DEBUG
```

## 📚 **Additional Resources**

- **Maven Surefire Plugin**: https://maven.apache.org/surefire/maven-surefire-plugin/
- **JUnit 5 User Guide**: https://junit.org/junit5/docs/current/user-guide/
- **Spring Boot Testing**: https://spring.io/guides/gs/testing-web/

## ✅ **Quick Start Commands**

```bash
# 1. Navigate to backend
cd backend

# 2. Clean and compile
./mvnw clean compile

# 3. Run all tests (RECOMMENDED)
./mvnw test -Dspring.profiles.active=test

# 4. Run specific test class
./mvnw test -Dtest=AuthServiceTest -Dspring.profiles.active=test

# 5. Run with verbose output
./mvnw test -Dspring.profiles.active=test -X
```

**That's it! You should now be able to run all of Vineet's test cases successfully.** 🎉
