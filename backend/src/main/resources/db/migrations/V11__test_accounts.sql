-- Seed deterministic test accounts for automated testing flows

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
) VALUES (
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'test.buyer@sjsu.edu',
    'test_buyer',
    '$2b$10$bp.Mm/UQq9GC5d0r7okbI.9zUu76O6a2QXCrdxX.h//p6DjsKJHsy',
    'Test',
    'Buyer',
    '408-555-0999',
    'BUYER',
    'VERIFIED',
    'TEST001',
    'test.buyer@sjsu.edu',
    2026,
    'Software Engineering',
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

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
) VALUES (
    (SELECT university_id FROM universities WHERE domain = 'sjsu.edu'),
    'test.admin@sjsu.edu',
    'test_admin',
    '$2b$10$bp.Mm/UQq9GC5d0r7okbI.9zUu76O6a2QXCrdxX.h//p6DjsKJHsy',
    'Test',
    'Admin',
    '408-555-0998',
    'ADMIN',
    'VERIFIED',
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;
