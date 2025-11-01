# Campus Marketplace - Backend Integration Guide

## 🎯 Database & Backend Alignment

This document outlines how the PostgreSQL database schema integrates with the Spring Boot backend for the Campus Marketplace (Facebook Marketplace clone for SJSU).

---

## 📊 Schema Overview

### Complete Entity Mapping

| PostgreSQL Table | JPA Entity | Repository | Status |
|------------------|------------|------------|--------|
| `universities` | `University.java` | `UniversityRepository` | ✅ Complete |
| `users` | `User.java` | `UserRepository` | ✅ Complete |
| `refresh_tokens` | `RefreshToken.java` | `RefreshTokenRepository` | ✅ Updated |
| `products` | `Product.java` | `ProductRepository` | ✅ Complete |
| `orders` | `Order.java` | `OrderRepository` | ✅ Complete |
| `order_items` | `OrderItem.java` | `OrderItemRepository` | ✅ Complete |

---

## 🔄 Major Changes from Original Backend

### 1. **ID Strategy: Long → UUID**

**Before:**
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE)
private Long userId;
```

**After (Aligned with PostgreSQL):**
```java
@Id
@GeneratedValue(generator = "UUID")
@Column(name = "user_id", updatable = false, nullable = false)
private UUID userId;
```

**Reason:** UUID provides better distributed system support and matches the PostgreSQL schema design.

### 2. **User Roles: STUDENT/ADMIN → BUYER/SELLER/ADMIN**

**Before:**
```java
public enum UserRole {
    STUDENT,
    ADMIN
}
```

**After (Campus Marketplace roles):**
```java
public enum UserRole {
    BUYER,    // Students purchasing items
    SELLER,   // Students listing items for sale
    ADMIN     // Platform administrators
}
```

**Reason:** Better aligns with marketplace functionality. Students can be both buyers and sellers.

### 3. **Removed Single Table Inheritance**

**Before:**
```java
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public class User { ... }

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User { ... }

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User { ... }
```

**After (Unified User Entity):**
```java
@Entity
@Table(name = "users")
public class User {
    @Enumerated(EnumType.STRING)
    private UserRole role;  // BUYER, SELLER, or ADMIN
    
    // All fields in one entity
    private String studentId;
    private String major;
    private Integer graduationYear;
    // ...
}
```

**Reason:** Simpler design, matches PostgreSQL schema, easier querying.

### 4. **Added University Multi-Tenancy**

**New Feature:**
```java
@Entity
@Table(name = "universities")
public class University {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID universityId;
    
    private String name;
    private String domain;  // e.g., "sjsu.edu"
    // ...
}

// In User.java
@ManyToOne
@JoinColumn(name = "university_id")
private University university;
```

**Reason:** Supports multiple universities (SJSU, Berkeley, Stanford, etc.) with data isolation.

### 5. **JSONB Support for Flexible Attributes**

**New Dependency (pom.xml):**
```xml
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.7.3</version>
</dependency>
```

**Usage in Entities:**
```java
@Type(JsonType.class)
@Column(name = "attributes", columnDefinition = "jsonb")
private Map<String, Object> attributes;

// Example for Product:
// {"isbn": "978-0134462066", "author": "Smith", "edition": "5th"}
```

**Reason:** Allows category-specific data without schema changes (textbook ISBN, electronics specs, etc.).

---

## 🚀 Getting Started

### Step 1: Database Setup

```bash
# Navigate to database directory
cd db

# Run PostgreSQL setup
./setup-database.sh

# Apply Flyway migrations
cd migrations
flyway migrate
```

### Step 2: Configure Spring Boot

**Environment Variables (.env or application.yml):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/campusmarketplace_db
    username: cm_app_user
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-generate schema, use Flyway
```

### Step 3: Run the Application

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

---

## 📋 Entity Details

### University Entity

```java
@Entity
@Table(name = "universities")
public class University {
    private UUID universityId;
    private String name;
    private String domain;  // "sjsu.edu"
    private String city;
    private String state;
    private boolean isActive;
    private Map<String, Object> settings;  // JSONB
}
```

**Key Methods:**
- `isValidDomain(String email)` - Validates university email

