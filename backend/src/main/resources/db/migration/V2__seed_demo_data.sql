-- =============================================================================
-- Campus Marketplace - Demo Data Seed Script
-- CMPE 202 Project: Sample Data for Demo Day
-- Version: 2.0.0
-- =============================================================================

-- =============================================================================
-- DEMO USERS - All Three Roles (Buyer, Seller, Admin)
-- =============================================================================

-- Demo Buyers
INSERT INTO users (
    university_id,
    email,
    username,
    password_hash,
    first_name,
    last_name,
    phone,
    role,
    verification_status,
    student_id,
    university_email,
    graduation_year,
    major,
    email_verified_at
) VALUES
-- Buyer 1: Active student looking for textbooks
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'alice.buyer@sjsu.edu',
    'alice_buyer',
    '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN',  -- password: buyer123
    'Alice',
    'Johnson',
    '408-555-0101',
    'BUYER',
    'VERIFIED',
    'BUY001',
    'alice.johnson@sjsu.edu',
    2025,
    'Computer Science',
    CURRENT_TIMESTAMP
),
-- Buyer 2: Graduate student
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'bob.student@sjsu.edu',
    'bob_buyer',
    '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN',
    'Bob',
    'Smith',
    '408-555-0102',
    'BUYER',
    'VERIFIED',
    'BUY002',
    'bob.smith@sjsu.edu',
    2026,
    'Business Administration',
    CURRENT_TIMESTAMP
);

-- Demo Sellers
INSERT INTO users (
    university_id,
    email,
    username,
    password_hash,
    first_name,
    last_name,
    phone,
    role,
    verification_status,
    student_id,
    university_email,
    graduation_year,
    major,
    email_verified_at
) VALUES
-- Seller 1: Active textbook seller
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'carol.seller@sjsu.edu',
    'carol_seller',
    '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN',  -- password: seller123
    'Carol',
    'Martinez',
    '408-555-0103',
    'SELLER',
    'VERIFIED',
    'SEL001',
    'carol.martinez@sjsu.edu',
    2024,
    'Engineering',
    CURRENT_TIMESTAMP
),
-- Seller 2: Electronics seller
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'david.tech@sjsu.edu',
    'david_techseller',
    '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN',
    'David',
    'Lee',
    '408-555-0104',
    'SELLER',
    'VERIFIED',
    'SEL002',
    'david.lee@sjsu.edu',
    2025,
    'Computer Engineering',
    CURRENT_TIMESTAMP
);

-- Demo Admins
INSERT INTO users (
    university_id,
    email,
    username,
    password_hash,
    first_name,
    last_name,
    phone,
    role,
    verification_status,
    email_verified_at
) VALUES
-- Admin: Platform administrator
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'admin@sjsu.edu',
    'sjsu_admin',
    '$2a$10$rOIHQnPyoJBSAcQWJYJSJe5Vx8eHOKYvOoYRNKZdNjZdNjZdNjZdN',  -- password: admin123
    'Emma',
    'Admin',
    '408-555-0100',
    'ADMIN',
    'VERIFIED',
    CURRENT_TIMESTAMP
);

-- =============================================================================
-- USER ADDRESSES - Delivery Locations
-- =============================================================================

-- Alice's dorm address
INSERT INTO user_addresses (
    user_id,
    address_type,
    address_line1,
    city,
    state,
    zip_code,
    building_name,
    room_number,
    is_default
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    'DORM',
    'Campus Village Building A',
    'San Jose',
    'CA',
    '95192',
    'Campus Village A',
    '304',
    true
);

-- Bob's apartment address
INSERT INTO user_addresses (
    user_id,
    address_type,
    address_line1,
    address_line2,
    city,
    state,
    zip_code,
    is_default
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'bob_buyer'),
    'APARTMENT',
    '123 E San Fernando St',
    'Apt 2B',
    'San Jose',
    'CA',
    '95112',
    true
);

-- =============================================================================
-- DEMO PRODUCTS - Marketplace Listings
-- =============================================================================

