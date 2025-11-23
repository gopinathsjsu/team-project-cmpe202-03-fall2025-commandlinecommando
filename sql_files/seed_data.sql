-- seed_data.sql
-- Campus Marketplace Sample Data for Testing and Development
-- Aligned with updated schema using UUID primary keys
-- Generated: 2024-10-29

-- =====================================================
-- SEED DATA - UNIVERSITIES
-- =====================================================

-- Insert test universities
INSERT INTO universities (university_id, name, domain, city, state, country, is_active) VALUES
('00000000-0000-0000-0000-000000000001', 'San Jose State University', 'sjsu.edu', 'San Jose', 'California', 'USA', true),
('00000000-0000-0000-0000-000000000002', 'Stanford University', 'stanford.edu', 'Stanford', 'California', 'USA', true),
('00000000-0000-0000-0000-000000000003', 'University of California Berkeley', 'berkeley.edu', 'Berkeley', 'California', 'USA', true);

-- =====================================================
-- SEED DATA - USERS (Students and Admins)
-- =====================================================

-- Note: All passwords are hashed with BCrypt
-- Plain text password for all users: "password123"
-- BCrypt hash: $2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN

-- Insert test students at SJSU
INSERT INTO users (user_id, university_id, username, email, password_hash, first_name, last_name, phone, role, verification_status, student_id, university_email, major, graduation_year, is_active) VALUES
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000001', 'student', 'student@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'John', 'Student', '555-0101', 'STUDENT', 'VERIFIED', 'STU001', 'student@sjsu.edu', 'Computer Science', 2025, true),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000001', 'alice_chen', 'alice.chen@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Alice', 'Chen', '555-0102', 'STUDENT', 'VERIFIED', 'STU002', 'alice.chen@sjsu.edu', 'Electrical Engineering', 2024, true),
('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000001', 'bob_martinez', 'bob.martinez@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Bob', 'Martinez', '555-0103', 'STUDENT', 'VERIFIED', 'STU003', 'bob.martinez@sjsu.edu', 'Business Administration', 2026, true),
('00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000001', 'sarah_kim', 'sarah.kim@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Sarah', 'Kim', '555-0104', 'STUDENT', 'VERIFIED', 'STU004', 'sarah.kim@sjsu.edu', 'Mechanical Engineering', 2025, true),
('00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000001', 'mike_johnson', 'mike.johnson@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Mike', 'Johnson', '555-0105', 'STUDENT', 'PENDING', 'STU005', 'mike.johnson@sjsu.edu', 'Computer Science', 2024, true);

-- Insert test admins
INSERT INTO users (user_id, username, email, password_hash, first_name, last_name, phone, role, verification_status, is_active) VALUES
('00000000-0000-0000-0000-000000000201', 'admin', 'admin@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Jane', 'Admin', '555-0201', 'ADMIN', 'VERIFIED', true),
('00000000-0000-0000-0000-000000000202', 'super_admin', 'superadmin@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Super', 'Administrator', '555-0202', 'ADMIN', 'VERIFIED', true);

-- =====================================================
-- SEED DATA - REFRESH TOKENS
-- =====================================================

INSERT INTO refresh_tokens (id, token, user_id, expires_at, device_info, is_revoked) VALUES
('10000000-0000-0000-0000-000000000001', 'eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMDAwMDAwMDAtMDAwMC0wMDAwLTAwMDAtMDAwMDAwMDAwMTAxIiwic3ViIjoic3R1ZGVudCIsImlhdCI6MTcwNTMyMDAwMCwiZXhwIjoxNzA1OTI0ODAwfQ.sample_refresh_token_1', '00000000-0000-0000-0000-000000000101', '2026-12-22 10:00:00', 'iPhone Safari', false),
('10000000-0000-0000-0000-000000000002', 'eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMDAwMDAwMDAtMDAwMC0wMDAwLTAwMDAtMDAwMDAwMDAwMTAyIiwic3ViIjoiYWxpY2VfY2hlbiIsImlhdCI6MTcwNTMyMDAwMCwiZXhwIjoxNzA1OTI0ODAwfQ.sample_refresh_token_2', '00000000-0000-0000-0000-000000000102', '2026-12-22 11:30:00', 'Chrome Windows', false),
('10000000-0000-0000-0000-000000000003', 'revoked_token_example_abc123', '00000000-0000-0000-0000-000000000103', '2026-11-20 12:00:00', 'Chrome Mobile', true);