### User Entity

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    private UUID userId;
    private University university;  // Multi-tenant support
    private String username;
    private String email;
    private String password;
    private UserRole role;  // BUYER, SELLER, ADMIN
    private VerificationStatus verificationStatus;
    private String studentId;
    private String major;
    private Integer graduationYear;
    private Map<String, Object> preferences;  // JSONB
}
```

**Key Methods:**
- `isSeller()`, `isBuyer()`, `isAdmin()` - Role checking
- `verifyEmail()` - Mark email as verified
- `recordLogin()` - Update last login timestamp

### Product Entity

```java
@Entity
@Table(name = "products")
public class Product {
    private UUID productId;
    private User seller;
    private University university;
    private String title;
    private String description;
    private ProductCategory category;  // TEXTBOOKS, ELECTRONICS, etc.
    private ProductCondition condition;  // NEW, LIKE_NEW, GOOD, FAIR
    private BigDecimal price;
    private Integer quantity;
    private Map<String, Object> attributes;  // JSONB for category-specific data
    private ModerationStatus moderationStatus;
}
```

**Key Methods:**
- `incrementViewCount()` - Track product views
- `decrementQuantity(int amount)` - Handle inventory
- `isAvailable()` - Check if product can be purchased

### Order Entity

```java
@Entity
@Table(name = "orders")
public class Order {
    private UUID orderId;
    private User buyer;
    private University university;
    private String orderNumber;  // Human-readable (ORD-20250108-000123)
    private OrderStatus status;  // CART, PENDING_PAYMENT, PAID, etc.
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private DeliveryMethod deliveryMethod;
    private List<OrderItem> orderItems;
}
```

**Order Lifecycle:**
```
CART → PENDING_PAYMENT → PAID → PROCESSING → SHIPPED → DELIVERED → COMPLETED
```

**Key Methods:**
- `addItem(OrderItem item)` - Add product to cart
- `placeOrder()` - Convert cart to order
- `markAsPaid()` - Confirm payment
- `recalculateTotal()` - Update pricing

### OrderItem Entity

```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    private UUID orderItemId;
    private Order order;
    private Product product;
    private User seller;
    private String productTitle;  // Snapshot at purchase time
    private ProductCondition productCondition;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
```

**Reason for Snapshots:** Preserves historical data even if product is deleted or modified.

---

## 🔍 Repository Query Examples

### UserRepository

```java
// Find by username
Optional<User> user = userRepository.findByUsername("alice_buyer");

// Find sellers at SJSU
List<User> sellers = userRepository.findByUniversityAndRole(
    sjsuUniversity, 
    UserRole.SELLER
);

// Check if email exists
boolean exists = userRepository.existsByEmail("bob@sjsu.edu");
```

### ProductRepository

```java
// Search products
Page<Product> products = productRepository.searchProducts(
    sjsuUniversity,
    "macbook",
    PageRequest.of(0, 20)
);

// Find by category
Page<Product> textbooks = productRepository.findByUniversityAndCategoryAndIsActiveTrueAndModerationStatus(
    sjsuUniversity,
    ProductCategory.TEXTBOOKS,
    ModerationStatus.APPROVED,
    pageable
);

// Find seller's products
List<Product> myProducts = productRepository.findBySellerAndIsActiveTrue(currentUser);
```

### OrderRepository

```java
// Get buyer's cart
Optional<Order> cart = orderRepository.findByBuyerAndStatus(
    currentUser,
    OrderStatus.CART
);

// Get order history
Page<Order> history = orderRepository.findOrderHistory(
    currentUser,
    pageable
);

// Find order by order number
Optional<Order> order = orderRepository.findByOrderNumber("ORD-20250108-000123");
```

---

## 🔐 Security Considerations

### Row-Level Security (RLS) in PostgreSQL

**Database-level isolation:**
```sql
CREATE POLICY university_isolation_users ON users
    FOR ALL
    USING (university_id = current_setting('app.university_id')::UUID);
```

**Application-level (Spring Boot):**
```java
@Service
public class UniversityContextService {
    
