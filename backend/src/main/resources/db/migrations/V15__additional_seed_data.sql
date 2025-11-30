-- =============================================================================
-- Campus Marketplace - Additional Seed Data
-- More users and listings for demo
-- =============================================================================

-- =============================================================================
-- MORE USERS
-- =============================================================================

-- Additional buyers/sellers (password hash is for 'password123')
INSERT INTO users (
    university_id,
    email,
    username,
    password_hash,
    first_name,
    last_name,
    phone,
    verification_status,
    student_id,
    university_email,
    graduation_year,
    major,
    email_verified_at
) VALUES
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'mike.chen@sjsu.edu',
    'mike_chen',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Mike',
    'Chen',
    '408-555-0201',
    'VERIFIED',
    'STU003',
    'mike.chen@sjsu.edu',
    2025,
    'Software Engineering',
    CURRENT_TIMESTAMP
),
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'sarah.wilson@sjsu.edu',
    'sarah_wilson',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Sarah',
    'Wilson',
    '408-555-0202',
    'VERIFIED',
    'STU004',
    'sarah.wilson@sjsu.edu',
    2024,
    'Business Analytics',
    CURRENT_TIMESTAMP
),
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'jason.park@sjsu.edu',
    'jason_park',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Jason',
    'Park',
    '408-555-0203',
    'VERIFIED',
    'STU005',
    'jason.park@sjsu.edu',
    2026,
    'Mechanical Engineering',
    CURRENT_TIMESTAMP
),
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'emily.nguyen@sjsu.edu',
    'emily_nguyen',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Emily',
    'Nguyen',
    '408-555-0204',
    'VERIFIED',
    'STU006',
    'emily.nguyen@sjsu.edu',
    2025,
    'Graphic Design',
    CURRENT_TIMESTAMP
),
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'alex.rodriguez@sjsu.edu',
    'alex_rod',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Alex',
    'Rodriguez',
    '408-555-0205',
    'VERIFIED',
    'STU007',
    'alex.rodriguez@sjsu.edu',
    2024,
    'Kinesiology',
    CURRENT_TIMESTAMP
),
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'priya.sharma@sjsu.edu',
    'priya_sharma',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Priya',
    'Sharma',
    '408-555-0206',
    'VERIFIED',
    'STU008',
    'priya.sharma@sjsu.edu',
    2025,
    'Data Science',
    CURRENT_TIMESTAMP
),
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'kevin.jones@sjsu.edu',
    'kevin_jones',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Kevin',
    'Jones',
    '408-555-0207',
    'VERIFIED',
    'STU009',
    'kevin.jones@sjsu.edu',
    2026,
    'Finance',
    CURRENT_TIMESTAMP
),
(
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'lisa.kim@sjsu.edu',
    'lisa_kim',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQq0UH2p7O1mN8jYgL5Z7vU5E3K6',
    'Lisa',
    'Kim',
    '408-555-0208',
    'VERIFIED',
    'STU010',
    'lisa.kim@sjsu.edu',
    2025,
    'Psychology',
    CURRENT_TIMESTAMP
);

-- Add roles for new users (BUYER and SELLER for all)
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'BUYER' FROM users WHERE username IN ('mike_chen', 'sarah_wilson', 'jason_park', 'emily_nguyen', 'alex_rod', 'priya_sharma', 'kevin_jones', 'lisa_kim');

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'SELLER' FROM users WHERE username IN ('mike_chen', 'sarah_wilson', 'jason_park', 'emily_nguyen', 'alex_rod', 'priya_sharma', 'kevin_jones', 'lisa_kim');

-- =============================================================================
-- MORE LISTINGS
-- =============================================================================