-- =====================================================
-- SEED DATA - PRODUCTS (Listings)
-- =====================================================

INSERT INTO products (product_id, seller_id, university_id, title, description, price, category, condition, quantity, moderation_status, view_count, is_active, pickup_location, published_at) VALUES
('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000001', 'MacBook Pro 13" 2021', 'Excellent condition MacBook Pro with M1 chip. Used for one semester only. Includes original charger and box. Perfect for CS students!', 1200.00, 'ELECTRONICS', 'LIKE_NEW', 1, 'APPROVED', 45, true, 'SJSU Library - Main Entrance', '2024-10-20 10:00:00'),

('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000001', 'Calculus Textbook - Stewart 8th Edition', 'Calculus: Early Transcendentals by James Stewart. Great condition with minimal highlighting. Perfect for MATH 30/31. ISBN: 978-1285741550', 180.00, 'TEXTBOOKS', 'GOOD', 1, 'APPROVED', 23, true, 'Engineering Building - Room 189', '2024-10-18 14:30:00'),

('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000001', 'iPhone 13 Pro Max', 'Unlocked iPhone 13 Pro Max 256GB in Pacific Blue. Screen protector and case included. Battery health 98%. No scratches or dents.', 850.00, 'ELECTRONICS', 'GOOD', 1, 'APPROVED', 67, true, 'Student Union - Food Court', '2024-10-15 09:15:00'),

('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000001', 'Scientific Calculator TI-84', 'Texas Instruments TI-84 Plus CE in Coral Pink. Required for engineering courses. Works perfectly, all functions tested.', 75.00, 'ELECTRONICS', 'GOOD', 1, 'APPROVED', 12, true, 'Engineering Building - Main Lobby', '2024-10-25 16:00:00'),

('20000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000001', 'Gaming Laptop - ASUS ROG', 'High-performance gaming laptop. RTX 3070, 16GB RAM, 1TB SSD. Perfect for CS students and gaming. Runs all modern games on high settings.', 1500.00, 'ELECTRONICS', 'GOOD', 1, 'APPROVED', 89, true, 'Campus Village - Dorm A', '2024-10-22 11:45:00'),

('20000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000001', 'Business Statistics Textbook', 'Business Statistics by Anderson, Sweeney & Williams. Used for BUS 130. Excellent condition, no writing inside. ISBN: 978-1337094171', 120.00, 'TEXTBOOKS', 'LIKE_NEW', 1, 'APPROVED', 15, true, 'Business Building - Room 120', '2024-10-19 13:20:00'),

('20000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000001', 'iPad Air with Apple Pencil', 'iPad Air 4th generation 64GB with Apple Pencil 2nd gen. Space Gray. Great for note-taking and digital art. Includes case.', 450.00, 'ELECTRONICS', 'GOOD', 1, 'PENDING', 5, true, 'Art Building - Studio 2', '2024-10-28 08:30:00'),

('20000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000001', 'Organic Chemistry Study Guide', 'Complete study guide for Organic Chemistry with practice problems. Helped me get an A! Includes solution manual.', 25.00, 'TEXTBOOKS', 'FAIR', 1, 'APPROVED', 8, false, 'Science Building - Lab 305', '2024-10-10 10:00:00');

-- =====================================================
-- SEED DATA - ORDERS
-- =====================================================

-- Cart for Bob (active shopping cart)
INSERT INTO orders (order_id, buyer_id, university_id, status, subtotal, tax_amount, delivery_fee, platform_fee, total_amount) VALUES
('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000001', 'CART', 0.00, 0.00, 0.00, 0.00, 0.00);

-- Completed order for Sarah
INSERT INTO orders (order_id, buyer_id, university_id, order_number, status, subtotal, tax_amount, delivery_fee, platform_fee, total_amount, delivery_method, buyer_notes, ordered_at, paid_at, delivered_at, completed_at) VALUES
('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000001', 'ORD-20241025-123456', 'COMPLETED', 25.00, 2.25, 0.00, 0.63, 27.88, 'CAMPUS_PICKUP', 'Please meet at the Science Building main entrance', '2024-10-25 14:00:00', '2024-10-25 14:05:00', '2024-10-26 10:00:00', '2024-10-26 10:15:00');

