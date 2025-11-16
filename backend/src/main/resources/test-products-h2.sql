-- Test Products for H2 Database (Autocomplete Testing)
-- Run this via H2 Console: http://localhost:8080/api/h2-console
-- JDBC URL: jdbc:h2:mem:campusmarketplace
-- Username: sa
-- Password: password

-- First, get your university_id and user_id
-- Replace these UUIDs with your actual values from the database

-- Get your university_id (should be created when you registered)
-- SELECT university_id FROM universities WHERE domain = 'sjsu.edu';

-- Get your user_id
-- SELECT user_id FROM users WHERE username = 'testuser';

-- Insert test products (replace the UUIDs below with your actual values)
-- Example: If your university_id is '123e4567-e89b-12d3-a456-426614174000'
--          and your user_id is '987fcdeb-51a2-43d7-8f9e-123456789abc'

-- H2-compatible test products script
-- Run this via H2 Console: http://localhost:8080/api/h2-console
-- JDBC URL: jdbc:h2:mem:campusmarketplace
-- Username: sa
-- Password: password

INSERT INTO products (
    product_id,
    seller_id,
    university_id,
    title,
    description,
    category,
    condition,
    price,
    quantity,
    is_active,
    moderation_status,
    view_count,
    created_at,
    published_at
) VALUES
-- Product 1: Laptop
(
    RANDOM_UUID(),  -- H2 supports RANDOM_UUID()
    (SELECT user_id FROM users WHERE username = 'testuser' LIMIT 1),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu' LIMIT 1),
    'MacBook Pro 13" 2021 - M1 Chip',
    'Excellent condition MacBook Pro with M1 chip. Used for one semester only.',
    'ELECTRONICS',
    'LIKE_NEW',
    1200.00,
    1,
    true,
    'APPROVED',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Product 2: Textbook
(
    RANDOM_UUID(),
    (SELECT user_id FROM users WHERE username = 'testuser' LIMIT 1),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu' LIMIT 1),
    'Calculus Textbook - Stewart 8th Edition',
    'Calculus: Early Transcendentals by James Stewart. Great condition.',
    'TEXTBOOKS',
    'GOOD',
    180.00,
    1,
    true,
    'APPROVED',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Product 3: Laptop (different brand)
(
    RANDOM_UUID(),
    (SELECT user_id FROM users WHERE username = 'testuser' LIMIT 1),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu' LIMIT 1),
    'Dell XPS 13 Laptop - 16GB RAM',
    'Lightweight ultrabook perfect for coding. 16GB RAM, 512GB SSD.',
    'ELECTRONICS',
    'GOOD',
    650.00,
    1,
    true,
    'APPROVED',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Product 4: Textbook
(
    RANDOM_UUID(),
    (SELECT user_id FROM users WHERE username = 'testuser' LIMIT 1),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu' LIMIT 1),
    'Data Structures and Algorithms in Java',
    'Textbook for CMPE 146. All chapters intact, minimal highlighting.',
    'TEXTBOOKS',
    'LIKE_NEW',
    45.00,
    1,
    true,
    'APPROVED',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Product 5: Electronics
(
    RANDOM_UUID(),
    (SELECT user_id FROM users WHERE username = 'testuser' LIMIT 1),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu' LIMIT 1),
    'iPad Air 4 with Apple Pencil',
    'Great for digital notes. Includes Apple Pencil 2 and case.',
    'ELECTRONICS',
    'GOOD',
    420.00,
    1,
    true,
    'APPROVED',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verify products were created
SELECT 'Products created: ' || COUNT(*) FROM products WHERE is_active = true;

