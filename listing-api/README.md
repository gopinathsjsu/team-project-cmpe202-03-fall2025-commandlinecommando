# Listing API

A Spring Boot microservice for managing campus marketplace listings with image upload capabilities.

## Overview

The Listing API is part of the Campus Marketplace application, providing comprehensive functionality for managing product listings including:

- Creating, updating, and deleting listings
- Advanced search and filtering capabilities
- Image upload and management
- Listing status management (Active, Sold, Cancelled)
- Seller-specific listing management
- View count tracking
- Report Management System - Complete reporting and moderation functionality
- Report Management System - Complete reporting and moderation functionality

## Features

### Core Listing Management
- **CRUD Operations**: Full create, read, update, delete functionality for listings
- **Status Management**: Track listing lifecycle (Pending, Active, Sold, Cancelled)
- **Seller Management**: Listings are associated with seller IDs for ownership control
- **View Tracking**: Automatic view count increment for active listings

### Search & Filtering
- **Text Search**: Search by title and description keywords
- **Category Filtering**: Filter by predefined categories (Textbooks, Gadgets, Electronics, Stationary, Other)
- **Condition Filtering**: Filter by item condition (New, Like New, Good, Used)
- **Price Range**: Filter by minimum and maximum price
- **Location Filtering**: Filter by location
- **Status Filtering**: Filter by listing status
- **Pagination**: Configurable page size and sorting options

### Image Management
- **Multiple Image Upload**: Support for uploading multiple images per listing
- **Image Organization**: Display order management for images
- **File Storage**: Secure file storage with validation
- **Image Retrieval**: Get all images associated with a listing

### Report Management System
- **Report Creation**: Users can report inappropriate listings, spam, fake listings, etc.
- **Report Types**: Support for multiple report categories (Inappropriate Content, Spam, Fake Listing, Harassment, Copyright Violation, Other)
- **Status Tracking**: Complete lifecycle management (Pending, Under Review, Resolved, Dismissed)
- **Admin Moderation**: Administrative tools for reviewing and managing reports
- **Advanced Search**: Filter reports by status, type, reporter, listing, and reviewer
- **Dashboard Statistics**: Report counts and analytics for admin dashboards

### Data Models

#### Listing
- `listingId`: Unique identifier
- `sellerId`: ID of the listing owner
- `title`: Listing title (2-255 characters)
- `description`: Detailed description (10-1000 characters)
- `category`: Product category enum
- `price`: Item price (BigDecimal)
- `condition`: Item condition enum
- `status`: Current listing status
- `location`: Item location
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `viewCount`: Number of times viewed
- `images`: Associated images

#### Report
- `reportId`: Unique identifier
- `reporterId`: ID of the user who submitted the report
- `listingId`: ID of the reported listing
- `reportType`: Type of report (enum)
- `description`: Detailed description of the issue
- `status`: Current report status (enum)
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `reviewedBy`: ID of the admin who reviewed the report
- `reviewedAt`: Timestamp when report was reviewed

#### Report
- `reportId`: Unique identifier
- `reporterId`: ID of the user who submitted the report
- `listingId`: ID of the reported listing
- `reportType`: Type of report (enum)
- `description`: Detailed description of the issue
- `status`: Current report status (enum)
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `reviewedBy`: ID of the admin who reviewed the report
- `reviewedAt`: Timestamp when report was reviewed

#### Enums
- **Category**: TEXTBOOKS, GADGETS, ELECTRONICS, STATIONARY, OTHER
- **ItemCondition**: NEW, LIKE_NEW, GOOD, USED
- **ListingStatus**: PENDING, ACTIVE, SOLD, CANCELLED
- **ReportType**: INAPPROPRIATE_CONTENT, SPAM, FAKE_LISTING, HARASSMENT, COPYRIGHT_VIOLATION, OTHER
- **ReportStatus**: PENDING, UNDER_REVIEW, RESOLVED, DISMISSED
- **ReportType**: INAPPROPRIATE_CONTENT, SPAM, FAKE_LISTING, HARASSMENT, COPYRIGHT_VIOLATION, OTHER
- **ReportStatus**: PENDING, UNDER_REVIEW, RESOLVED, DISMISSED

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL (production), H2 (development)
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Validation
- **Session Management**: Spring Session JDBC
- **REST**: Spring Web, Spring HATEOAS, Spring Data REST

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.6+
- PostgreSQL (for production)
- H2 Database (for development)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd listing-api
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   # or
   make run
   ```

The API will be available at `http://localhost:8100`