    public void setUniversityContext(UUID universityId) {
        // Set session variable for RLS
        jdbcTemplate.execute(
            "SET app.university_id = '" + universityId + "'"
        );
    }
}
```

### Authentication Flow

1. User logs in with email (e.g., `alice@sjsu.edu`)
2. Extract university domain (`sjsu.edu`)
3. Lookup `University` by domain
4. Set university context for RLS
5. Generate JWT with university claim
6. All queries automatically filtered by university

---

## 🧪 Testing

### Test Data Setup

```java
@SpringBootTest
@Transactional
public class ProductServiceTest {
    
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        // Create university
        University sjsu = new University("San Jose State University", "sjsu.edu", "San Jose", "CA");
        universityRepository.save(sjsu);
        
        // Create seller
        User seller = new User("john_seller", "john@sjsu.edu", "password", UserRole.SELLER, sjsu);
        seller.setFirstName("John");
        seller.setLastName("Doe");
        userRepository.save(seller);
        
        // Create product
        Product product = new Product(seller, "MacBook Pro", "Like new condition", 
            ProductCategory.ELECTRONICS, ProductCondition.LIKE_NEW, new BigDecimal("750.00"));
        productRepository.save(product);
    }
    
    @Test
    void testProductCreation() {
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getTitle()).isEqualTo("MacBook Pro");
    }
}
```

---

## 🚨 Migration Checklist

### For Existing Codebase

- [x] Update `UserRole` enum (STUDENT → BUYER/SELLER)
- [x] Change ID types from `Long` to `UUID`
- [x] Remove `Student` and `Admin` entities
- [x] Add `University` entity
- [x] Add `Product`, `Order`, `OrderItem` entities
- [x] Update all repositories to use UUID
- [x] Add Hypersistence Utils dependency for JSONB
- [x] Update `application.yml` to use PostgreSQL by default
- [ ] Update `AuthService` to handle new User structure
- [ ] Update `RegisterRequest` DTO to include university
- [ ] Update controllers to work with UUID
- [ ] Update JWT claims to include university ID
- [ ] Add integration tests for new entities

---

## 📚 API Endpoint Examples

### User Registration

```bash
POST /api/auth/register
{
    "username": "alice_buyer",
    "email": "alice@sjsu.edu",  # University auto-detected from domain
    "password": "SecurePass123!",
    "firstName": "Alice",
    "lastName": "Johnson",
    "role": "BUYER",
    "studentId": "BUY001"
}
```

### Create Product Listing

```bash
POST /api/products
Authorization: Bearer {token}
{
    "title": "Data Structures Textbook - 6th Edition",
    "description": "Like new, minimal highlighting",
    "category": "TEXTBOOKS",
    "condition": "LIKE_NEW",
    "price": 45.00,
    "quantity": 1,
    "negotiable": true,
    "attributes": {
        "isbn": "978-0134462066",
        "author": "Michael T. Goodrich",
        "course": "CMPE 146"
    },
    "pickupLocation": "Engineering Building, Room 285"
}
```

### Search Products

```bash
GET /api/products/search?q=macbook&category=ELECTRONICS&maxPrice=1000&page=0&size=20
Authorization: Bearer {token}
```

### Add to Cart

```bash
POST /api/cart/items
Authorization: Bearer {token}
{
    "productId": "uuid-here",
    "quantity": 1
}
```

---

## 🎯 Next Steps

1. **Run Migrations**: Apply Flyway migrations to create database schema
2. **Update Services**: Modify `AuthService` and related services for UUID and new User structure
3. **Update DTOs**: Create request/response DTOs for new entities
4. **Add Controllers**: Create REST controllers for Products, Orders
5. **Integration Tests**: Write tests for complete user journeys
6. **Documentation**: Generate Swagger/OpenAPI docs

---

## 📖 Related Documentation

- **[Database Schema Design](./SCHEMA_DESIGN.md)** - Complete schema documentation
- **[Demo Day Guide](./DEMO_DAY_GUIDE.md)** - Presentation guide
- **[Database Setup](./DATABASE_SETUP.md)** - Installation instructions
- **[Migration README](../migrations/README.md)** - Flyway migration guide

---

**Last Updated:** January 8, 2025  
**Version:** 1.0.0  
**Team:** Commandline Commandos  
**Project:** CMPE 202 Campus Marketplace