INSERT INTO listings (
    seller_id,
    university_id,
    title,
    description,
    category,
    condition,
    price,
    quantity,
    negotiable,
    is_active,
    moderation_status,
    pickup_location,
    published_at
) VALUES
-- ELECTRONICS
(
    (SELECT user_id FROM users WHERE username = 'mike_chen'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Sony WH-1000XM4 Headphones',
    'Noise cancelling wireless headphones. Used for one semester, still has great battery life. Comes with case and cable.',
    'ELECTRONICS',
    'GOOD',
    180.00,
    1,
    true,
    true,
    'APPROVED',
    'Student Union',
    CURRENT_TIMESTAMP - INTERVAL '6 days'
),
(
    (SELECT user_id FROM users WHERE username = 'priya_sharma'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'iPad Air 4th Gen 64GB WiFi',
    'Space Gray iPad Air with Apple Pencil 2nd gen. Perfect for note-taking. Screen protector installed.',
    'ELECTRONICS',
    'LIKE_NEW',
    420.00,
    1,
    true,
    true,
    'APPROVED',
    'MLK Library',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    (SELECT user_id FROM users WHERE username = 'kevin_jones'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Logitech MX Master 3 Mouse',
    'Best mouse for productivity. Ergonomic design. Works on any surface.',
    'ELECTRONICS',
    'LIKE_NEW',
    65.00,
    1,
    false,
    true,
    'APPROVED',
    'Engineering Building',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),

-- TEXTBOOKS
(
    (SELECT user_id FROM users WHERE username = 'sarah_wilson'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Business Statistics Textbook - 14th Ed',
    'Sharpe, De Veaux, Velleman. Required for BUS 90. Good condition with some highlighting.',
    'TEXTBOOKS',
    'GOOD',
    55.00,
    1,
    true,
    true,
    'APPROVED',
    'Business Tower Lobby',
    CURRENT_TIMESTAMP - INTERVAL '8 days'
),
(
    (SELECT user_id FROM users WHERE username = 'lisa_kim'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Psychology 101 Bundle - 3 Books',
    'Intro to Psych, Abnormal Psych, and Research Methods. Selling as a bundle only.',
    'TEXTBOOKS',
    'GOOD',
    75.00,
    1,
    true,
    true,
    'APPROVED',
    'Dudley Moorhead Hall',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    (SELECT user_id FROM users WHERE username = 'mike_chen'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Clean Code by Robert Martin',
    'Classic software engineering book. A must-read for CS students.',
    'TEXTBOOKS',
    'LIKE_NEW',
    25.00,
    1,
    false,
    true,
    'APPROVED',
    'Engineering Building',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),

-- CLOTHING
(
    (SELECT user_id FROM users WHERE username = 'emily_nguyen'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'SJSU Spartan Hoodie - Size M',
    'Official SJSU hoodie, navy blue. Worn a few times, still looks new.',
    'CLOTHING',
    'LIKE_NEW',
    35.00,
    1,
    false,
    true,
    'APPROVED',
    'Event Center',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    (SELECT user_id FROM users WHERE username = 'alex_rod'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Nike Running Shoes Size 10',
    'Nike Pegasus 39. Used for one season of track. Still have lots of life left.',
    'CLOTHING',
    'GOOD',
    45.00,
    1,
    true,
    true,
    'APPROVED',
    'SRAC Gym',
    CURRENT_TIMESTAMP - INTERVAL '7 days'
),

-- FURNITURE
(
    (SELECT user_id FROM users WHERE username = 'jason_park'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Mini Fridge - Black',
    'Compact fridge perfect for dorm. Works great, just upgrading.',
    'FURNITURE',
    'GOOD',
    50.00,
    1,
    true,
    true,
    'APPROVED',
    'Campus Village',
    CURRENT_TIMESTAMP - INTERVAL '9 days'
),
(
    (SELECT user_id FROM users WHERE username = 'sarah_wilson'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Desk Lamp with USB Charging',
    'LED desk lamp with adjustable brightness. Has USB port for charging phone.',
    'FURNITURE',
    'LIKE_NEW',
    20.00,
    1,
    false,
    true,
    'APPROVED',
    'CVB Lobby',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),

-- SPORTS
(
    (SELECT user_id FROM users WHERE username = 'alex_rod'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Wilson Tennis Racket + Balls',
    'Great for beginners. Comes with 3 new tennis balls.',
    'SPORTS_EQUIPMENT',
    'GOOD',
    40.00,
    1,
    true,
    true,
    'APPROVED',
    'Tennis Courts',
    CURRENT_TIMESTAMP - INTERVAL '6 days'
),
(
    (SELECT user_id FROM users WHERE username = 'kevin_jones'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Yoga Mat + Blocks Set',
    'Purple yoga mat with 2 foam blocks. Used for one semester of yoga class.',
    'SPORTS_EQUIPMENT',
    'GOOD',
    25.00,
    1,
    false,
    true,
    'APPROVED',
    'SRAC',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),

-- SERVICES
(
    (SELECT user_id FROM users WHERE username = 'priya_sharma'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Python Tutoring - $25/hour',
    'CS grad student offering Python tutoring. Can help with homework, projects, or exam prep.',
    'SERVICES',
    'NEW',
    25.00,
    10,
    false,
    true,
    'APPROVED',
    'MLK Library or Zoom',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    (SELECT user_id FROM users WHERE username = 'emily_nguyen'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Resume Design Service',
    'Professional resume design for job applications. Quick turnaround.',
    'SERVICES',
    'NEW',
    30.00,
    5,
    true,
    true,
    'APPROVED',
    'Online delivery',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),

-- OTHER
(
    (SELECT user_id FROM users WHERE username = 'lisa_kim'),
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'Instant Pot Duo 6qt',
    'Multi-cooker, barely used. Great for meal prep.',
    'OTHER',
    'LIKE_NEW',
    55.00,
    1,
    true,
    true,
    'APPROVED',
    'Campus Village',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
);

-- =============================================================================
-- SUMMARY
-- =============================================================================
-- Added: 8 new users
-- Added: 15 new listings
-- Categories covered: Electronics, Textbooks, Clothing, Furniture, Sports Equipment, Services, Other