### Configuration

#### Database Configuration
The application supports both H2 (development) and PostgreSQL (production) databases:

**H2 (Default - Development)**
- Database: In-memory H2 database
- Console: Available at `/h2-console`
- Auto-configuration enabled

**PostgreSQL (Production)**
- Update `application.yml` with PostgreSQL connection details
- Ensure PostgreSQL is running and accessible

#### Application Properties
Key configuration options in `application.yml`:
```yaml
server:
  port: 8100

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console
```

## API Documentation

### Base URL
```
http://localhost:8100/api
```

### Authentication
Currently, the API uses placeholder authentication. In production, integrate with the main authentication service to retrieve actual user IDs.

### Error Handling
All API endpoints return consistent error responses using the global exception handler. See the [API Documentation](API_DOCUMENTATION.md#error-responses) for detailed error response formats and examples.

### Endpoints

#### Listing Management

**Get All Listings**
```
GET /api/listings
```
Parameters:
- `page` (default: 0): Page number
- `size` (default: 20): Page size
- `sortBy` (default: createdAt): Sort field
- `sortDirection` (default: desc): Sort direction (asc/desc)

**Search Listings**
```
GET /api/listings/search
```
Parameters:
- `status`: Listing status filter
- `keyword`: Text search in title/description
- `category`: Category filter
- `condition`: Item condition filter
- `minPrice`: Minimum price filter
- `maxPrice`: Maximum price filter
- `location`: Location filter
- `page`, `size`, `sortBy`, `sortDirection`: Pagination options

**Get Listing by ID**
```
GET /api/listings/{listingId}
```

**Get Listings by Seller**
```
GET /api/listings/seller/{sellerId}
```

**Create Listing**
```
POST /api/listings/
```
Request Body:
```json
{
  "title": "MacBook Pro 13-inch",
  "description": "Excellent condition MacBook Pro, barely used",
  "price": 1200.00,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "location": "San Jose, CA"
}
```

**Update Listing**
```
PUT /api/listings/{listingId}
```

**Mark as Sold**
```
PUT /api/listings/{listingId}/sold
```

**Cancel Listing**
```
PUT /api/listings/{listingId}/cancel
```

**Delete Listing**
```
DELETE /api/listings/{listingId}
```

#### Image Management

**Upload Single Image**
```
POST /api/files/upload/{listingId}
```
Form Data:
- `file`: Image file
- `displayOrder`: Display order (integer)

**Upload Multiple Images**
```
POST /api/files/upload-multiple/{listingId}
```
Form Data:
- `files`: Array of image files
- `displayOrders`: Array of display orders

**Get Listing Images**
```
GET /api/files/listing/{listingId}
```

**Delete Image**
```
DELETE /api/files/listing/{listingId}/{imageId}
```

#### Report Management

**Get All Reports**
```
GET /api/reports
```
Parameters: `page`, `size`, `sortBy`, `sortDirection`

**Search Reports**
```
GET /api/reports/search
```
Parameters: `status`, `reporterId`, `listingId`, `reportType`, `reviewedBy`, pagination options

**Get Pending Reports**
```
GET /api/reports/pending
```

**Get Reports by Reporter**
```
GET /api/reports/reporter/{reporterId}
```

**Get Reports by Listing**
```
GET /api/reports/listing/{listingId}
```

**Get Reports by Type**
```
GET /api/reports/type/{reportType}
```

**Get Reports by Status**
```
GET /api/reports/status/{status}
```

**Get Report by ID**
```
GET /api/reports/{reportId}
```

**Create Report**
```
POST /api/reports/
```
Request Body:
```json
{
  "reporterId": 123,
  "listingId": 456,
  "reportType": "INAPPROPRIATE_CONTENT",
  "description": "Description of the issue"
}
```

**Update Report**
```
PUT /api/reports/{reportId}
```

**Mark Report as Reviewed**
```
PUT /api/reports/{reportId}/review
```

**Mark Report as Resolved**
```
PUT /api/reports/{reportId}/resolve
```

**Mark Report as Dismissed**
```
PUT /api/reports/{reportId}/dismiss
```

**Delete Report**
```
DELETE /api/reports/{reportId}
```

**Get Report Counts**
```
GET /api/reports/count
```

#### Report Management

**Get All Reports**
```
GET /api/reports
```
Parameters: `page`, `size`, `sortBy`, `sortDirection`

**Search Reports**
```
GET /api/reports/search
```
Parameters: `status`, `reporterId`, `listingId`, `reportType`, `reviewedBy`, pagination options

**Get Pending Reports**
```
GET /api/reports/pending
```

**Get Reports by Reporter**
```
GET /api/reports/reporter/{reporterId}
```

**Get Reports by Listing**
```
GET /api/reports/listing/{listingId}
```

**Get Reports by Type**
```
GET /api/reports/type/{reportType}
```

**Get Reports by Status**
```
GET /api/reports/status/{status}
```

**Get Report by ID**
```
GET /api/reports/{reportId}
```

**Create Report**
```
POST /api/reports/
```
Request Body:
```json
{
  "reporterId": 123,
  "listingId": 456,
  "reportType": "INAPPROPRIATE_CONTENT",
  "description": "Description of the issue"
}
```

**Update Report**
```
PUT /api/reports/{reportId}
```

**Mark Report as Reviewed**
```
PUT /api/reports/{reportId}/review
```

**Mark Report as Resolved**
```
PUT /api/reports/{reportId}/resolve
```

**Mark Report as Dismissed**
```
PUT /api/reports/{reportId}/dismiss
```

**Delete Report**
```
DELETE /api/reports/{reportId}
```

**Get Report Counts**
```
GET /api/reports/count
```

## Development

### Project Structure
```
src/main/java/com/commandlinecommandos/listingapi/
├── controller/          # REST controllers
│   ├── ListingController.java
│   ├── FileUploadController.java
│   ├── ReportController.java
│   └── TestController.java
│   ├── ListingController.java
│   ├── FileUploadController.java
│   ├── ReportController.java
│   └── TestController.java
├── model/              # Entity models and enums
│   ├── Listing.java
│   ├── ListingImage.java
│   ├── Report.java
│   └── enums/
│   ├── Listing.java
│   ├── ListingImage.java
│   ├── Report.java
│   └── enums/
├── repository/         # Data access layer
│   ├── ListingRepository.java
│   └── ReportRepository.java
│   ├── ListingRepository.java
│   └── ReportRepository.java
├── service/            # Business logic layer
│   ├── ListingService.java
│   ├── FileStorageService.java
│   └── ReportService.java
│   ├── ListingService.java
│   ├── FileStorageService.java
│   └── ReportService.java
├── exception/          # Custom exceptions
└── ListingApiApplication.java
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ListingControllerTest

# Run with coverage
mvn test jacoco:report
```

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Maintain consistent indentation (4 spaces)

## Deployment

### Production Deployment
1. Configure PostgreSQL database
2. Update application.yml with production settings
3. Set environment variables for sensitive data
4. Build and deploy using your preferred method (Docker, traditional deployment, etc.)

### Docker Deployment
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/listingapi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8100
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Integration

### Database Integration
The listing-api integrates with the main campus marketplace database schema:
- Uses the `listings` table for listing management
- Uses the `listings` table for listing management
- References `listing_images` table for image storage
- Uses the `reports` table for report management
- Uses the `reports` table for report management
- Compatible with existing database migrations

### Authentication Integration
Currently uses placeholder authentication. To integrate with the main auth service:
1. Remove placeholder seller ID assignments
2. Implement proper user authentication
3. Extract user ID from authenticated session/token

## Troubleshooting

### Common Issues

**Database Connection Issues**
- Verify database is running
- Check connection URL and credentials
- Ensure database schema exists

**File Upload Issues**
- Verify file upload directory permissions
- Check file size limits
- Ensure supported file formats

**Port Already in Use**
- Change port in application.yml
- Kill existing process on port 8100

### Logs
Application logs are available in the console. For production, configure proper logging:
```yaml
logging:
  level:
    com.commandlinecommandos.listingapi: DEBUG
    org.springframework.web: DEBUG
```

## Contributing

1. Follow the existing code structure and patterns
2. Add tests for new functionality
3. Update documentation for API changes
4. Ensure all tests pass before submitting PR

## Documentation

Comprehensive documentation is available for different aspects of the Listing API:

- **[API Documentation](API_DOCUMENTATION.md)** - Detailed endpoint descriptions, request/response formats, and usage examples
- **[Architecture Documentation](ARCHITECTURE.md)** - Technical architecture, design patterns, and component structure
- **[Deployment Guide](DEPLOYMENT.md)** - Complete deployment instructions for development, staging, and production environments
- **[Quick Reference](QUICK_REFERENCE.md)** - Essential commands, configurations, and troubleshooting for developers

## License

This project is part of the CMPE-202 Campus Marketplace application.

## Support

For issues and questions, please refer to the main project documentation or contact the development team.