package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service to initialize dummy data for development
 * Only runs in dev profile with H2 database
 */
@Service
@Order(1)
public class DataInitializationService implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private org.springframework.core.env.Environment environment;
    
    @Override
    @Transactional
    public void run(String... args) {
        // Only initialize data in dev profile with H2
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        if (!"dev".equals(activeProfile)) {
            return;
        }
        
        // Check if data already exists
        if (productRepository.count() > 0) {
            return; // Data already initialized
        }
        
        // Get or create a test user
        User testUser = userRepository.findByUsername("student")
            .orElseGet(() -> {
                User user = new User();
                user.setUsername("student");
                user.setEmail("student@test.com");
                user.setPassword("$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN"); // password123
                user.setFirstName("Test");
                user.setLastName("Student");
                // Students get both BUYER and SELLER roles (many-to-many)
                user.setRoles(new HashSet<>(Set.of(UserRole.BUYER, UserRole.SELLER)));
                user.setActive(true);
                // University can be null for H2
                return userRepository.save(user);
            });
        
        // Create dummy products
        createDummyProducts(testUser);
    }
    
    private void createDummyProducts(User seller) {
        // Textbook 1
        Product p1 = new Product();
        p1.setSeller(seller);
        p1.setUniversity(seller.getUniversity()); // Can be null
        p1.setTitle("Data Structures and Algorithms in Java - 6th Edition");
        p1.setDescription("Excellent condition textbook for CMPE 146. All chapters intact, minimal highlighting. Perfect for CS students.");
        p1.setCategory(ProductCategory.TEXTBOOKS);
        p1.setCondition(ProductCondition.LIKE_NEW);
        p1.setPrice(new BigDecimal("45.00"));
        p1.setOriginalPrice(new BigDecimal("120.00"));
        p1.setNegotiable(true);
        p1.setQuantity(1);
        p1.setActive(true);
        p1.setModerationStatus(ModerationStatus.APPROVED);
        p1.setPickupLocation("Engineering Building, Room 285");
        p1.setPublishedAt(LocalDateTime.now().minusDays(5));
        p1.setViewCount(23);
        Map<String, Object> attrs1 = new HashMap<>();
        attrs1.put("isbn", "978-0134462066");
        attrs1.put("author", "Michael T. Goodrich");
        attrs1.put("edition", "6th");
        p1.setAttributes(attrs1);
        productRepository.save(p1);
        
        // Textbook 2
        Product p2 = new Product();
        p2.setSeller(seller);
        p2.setUniversity(seller.getUniversity());
        p2.setTitle("Calculus: Early Transcendentals - 8th Edition");
        p2.setDescription("Used for Math 30. Good condition with some notes in margins. Solutions manual included!");
        p2.setCategory(ProductCategory.TEXTBOOKS);
        p2.setCondition(ProductCondition.GOOD);
        p2.setPrice(new BigDecimal("35.00"));
        p2.setOriginalPrice(new BigDecimal("95.00"));
        p2.setNegotiable(true);
        p2.setQuantity(1);
        p2.setActive(true);
        p2.setModerationStatus(ModerationStatus.APPROVED);
        p2.setPickupLocation("MLK Library, 3rd Floor");
        p2.setPublishedAt(LocalDateTime.now().minusDays(3));
        p2.setViewCount(15);
        Map<String, Object> attrs2 = new HashMap<>();
        attrs2.put("isbn", "978-1285741550");
        attrs2.put("author", "James Stewart");
        attrs2.put("edition", "8th");
        p2.setAttributes(attrs2);
        productRepository.save(p2);
        
        // Electronics 1
        Product p3 = new Product();
        p3.setSeller(seller);
        p3.setUniversity(seller.getUniversity());
        p3.setTitle("MacBook Pro 13\" M1 2020 - 8GB RAM 256GB SSD");
        p3.setDescription("Gently used MacBook Pro in excellent condition. Perfect for CS students. Includes charger and original box. Battery health 92%.");
        p3.setCategory(ProductCategory.ELECTRONICS);
        p3.setCondition(ProductCondition.LIKE_NEW);
        p3.setPrice(new BigDecimal("750.00"));
        p3.setOriginalPrice(new BigDecimal("1299.00"));
        p3.setNegotiable(true);
        p3.setQuantity(1);
        p3.setActive(true);
        p3.setModerationStatus(ModerationStatus.APPROVED);
        p3.setPickupLocation("Student Union, 2nd Floor Lounge");
        p3.setPublishedAt(LocalDateTime.now().minusDays(2));
        p3.setViewCount(67);
        Map<String, Object> attrs3 = new HashMap<>();
        attrs3.put("brand", "Apple");
        attrs3.put("model", "MacBook Pro 13-inch");
        attrs3.put("processor", "Apple M1");
        attrs3.put("ram", "8GB");
        attrs3.put("storage", "256GB SSD");
        p3.setAttributes(attrs3);
        productRepository.save(p3);
        
        // Electronics 2
        Product p4 = new Product();
        p4.setSeller(seller);
        p4.setUniversity(seller.getUniversity());
        p4.setTitle("TI-84 Plus CE Graphing Calculator - Blue");
        p4.setDescription("Barely used graphing calculator. Required for Math and Engineering courses. Includes USB cable.");
        p4.setCategory(ProductCategory.ELECTRONICS);
        p4.setCondition(ProductCondition.LIKE_NEW);
        p4.setPrice(new BigDecimal("80.00"));
        p4.setOriginalPrice(new BigDecimal("140.00"));
        p4.setNegotiable(false);
        p4.setQuantity(1);
        p4.setActive(true);
        p4.setModerationStatus(ModerationStatus.APPROVED);
        p4.setPickupLocation("Engineering Building Lobby");
        p4.setPublishedAt(LocalDateTime.now().minusDays(1));
        p4.setViewCount(12);
        Map<String, Object> attrs4 = new HashMap<>();
        attrs4.put("brand", "Texas Instruments");
        attrs4.put("model", "TI-84 Plus CE");
        attrs4.put("color", "Blue");
        p4.setAttributes(attrs4);
        productRepository.save(p4);
        
        // Furniture
        Product p5 = new Product();
        p5.setSeller(seller);
        p5.setUniversity(seller.getUniversity());
        p5.setTitle("IKEA Desk with Chair - Perfect for Dorm");
        p5.setDescription("Moving out sale! Compact desk and chair set perfect for dorm rooms. Easy to assemble/disassemble.");
        p5.setCategory(ProductCategory.FURNITURE);
        p5.setCondition(ProductCondition.GOOD);
        p5.setPrice(new BigDecimal("60.00"));
        p5.setOriginalPrice(new BigDecimal("150.00"));
        p5.setNegotiable(true);
        p5.setQuantity(1);
        p5.setActive(true);
        p5.setModerationStatus(ModerationStatus.APPROVED);
        p5.setPickupLocation("Campus Village Parking Lot");
        p5.setPublishedAt(LocalDateTime.now().minusDays(4));
        p5.setViewCount(8);
        Map<String, Object> attrs5 = new HashMap<>();
        attrs5.put("brand", "IKEA");
        attrs5.put("desk_dimensions", "47x24 inches");
        attrs5.put("chair_type", "Office chair with wheels");
        p5.setAttributes(attrs5);
        productRepository.save(p5);
        
        // More Electronics
        Product p6 = new Product();
        p6.setSeller(seller);
        p6.setUniversity(seller.getUniversity());
        p6.setTitle("iPad Air with Apple Pencil");
        p6.setDescription("iPad Air 4th generation 64GB with Apple Pencil 2nd gen. Space Gray. Great for note-taking and digital art.");
        p6.setCategory(ProductCategory.ELECTRONICS);
        p6.setCondition(ProductCondition.GOOD);
        p6.setPrice(new BigDecimal("450.00"));
        p6.setOriginalPrice(new BigDecimal("650.00"));
        p6.setNegotiable(true);
        p6.setQuantity(1);
        p6.setActive(true);
        p6.setModerationStatus(ModerationStatus.APPROVED);
        p6.setPickupLocation("Art Building - Studio 2");
        p6.setPublishedAt(LocalDateTime.now().minusHours(12));
        p6.setViewCount(34);
        productRepository.save(p6);
        
        // More Textbooks
        Product p7 = new Product();
        p7.setSeller(seller);
        p7.setUniversity(seller.getUniversity());
        p7.setTitle("Introduction to Algorithms - 4th Edition (CLRS)");
        p7.setDescription("The classic algorithms textbook. Excellent condition, no highlighting. Perfect for CMPE 146 and advanced CS courses.");
        p7.setCategory(ProductCategory.TEXTBOOKS);
        p7.setCondition(ProductCondition.LIKE_NEW);
        p7.setPrice(new BigDecimal("55.00"));
        p7.setOriginalPrice(new BigDecimal("150.00"));
        p7.setNegotiable(true);
        p7.setQuantity(1);
        p7.setActive(true);
        p7.setModerationStatus(ModerationStatus.APPROVED);
        p7.setPickupLocation("Engineering Building, Room 189");
        p7.setPublishedAt(LocalDateTime.now().minusDays(6));
        p7.setViewCount(19);
        productRepository.save(p7);
        
        // Clothing
        Product p8 = new Product();
        p8.setSeller(seller);
        p8.setUniversity(seller.getUniversity());
        p8.setTitle("SJSU Hoodie - Size Large");
        p8.setDescription("Official SJSU hoodie, barely worn. Great condition, perfect for showing school spirit!");
        p8.setCategory(ProductCategory.CLOTHING);
        p8.setCondition(ProductCondition.LIKE_NEW);
        p8.setPrice(new BigDecimal("25.00"));
        p8.setOriginalPrice(new BigDecimal("50.00"));
        p8.setNegotiable(false);
        p8.setQuantity(1);
        p8.setActive(true);
        p8.setModerationStatus(ModerationStatus.APPROVED);
        p8.setPickupLocation("Student Union");
        p8.setPublishedAt(LocalDateTime.now().minusDays(7));
        p8.setViewCount(5);
        productRepository.save(p8);
    }
}

