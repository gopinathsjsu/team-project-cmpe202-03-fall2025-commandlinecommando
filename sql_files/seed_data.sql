-- seed_data.sql
-- Campus Marketplace Sample Data for Testing and Development
-- Insert realistic test data for all tables

-- =====================================================
-- SEED DATA - USERS (Students and Admins)
-- =====================================================

-- Insert test students
INSERT INTO users (user_type, username, email, password_hash, first_name, last_name, phone, role, student_id, major, graduation_year, campus_location, is_active) VALUES
('STUDENT', 'student', 'student@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'John', 'Student', '555-0101', 'STUDENT', 'STU001', 'Computer Science', 2025, 'San Jose Main Campus', true),
('STUDENT', 'alice_chen', 'alice.chen@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Alice', 'Chen', '555-0102', 'STUDENT', 'STU002', 'Electrical Engineering', 2024, 'San Jose Main Campus', true),
('STUDENT', 'bob_martinez', 'bob.martinez@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Bob', 'Martinez', '555-0103', 'STUDENT', 'STU003', 'Business Administration', 2026, 'San Jose Main Campus', true),
('STUDENT', 'sarah_kim', 'sarah.kim@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Sarah', 'Kim', '555-0104', 'STUDENT', 'STU004', 'Mechanical Engineering', 2025, 'San Jose Main Campus', true),
('STUDENT', 'mike_johnson', 'mike.johnson@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Mike', 'Johnson', '555-0105', 'STUDENT', 'STU005', 'Computer Science', 2024, 'San Jose Main Campus', true);

-- Insert test admins
INSERT INTO users (user_type, username, email, password_hash, first_name, last_name, phone, role, admin_level, is_active) VALUES
('ADMIN', 'admin', 'admin@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Jane', 'Admin', '555-0201', 'ADMIN', 'ADMIN', true),
('ADMIN', 'super_admin', 'superadmin@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Super', 'Administrator', '555-0202', 'ADMIN', 'SUPER_ADMIN', true),
('ADMIN', 'moderator1', 'mod1@sjsu.edu', '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN', 'Tom', 'Moderator', '555-0203', 'ADMIN', 'MODERATOR', true);

-- Insert admin permissions
INSERT INTO admin_permissions (admin_id, permission) VALUES
-- Super admin gets all permissions
(7, 'CREATE_USER'), (7, 'READ_USER'), (7, 'UPDATE_USER'), (7, 'DELETE_USER'),
(7, 'CREATE_LISTING'), (7, 'READ_LISTING'), (7, 'UPDATE_LISTING'), (7, 'DELETE_LISTING'), (7, 'MODERATE_LISTING'),
(7, 'VIEW_REPORTS'), (7, 'RESOLVE_REPORTS'), (7, 'SYSTEM_CONFIG'), (7, 'VIEW_ANALYTICS'), (7, 'MANAGE_CATEGORIES'),
-- Regular admin gets standard permissions
(6, 'READ_USER'), (6, 'READ_LISTING'), (6, 'MODERATE_LISTING'), (6, 'VIEW_REPORTS'), (6, 'RESOLVE_REPORTS'),
-- Moderator gets limited permissions
(8, 'READ_LISTING'), (8, 'MODERATE_LISTING'), (8, 'VIEW_REPORTS');

-- =====================================================
-- SEED DATA - LISTINGS
-- =====================================================

INSERT INTO listings (seller_id, title, description, price, category, status, condition, location, view_count) VALUES
(1, 'MacBook Pro 13" 2021', 'Excellent condition MacBook Pro with M1 chip. Used for one semester only. Includes original charger and box.', 1200.00, 'ELECTRONICS', 'ACTIVE', 'LIKE_NEW', 'San Jose Campus - Library', 45),
(2, 'Calculus Textbook - Stewart 8th Edition', 'Calculus: Early Transcendentals by James Stewart. Great condition with minimal highlighting. Perfect for MATH 30/31.', 180.00, 'TEXTBOOKS', 'ACTIVE', 'GOOD', 'Engineering Building', 23),
(3, 'iPhone 13 Pro Max', 'Unlocked iPhone 13 Pro Max 256GB in Pacific Blue. Screen protector and case included. Battery health 98%.', 850.00, 'ELECTRONICS', 'ACTIVE', 'GOOD', 'Student Union', 67),
(4, 'Scientific Calculator TI-84', 'Texas Instruments TI-84 Plus CE. Required for engineering courses. Works perfectly.', 75.00, 'ELECTRONICS', 'ACTIVE', 'GOOD', 'Engineering Building', 12),
(5, 'Organic Chemistry Study Guide', 'Complete study guide for Organic Chemistry with practice problems. Helped me get an A!', 25.00, 'TEXTBOOKS', 'SOLD', 'FAIR', 'Science Building', 8),
(1, 'Gaming Laptop - ASUS ROG', 'High-performance gaming laptop. RTX 3070, 16GB RAM, 1TB SSD. Perfect for CS students and gaming.', 1500.00, 'ELECTRONICS', 'ACTIVE', 'GOOD', 'Dorm Area', 89),
(3, 'Business Statistics Textbook', 'Business Statistics by Anderson, Sweeney & Williams. Used for BUS 130. Excellent condition.', 120.00, 'TEXTBOOKS', 'ACTIVE', 'LIKE_NEW', 'Business Building', 15),
(4, 'iPad Air with Apple Pencil', 'iPad Air 4th generation with Apple Pencil 2nd gen. Great for note-taking and digital art.', 450.00, 'ELECTRONICS', 'PENDING', 'GOOD', 'Art Building', 5);

-- =====================================================
-- SEED DATA - LISTING IMAGES
-- =====================================================

INSERT INTO listing_images (listing_id, image_url, file_name, alt_text, display_order, file_size, mime_type) VALUES
(1, '/uploads/macbook_pro_1.jpg', 'macbook_pro_1.jpg', 'MacBook Pro front view', 1, 2048576, 'image/jpeg'),
(1, '/uploads/macbook_pro_2.jpg', 'macbook_pro_2.jpg', 'MacBook Pro with charger', 2, 1843200, 'image/jpeg'),
(2, '/uploads/calculus_book.jpg', 'calculus_book.jpg', 'Stewart Calculus textbook cover', 1, 1024000, 'image/jpeg'),
(3, '/uploads/iphone_13_pro.jpg', 'iphone_13_pro.jpg', 'iPhone 13 Pro Max Pacific Blue', 1, 1536000, 'image/jpeg'),
(4, '/uploads/ti84_calculator.jpg', 'ti84_calculator.jpg', 'TI-84 Plus CE calculator', 1, 768000, 'image/jpeg'),
(6, '/uploads/gaming_laptop_1.jpg', 'gaming_laptop_1.jpg', 'ASUS ROG gaming laptop', 1, 2560000, 'image/jpeg'),
(6, '/uploads/gaming_laptop_2.jpg', 'gaming_laptop_2.jpg', 'Gaming laptop specifications screen', 2, 1920000, 'image/jpeg');

-- =====================================================
-- SEED DATA - CONVERSATIONS AND MESSAGES
-- =====================================================

INSERT INTO conversations (listing_id, buyer_id, seller_id, status, last_message_at) VALUES
(1, 3, 1, 'ACTIVE', '2024-01-15 14:30:00'),
(3, 1, 2, 'ACTIVE', '2024-01-15 16:45:00'),
(6, 4, 1, 'ACTIVE', '2024-01-14 10:20:00'),
(2, 5, 2, 'CLOSED', '2024-01-12 09:15:00');

INSERT INTO messages (conversation_id, sender_id, content, message_type, is_read, sent_at) VALUES
-- Conversation 1: Bob interested in John's MacBook
(1, 3, 'Hi! Is this MacBook still available?', 'TEXT', true, '2024-01-15 14:25:00'),
(1, 1, 'Yes, it is! Are you interested in seeing it in person?', 'TEXT', true, '2024-01-15 14:27:00'),
(1, 3, 'Definitely! When would be a good time to meet?', 'TEXT', true, '2024-01-15 14:28:00'),
(1, 1, 'How about tomorrow at 2 PM near the library?', 'TEXT', false, '2024-01-15 14:30:00'),

-- Conversation 2: John interested in Alice iPhone
(2, 1, 'Is the battery health really 98%? That is impressive for a used phone.', 'TEXT', true, '2024-01-15 16:40:00'),
(2, 2, 'Yes! I can show you the battery settings when we meet. I have taken great care of it.', 'TEXT', true, '2024-01-15 16:42:00'),
(2, 1, 'Great! What is your lowest price?', 'TEXT', true, '2024-01-15 16:44:00'),
(2, 2, 'I could do $800 if you can meet today.', 'TEXT', false, '2024-01-15 16:45:00'),

-- Conversation 3: Sarah interested in John gaming laptop
(3, 4, 'Does this laptop run the latest games smoothly?', 'TEXT', true, '2024-01-14 10:15:00'),
(3, 1, 'Absolutely! I have been playing Cyberpunk 2077 on high settings without issues.', 'TEXT', true, '2024-01-14 10:18:00'),
(3, 4, 'Perfect! I am a CS major and need something powerful for both gaming and development.', 'TEXT', false, '2024-01-14 10:20:00');

-- =====================================================
-- SEED DATA - REPORTS
-- =====================================================

INSERT INTO reports (reporter_id, listing_id, report_type, description, status, created_at) VALUES
(2, 6, 'SPAM', 'This listing has been posted multiple times with slight variations. Seems like spam.', 'PENDING', '2024-01-15 09:00:00'),
(4, 3, 'OTHER', 'Price seems too good to be true for this model. Might be a scam.', 'UNDER_REVIEW', '2024-01-14 15:30:00'),
(5, 1, 'DUPLICATE', 'I saw this exact same MacBook listed by another user yesterday.', 'DISMISSED', '2024-01-13 11:20:00');

-- Update reviewed reports
UPDATE reports SET 
    reviewed_at = '2024-01-13 14:30:00',
    reviewed_by = 6,
    resolution_notes = 'Verified with seller - this is a legitimate listing. Different MacBook than reported duplicate.'
WHERE report_id = 3;

-- =====================================================
-- SEED DATA - CHATBOT QUERIES
-- =====================================================

INSERT INTO chatbot_queries (user_id, query, response, created_at, processed_at, processing_time_ms) VALUES
(1, 'How do I create a new listing?', 'To create a new listing, go to your dashboard and click "Create Listing". Fill in the title, description, price, and category. You can also upload up to 5 images of your item.', '2024-01-15 10:00:00', '2024-01-15 10:00:01', 1250),
(3, 'What payment methods are accepted?', 'The campus marketplace facilitates connections between buyers and sellers. Payment arrangements are made directly between users. We recommend meeting in person at safe campus locations for transactions.', '2024-01-15 11:30:00', '2024-01-15 11:30:02', 1800),
(2, 'How do I report a suspicious listing?', 'To report a listing, click the "Report" button on the listing page. Select the appropriate reason (spam, inappropriate, fraud, etc.) and provide details. Our moderation team will review it within 24 hours.', '2024-01-14 16:45:00', '2024-01-14 16:45:01', 950),
(NULL, 'What categories are available for listings?', 'Available categories include: Textbooks, Gadgets, Electronics, Stationary, and Other. Choose the category that best matches your item to help buyers find it easily.', '2024-01-14 09:15:00', '2024-01-14 09:15:03', 2100);

-- =====================================================
-- SEED DATA - REFRESH TOKENS
-- =====================================================

INSERT INTO refresh_tokens (token, user_id, expires_at, device_info, is_revoked) VALUES
('eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoxLCJzdWIiOiJzdHVkZW50IiwiaWF0IjoxNzA1MzIwMDAwLCJleHAiOjE3MDU5MjQ4MDB9.sample_refresh_token_1', 1, '2024-01-22 10:00:00', 'iPhone Safari', false),
('eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoyLCJzdWIiOiJhbGljZV9jaGVuIiwiaWF0IjoxNzA1MzIwMDAwLCJleHAiOjE3MDU5MjQ4MDB9.sample_refresh_token_2', 2, '2024-01-22 11:30:00', 'Chrome Windows', false),
('eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjo2LCJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNTMyMDAwMCwiZXhwIjoxNzA1OTI0ODAwfQ.sample_refresh_token_admin', 6, '2024-01-22 08:00:00', 'Firefox Mac', false),
('revoked_token_example', 3, '2024-01-20 12:00:00', 'Chrome Mobile', true);

-- Update conversation last_message_at timestamps to match latest messages
UPDATE conversations SET last_message_at = (
    SELECT MAX(sent_at) FROM messages WHERE messages.conversation_id = conversations.conversation_id
);