-- Pending order for Mike
INSERT INTO orders (order_id, buyer_id, university_id, order_number, status, subtotal, tax_amount, delivery_fee, platform_fee, total_amount, delivery_method, ordered_at, paid_at) VALUES
('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000001', 'ORD-20241028-789012', 'PROCESSING', 180.00, 16.20, 0.00, 4.50, 200.70, 'CAMPUS_PICKUP', '2024-10-28 09:00:00', '2024-10-28 09:05:00');

-- =====================================================
-- SEED DATA - ORDER ITEMS
-- =====================================================

-- Order item for Sarah completed order (Organic Chem book)
INSERT INTO order_items (order_item_id, order_id, product_id, seller_id, product_title, product_condition, unit_price, quantity, total_price, fulfillment_status) VALUES
('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000102', 'Organic Chemistry Study Guide', 'FAIR', 25.00, 1, 25.00, 'COMPLETED');

-- Order item for Mike's pending order (Calculus textbook)
INSERT INTO order_items (order_item_id, order_id, product_id, seller_id, product_title, product_condition, unit_price, quantity, total_price, fulfillment_status) VALUES
('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000102', 'Calculus Textbook - Stewart 8th Edition', 'GOOD', 180.00, 1, 180.00, 'PROCESSING');

-- =====================================================
-- UPDATE PRODUCTS SOLD STATUS
-- =====================================================

-- Mark sold products
UPDATE products SET
    quantity = 0,
    sold_quantity = 1,
    is_active = false
WHERE product_id IN ('20000000-0000-0000-0000-000000000008');

-- =====================================================
-- VERIFICATION
-- =====================================================

-- Quick counts to verify data loaded correctly
SELECT 'Universities' as table_name, COUNT(*) as count FROM universities
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Students', COUNT(*) FROM users WHERE role = 'STUDENT'
UNION ALL
SELECT 'Admins', COUNT(*) FROM users WHERE role = 'ADMIN'
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Active Products', COUNT(*) FROM products WHERE is_active = true
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items
UNION ALL
SELECT 'Refresh Tokens', COUNT(*) FROM refresh_tokens
UNION ALL
SELECT 'Listings', COUNT(*) FROM listings
UNION ALL
SELECT 'Listing Images', COUNT(*) FROM listing_images
UNION ALL
SELECT 'Reports', COUNT(*) FROM reports
UNION ALL
SELECT 'Conversations', COUNT(*) FROM conversations
UNION ALL
SELECT 'Messages', COUNT(*) FROM messages;

-- =====================================================
-- SEED DATA - LISTING API (Listings, Images, Reports)
-- =====================================================

