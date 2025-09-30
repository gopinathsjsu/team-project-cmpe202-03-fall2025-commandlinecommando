-- verification_queries.sql
-- Comprehensive verification queries to test database integrity and relationships

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Query 1: Verify user inheritance and role consistency
SELECT 
    'User Role Verification' as test_name,
    user_type,
    role,
    COUNT(*) as count,
    CASE 
        WHEN user_type = role THEN 'PASS'
        ELSE 'FAIL'
    END as status
FROM users 
GROUP BY user_type, role
ORDER BY user_type;

-- Query 2: Verify student-specific fields are properly populated
SELECT 
    'Student Fields Verification' as test_name,
    username,
    student_id,
    major,
    graduation_year,
    CASE 
        WHEN user_type = 'STUDENT' AND student_id IS NOT NULL THEN 'PASS'
        WHEN user_type = 'ADMIN' AND student_id IS NULL THEN 'PASS'
        ELSE 'FAIL'
    END as status
FROM users
ORDER BY user_type, username;

-- Query 3: Verify admin permissions are properly assigned
SELECT 
    'Admin Permissions Verification' as test_name,
    u.username,
    u.admin_level,
    COUNT(ap.permission) as permission_count,
    array_agg(ap.permission ORDER BY ap.permission) as permissions
FROM users u
LEFT JOIN admin_permissions ap ON u.user_id = ap.admin_id
WHERE u.user_type = 'ADMIN'
GROUP BY u.user_id, u.username, u.admin_level
ORDER BY u.admin_level;

-- Query 4: Verify listing relationships and constraints
SELECT 
    'Listing Relationships Verification' as test_name,
    l.listing_id,
    l.title,
    u.username as seller,
    l.price,
    l.category,
    l.status,
    COUNT(li.image_id) as image_count
FROM listings l
JOIN users u ON l.seller_id = u.user_id
LEFT JOIN listing_images li ON l.listing_id = li.listing_id
GROUP BY l.listing_id, l.title, u.username, l.price, l.category, l.status
ORDER BY l.listing_id;

-- Query 5: Verify conversation and message integrity
SELECT 
    'Conversation Integrity Verification' as test_name,
    c.conversation_id,
    buyer.username as buyer,
    seller.username as seller,
    l.title as listing_title,
    c.status,
    COUNT(m.message_id) as message_count,
    MAX(m.sent_at) as last_message
FROM conversations c
JOIN users buyer ON c.buyer_id = buyer.user_id
JOIN users seller ON c.seller_id = seller.user_id
JOIN listings l ON c.listing_id = l.listing_id
LEFT JOIN messages m ON c.conversation_id = m.conversation_id
GROUP BY c.conversation_id, buyer.username, seller.username, l.title, c.status
ORDER BY c.conversation_id;

-- Query 6: Verify report system and moderation workflow
SELECT 
    'Report System Verification' as test_name,
    r.report_id,
    reporter.username as reporter,
    l.title as reported_listing,
    r.report_type,
    r.status,
    reviewer.username as reviewed_by,
    r.reviewed_at
FROM reports r
JOIN users reporter ON r.reporter_id = reporter.user_id
LEFT JOIN listings l ON r.listing_id = l.listing_id
LEFT JOIN users reviewer ON r.reviewed_by = reviewer.user_id
ORDER BY r.created_at DESC;

-- Query 7: Verify refresh token management
SELECT 
    'Refresh Token Verification' as test_name,
    rt.id,
    u.username,
    rt.device_info,
    rt.is_revoked,
    rt.expires_at,
    CASE 
        WHEN rt.expires_at > CURRENT_TIMESTAMP AND NOT rt.is_revoked THEN 'VALID'
        WHEN rt.expires_at <= CURRENT_TIMESTAMP THEN 'EXPIRED'
        WHEN rt.is_revoked THEN 'REVOKED'
        ELSE 'UNKNOWN'
    END as token_status
FROM refresh_tokens rt
JOIN users u ON rt.user_id = u.user_id
ORDER BY rt.created_at DESC;

-- Query 8: Verify chatbot query performance and response times
SELECT 
    'Chatbot Performance Verification' as test_name,
    cq.query_id,
    COALESCE(u.username, 'Anonymous') as user,
    LENGTH(cq.query) as query_length,
    LENGTH(COALESCE(cq.response, '')) as response_length,
    cq.processing_time_ms,
    CASE 
        WHEN cq.processing_time_ms IS NULL THEN 'NO_RESPONSE'
        WHEN cq.processing_time_ms < 1000 THEN 'FAST'
        WHEN cq.processing_time_ms < 3000 THEN 'NORMAL'
        ELSE 'SLOW'
    END as performance_rating
FROM chatbot_queries cq
LEFT JOIN users u ON cq.user_id = u.user_id
ORDER BY cq.created_at DESC;

-- Query 9: Database statistics and health check
SELECT 
    'Database Statistics' as test_name,
    'users' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN user_type = 'STUDENT' THEN 1 END) as students,
    COUNT(CASE WHEN user_type = 'ADMIN' THEN 1 END) as admins
FROM users
UNION ALL
SELECT 
    'Database Statistics',
    'listings',
    COUNT(*),
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END),
    COUNT(CASE WHEN status = 'SOLD' THEN 1 END)
FROM listings
UNION ALL
SELECT 
    'Database Statistics',
    'conversations',
    COUNT(*),
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END),
    COUNT(CASE WHEN status = 'CLOSED' THEN 1 END)
FROM conversations
UNION ALL
SELECT 
    'Database Statistics',
    'messages',
    COUNT(*),
    COUNT(CASE WHEN is_read = true THEN 1 END),
    COUNT(CASE WHEN is_read = false THEN 1 END)
FROM messages
UNION ALL
SELECT 
    'Database Statistics',
    'reports',
    COUNT(*),
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END),
    COUNT(CASE WHEN status IN ('RESOLVED', 'DISMISSED') THEN 1 END)
FROM reports;

-- Query 10: Index usage and performance verification
SELECT 
    'Index Verification' as test_name,
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE schemaname = 'public' 
    AND tablename IN ('users', 'listings', 'conversations', 'messages', 'reports', 'refresh_tokens')
ORDER BY tablename, indexname;