-- Textbooks by Carol
INSERT INTO products (
    seller_id,
    university_id,
    title,
    description,
    category,
    condition,
    price,
    original_price,
    quantity,
    negotiable,
    attributes,
    is_active,
    moderation_status,
    pickup_location,
    published_at
) VALUES
-- Textbook 1: Data Structures
(
    (SELECT user_id FROM users WHERE username = 'carol_seller'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Data Structures and Algorithms in Java - 6th Edition',
    'Excellent condition textbook for CMPE 146. All chapters intact, minimal highlighting. Perfect for CS students.',
    'TEXTBOOKS',
    'LIKE_NEW',
    45.00,
    120.00,
    1,
    true,
    '{
        "isbn": "978-0134462066",
        "author": "Michael T. Goodrich",
        "edition": "6th",
        "subject": "Computer Science",
        "course": "CMPE 146"
    }'::jsonb,
    true,
    'APPROVED',
    'Engineering Building, Room 285',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
-- Textbook 2: Calculus
(
    (SELECT user_id FROM users WHERE username = 'carol_seller'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Calculus: Early Transcendentals - 8th Edition',
    'Used for Math 30. Good condition with some notes in margins. Solutions manual included!',
    'TEXTBOOKS',
    'GOOD',
    35.00,
    95.00,
    1,
    true,
    '{
        "isbn": "978-1285741550",
        "author": "James Stewart",
        "edition": "8th",
        "subject": "Mathematics",
        "course": "MATH 30",
        "includes": ["Solutions Manual"]
    }'::jsonb,
    true,
    'APPROVED',
    'MLK Library, 3rd Floor',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
);

-- Electronics by David
INSERT INTO products (
    seller_id,
    university_id,
    title,
    description,
    category,
    condition,
    price,
    original_price,
    quantity,
    negotiable,
    attributes,
    is_active,
    moderation_status,
    pickup_location,
    published_at
) VALUES
-- Electronics 1: Laptop
(
    (SELECT user_id FROM users WHERE username = 'david_techseller'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'MacBook Pro 13" M1 2020 - 8GB RAM 256GB SSD',
    'Gently used MacBook Pro in excellent condition. Perfect for CS students. Includes charger and original box. Battery health 92%.',
    'ELECTRONICS',
    'LIKE_NEW',
    750.00,
    1299.00,
    1,
    true,
    '{
        "brand": "Apple",
        "model": "MacBook Pro 13-inch",
        "processor": "Apple M1",
        "ram": "8GB",
        "storage": "256GB SSD",
        "year": 2020,
        "battery_health": "92%",
        "includes": ["Charger", "Original Box"]
    }'::jsonb,
    true,
    'APPROVED',
    'Student Union, 2nd Floor Lounge',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
-- Electronics 2: Calculator
(
    (SELECT user_id FROM users WHERE username = 'david_techseller'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'TI-84 Plus CE Graphing Calculator - Blue',
    'Barely used graphing calculator. Required for Math and Engineering courses. Includes USB cable.',
    'ELECTRONICS',
    'LIKE_NEW',
    80.00,
    140.00,
    1,
    false,
    '{
        "brand": "Texas Instruments",
        "model": "TI-84 Plus CE",
        "color": "Blue",
        "includes": ["USB Cable", "Manual"]
    }'::jsonb,
    true,
    'APPROVED',
    'Engineering Building Lobby',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
);

-- Furniture listing
INSERT INTO products (
    seller_id,
    university_id,
    title,
    description,
    category,
    condition,
    price,
    original_price,
    quantity,
    negotiable,
    attributes,
    is_active,
    moderation_status,
    pickup_location,
    published_at
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'carol_seller'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'IKEA Desk with Chair - Perfect for Dorm',
    'Moving out sale! Compact desk and chair set perfect for dorm rooms. Easy to assemble/disassemble.',
    'FURNITURE',
    'GOOD',
    60.00,
    150.00,
    1,
    true,
    '{
        "brand": "IKEA",
        "desk_dimensions": "47x24 inches",
        "chair_type": "Office chair with wheels",
        "assembly_required": true
    }'::jsonb,
    true,
    'APPROVED',
    'Campus Village Parking Lot',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
);

-- =============================================================================
-- PRODUCT IMAGES
-- =============================================================================

-- Images for Data Structures textbook
INSERT INTO product_images (product_id, image_url, thumbnail_url, display_order, is_primary)
VALUES
(
    (SELECT product_id FROM products WHERE title LIKE 'Data Structures%'),
    'https://images.example.com/textbooks/data-structures-java-6th.jpg',
    'https://images.example.com/textbooks/thumbs/data-structures-java-6th.jpg',
    0,
    true
);

-- Images for MacBook
INSERT INTO product_images (product_id, image_url, thumbnail_url, display_order, is_primary)
VALUES
(
    (SELECT product_id FROM products WHERE title LIKE 'MacBook Pro%'),
    'https://images.example.com/electronics/macbook-pro-m1-1.jpg',
    'https://images.example.com/electronics/thumbs/macbook-pro-m1-1.jpg',
    0,
    true
),
(
    (SELECT product_id FROM products WHERE title LIKE 'MacBook Pro%'),
    'https://images.example.com/electronics/macbook-pro-m1-2.jpg',
    'https://images.example.com/electronics/thumbs/macbook-pro-m1-2.jpg',
    1,
    false
);

-- =============================================================================
-- DEMO ORDERS & TRANSACTIONS
-- =============================================================================

-- Alice's completed order for Calculator
INSERT INTO orders (
    buyer_id,
    university_id,
    order_number,
    status,
    subtotal,
    tax_amount,
    delivery_fee,
    total_amount,
    delivery_method,
    delivery_address_id,
    cart_created_at,
    ordered_at,
    paid_at,
    delivered_at,
    completed_at
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'ORD-20250107-000001',
    'COMPLETED',
    80.00,
    7.20,
    0.00,
    87.20,
    'CAMPUS_PICKUP',
    (SELECT address_id FROM user_addresses WHERE user_id = (SELECT user_id FROM users WHERE username = 'alice_buyer') LIMIT 1),
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- Order item for calculator
INSERT INTO order_items (
    order_id,
    product_id,
    seller_id,
    product_title,
    product_condition,
    unit_price,
    quantity,
    total_price,
    fulfillment_status,
    shipped_at
) VALUES
(
    (SELECT order_id FROM orders WHERE order_number = 'ORD-20250107-000001'),
    (SELECT product_id FROM products WHERE title LIKE 'TI-84%'),
    (SELECT user_id FROM users WHERE username = 'david_techseller'),
    'TI-84 Plus CE Graphing Calculator - Blue',
    'LIKE_NEW',
    80.00,
    1,
    80.00,
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- Bob's pending order for Desk
INSERT INTO orders (
    buyer_id,
    university_id,
    order_number,
    status,
    subtotal,
    tax_amount,
    delivery_fee,
    total_amount,
    delivery_method,
    delivery_address_id,
    cart_created_at,
    ordered_at,
    paid_at
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'bob_buyer'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'ORD-20250107-000002',
    'PAID',
    60.00,
    5.40,
    0.00,
    65.40,
    'CAMPUS_PICKUP',
    (SELECT address_id FROM user_addresses WHERE user_id = (SELECT user_id FROM users WHERE username = 'bob_buyer') LIMIT 1),
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
);

-- Order item for desk
INSERT INTO order_items (
    order_id,
    product_id,
    seller_id,
    product_title,
    product_condition,
    unit_price,
    quantity,
    total_price,
    fulfillment_status
) VALUES
(
    (SELECT order_id FROM orders WHERE order_number = 'ORD-20250107-000002'),
    (SELECT product_id FROM products WHERE title LIKE 'IKEA Desk%'),
    (SELECT user_id FROM users WHERE username = 'carol_seller'),
    'IKEA Desk with Chair - Perfect for Dorm',
    'GOOD',
    60.00,
    1,
    60.00,
    'PROCESSING'
);

-- Alice's active shopping cart
INSERT INTO orders (
    buyer_id,
    university_id,
    status,
    subtotal,
    total_amount,
    cart_created_at
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'CART',
    45.00,
    45.00,
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
);

-- Cart item for Data Structures book
INSERT INTO order_items (
    order_id,
    product_id,
    seller_id,
    product_title,
    product_condition,
    unit_price,
    quantity,
    total_price,
    fulfillment_status
) VALUES
(
    (SELECT order_id FROM orders WHERE status = 'CART' AND buyer_id = (SELECT user_id FROM users WHERE username = 'alice_buyer')),
    (SELECT product_id FROM products WHERE title LIKE 'Data Structures%'),
    (SELECT user_id FROM users WHERE username = 'carol_seller'),
    'Data Structures and Algorithms in Java - 6th Edition',
    'LIKE_NEW',
    45.00,
    1,
    45.00,
    'CART'
);

-- =============================================================================
-- PRODUCT REVIEWS
-- =============================================================================

-- Alice's review for the calculator
INSERT INTO product_reviews (
    product_id,
    buyer_id,
    order_id,
    rating,
    title,
    comment,
    is_verified_purchase,
    is_visible,
    helpful_count
) VALUES
(
    (SELECT product_id FROM products WHERE title LIKE 'TI-84%'),
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    (SELECT order_id FROM orders WHERE order_number = 'ORD-20250107-000001'),
    5,
    'Perfect for my Math class!',
    'Calculator works great and was in like-new condition as described. Seller was very responsive and met me on campus. Highly recommend!',
    true,
    true,
    3
);

-- =============================================================================
-- USER FAVORITES/WISHLIST
-- =============================================================================

-- Bob's wishlist
INSERT INTO user_favorites (user_id, product_id) VALUES
(
    (SELECT user_id FROM users WHERE username = 'bob_buyer'),
    (SELECT product_id FROM products WHERE title LIKE 'MacBook Pro%')
),
(
    (SELECT user_id FROM users WHERE username = 'bob_buyer'),
    (SELECT product_id FROM products WHERE title LIKE 'Data Structures%')
);

-- =============================================================================
-- SEARCH HISTORY - Analytics Data
-- =============================================================================

INSERT INTO search_history (user_id, search_query, filters_applied, results_count, clicked_product_id)
VALUES
(
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    'calculator engineering',
    '{"category": "ELECTRONICS", "max_price": 100}'::jsonb,
    2,
    (SELECT product_id FROM products WHERE title LIKE 'TI-84%')
),
(
    (SELECT user_id FROM users WHERE username = 'bob_buyer'),
    'textbook computer science',
    '{"category": "TEXTBOOKS"}'::jsonb,
    3,
    (SELECT product_id FROM products WHERE title LIKE 'Data Structures%')
),
(
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    'laptop macbook',
    '{"category": "ELECTRONICS", "condition": "LIKE_NEW"}'::jsonb,
    1,
    (SELECT product_id FROM products WHERE title LIKE 'MacBook%')
);

-- =============================================================================
-- PRODUCT VIEWS - Engagement Tracking
-- =============================================================================

-- Simulate product views
INSERT INTO product_views (product_id, user_id, session_id, viewed_at)
SELECT 
    p.product_id,
    u.user_id,
    'session_' || md5(random()::text),
    CURRENT_TIMESTAMP - (random() * INTERVAL '7 days')
FROM products p
CROSS JOIN users u
WHERE u.role IN ('BUYER', 'SELLER')
AND random() < 0.6  -- 60% chance of view
LIMIT 50;

-- Update product view counts
UPDATE products p
SET view_count = (
    SELECT COUNT(*) FROM product_views WHERE product_id = p.product_id
);

-- =============================================================================
-- DAILY ANALYTICS - Aggregated Metrics
-- =============================================================================

INSERT INTO daily_analytics (
    university_id,
    date,
    new_users_count,
    active_users_count,
    new_products_count,
    active_products_count,
    orders_count,
    revenue,
    platform_fees,
    searches_count,
    product_views_count
)
SELECT
    uni.university_id,
    CURRENT_DATE - i,
    (random() * 10)::integer,  -- Simulated new users
    (random() * 50)::integer,  -- Simulated active users
    (random() * 5)::integer,   -- Simulated new products
    (SELECT COUNT(*) FROM products WHERE is_active = true),
    (random() * 8)::integer,   -- Simulated orders
    (random() * 500)::numeric(10,2),  -- Simulated revenue
    (random() * 50)::numeric(10,2),   -- Simulated platform fees
    (random() * 100)::integer, -- Simulated searches
    (random() * 200)::integer  -- Simulated views
FROM universities uni
CROSS JOIN generate_series(0, 6) i;  -- Last 7 days

-- =============================================================================
-- PAYMENT METHODS (Tokenized Demo Data)
-- =============================================================================

-- Alice's payment method
INSERT INTO payment_methods (
    user_id,
    method_type,
    payment_token,
    last_four,
    card_brand,
    expiry_month,
    expiry_year,
    is_default
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    'CREDIT_CARD',
    'tok_visa_demo_' || md5(random()::text),
    '4242',
    'VISA',
    12,
    2026,
    true
);

-- =============================================================================
-- TRANSACTIONS - Payment Records
-- =============================================================================

-- Transaction for Alice's completed order
INSERT INTO transactions (
    order_id,
    buyer_id,
    payment_method_id,
    amount,
    status,
    gateway_transaction_id,
    gateway_response,
    transaction_type,
    processed_at
) VALUES
(
    (SELECT order_id FROM orders WHERE order_number = 'ORD-20250107-000001'),
    (SELECT user_id FROM users WHERE username = 'alice_buyer'),
    (SELECT payment_method_id FROM payment_methods WHERE user_id = (SELECT user_id FROM users WHERE username = 'alice_buyer') LIMIT 1),
    87.20,
    'COMPLETED',
    'pi_demo_' || md5(random()::text),
    '{"status": "succeeded", "payment_method": "card_visa"}'::jsonb,
    'PURCHASE',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
);

-- =============================================================================
-- AUDIT LOGS - Sample Activity
-- =============================================================================

INSERT INTO audit_logs (
    user_id,
    university_id,
    table_name,
    record_id,
    action,
    old_values,
    new_values,
    changed_fields,
    ip_address
) VALUES
(
    (SELECT user_id FROM users WHERE username = 'carol_seller'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'products',
    (SELECT product_id FROM products WHERE title LIKE 'Data Structures%'),
    'INSERT',
    NULL,
    '{"title": "Data Structures and Algorithms in Java - 6th Edition", "price": 45.00}'::jsonb,
    ARRAY['title', 'price', 'category'],
    '192.168.1.100'::inet
);

-- =============================================================================
-- SCHEMA VERSION UPDATE
-- =============================================================================

INSERT INTO schema_version (version, description) VALUES
('2.0.0', 'Campus Marketplace Demo Data Seed - User Roles & Transactions');

-- =============================================================================
-- END OF DEMO DATA SEED
-- =============================================================================