-- Sample listings
INSERT INTO listings (seller_id, title, description, category, price, condition, status, location, view_count, created_at, updated_at) VALUES
  (101, 'Dell XPS 13 Laptop', 'Lightweight ultrabook perfect for note taking and coding. 16GB RAM, 512GB SSD.', 'ELECTRONICS', 650.00, 'GOOD', 'ACTIVE', 'SJSU Library', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (102, 'Discrete Math Textbook', 'Discrete Mathematics and Its Applications, 8th Ed. Minimal highlights.', 'TEXTBOOKS', 60.00, 'LIKE_NEW', 'ACTIVE', 'Engineering Building', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (103, 'iPad Air 4 + Pencil', 'Great for digital notes. Includes Apple Pencil 2 and case.', 'ELECTRONICS', 420.00, 'GOOD', 'PENDING', 'Student Union', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Images for the listings (assumes generated listing_id values 1..3)
INSERT INTO listing_images (listing_id, image_url, alt_text, display_order) VALUES
  (1, 'https://cdn.example.com/listings/xps13/front.jpg', 'Dell XPS 13 Front', 1),
  (1, 'https://cdn.example.com/listings/xps13/side.jpg', 'Dell XPS 13 Side', 2),
  (2, 'https://cdn.example.com/listings/discrete-math/cover.jpg', 'Discrete Math Cover', 1),
  (3, 'https://cdn.example.com/listings/ipad-air/hero.jpg', 'iPad Air with Pencil', 1);

-- Reports against listings
INSERT INTO reports (reporter_id, listing_id, report_type, description, status, created_at)
VALUES
  (201, 3, 'SPAM', 'Looks like a duplicate listing posted multiple times.', 'PENDING', CURRENT_TIMESTAMP),
  (202, 1, 'INAPPROPRIATE_CONTENT', 'Listing description includes inappropriate language.', 'UNDER_REVIEW', CURRENT_TIMESTAMP);

-- =====================================================
-- SEED DATA - COMMUNICATION SERVICE (Conversations & Messages)
-- =====================================================

-- Sample conversations between buyers and sellers
-- Note: buyer_id and seller_id use numeric IDs (matching listing API user IDs)
-- listing_id references the listings table created above

-- Conversation 1: Buyer 104 (Sarah) messaging Seller 101 about listing 1 (Dell XPS 13)
INSERT INTO conversations (listing_id, buyer_id, seller_id, created_at, updated_at) VALUES
  (1, 104, 101, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 hour');

-- Conversation 2: Buyer 103 (Bob) messaging Seller 102 about listing 2 (Discrete Math Textbook)
INSERT INTO conversations (listing_id, buyer_id, seller_id, created_at, updated_at) VALUES
  (2, 103, 102, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '30 minutes');

-- Conversation 3: Buyer 105 (Mike) messaging Seller 103 about listing 3 (iPad Air)
INSERT INTO conversations (listing_id, buyer_id, seller_id, created_at, updated_at) VALUES
  (3, 105, 103, CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '5 minutes');

-- Messages for Conversation 1 (Dell XPS 13)
-- Assumes conversation_id = 1 (first conversation inserted)
INSERT INTO messages (conversation_id, sender_id, content, is_read, created_at) VALUES
  (1, 104, 'Hi! Is the Dell XPS 13 still available?', true, CURRENT_TIMESTAMP - INTERVAL '2 days'),
  (1, 101, 'Yes, it is! Are you interested in seeing it?', true, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '10 minutes'),
  (1, 104, 'Yes, I would like to check it out. Can we meet at the library?', true, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '20 minutes'),
  (1, 101, 'Sure! I can meet you there tomorrow at 2 PM. Does that work?', true, CURRENT_TIMESTAMP - INTERVAL '1 day'),
  (1, 104, 'Perfect! See you then.', false, CURRENT_TIMESTAMP - INTERVAL '1 hour');

-- Messages for Conversation 2 (Discrete Math Textbook)
-- Assumes conversation_id = 2 (second conversation inserted)
INSERT INTO messages (conversation_id, sender_id, content, is_read, created_at) VALUES
  (2, 103, 'Hello, I saw your Discrete Math textbook listing. Is it the 8th edition?', true, CURRENT_TIMESTAMP - INTERVAL '1 day'),
  (2, 102, 'Yes, it is the 8th edition. It is in great condition with minimal highlighting.', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '5 minutes'),
  (2, 103, 'Great! Is the price negotiable?', true, CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '15 minutes'),
  (2, 102, 'I can do $55 if you can pick it up today.', false, CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
  (2, 103, 'Deal! Where should I meet you?', false, CURRENT_TIMESTAMP - INTERVAL '25 minutes');

-- Messages for Conversation 3 (iPad Air)
-- Assumes conversation_id = 3 (third conversation inserted)
INSERT INTO messages (conversation_id, sender_id, content, is_read, created_at) VALUES
  (3, 105, 'Hi, is the iPad still available?', true, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
  (3, 103, 'Yes, it is still available. The listing is pending approval but I can show it to you.', true, CURRENT_TIMESTAMP - INTERVAL '3 hours' + INTERVAL '2 minutes'),
  (3, 105, 'Does it come with the Apple Pencil?', true, CURRENT_TIMESTAMP - INTERVAL '2 hours' + INTERVAL '30 minutes'),
  (3, 103, 'Yes, it includes the Apple Pencil 2nd gen and a protective case.', true, CURRENT_TIMESTAMP - INTERVAL '2 hours' + INTERVAL '25 minutes'),
  (3, 105, 'Perfect! Can you send me some photos?', false, CURRENT_TIMESTAMP - INTERVAL '5 minutes');
