-- verification_queries.sql
-- Comprehensive verification queries for Campus Marketplace database
-- Tests schema integrity, relationships, and data consistency
-- Generated: 2024-10-29

-- =====================================================
-- BASIC DATA VERIFICATION
-- =====================================================

-- Query 1: Database statistics overview
SELECT
    'Database Statistics' as test_name,
    'Universities' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_count
FROM universities
UNION ALL
SELECT
    'Database Statistics',
    'Users',
    COUNT(*),
    COUNT(CASE WHEN is_active = true THEN 1 END)
FROM users
UNION ALL
SELECT
    'Database Statistics',
    'Products',
    COUNT(*),
    COUNT(CASE WHEN is_active = true THEN 1 END)
FROM products
UNION ALL
SELECT
    'Database Statistics',
    'Orders',
    COUNT(*),
    COUNT(CASE WHEN status != 'CANCELLED' THEN 1 END)
FROM orders
UNION ALL
SELECT
    'Database Statistics',
    'Order Items',
    COUNT(*),
    COUNT(CASE WHEN fulfillment_status != 'CANCELLED' THEN 1 END)
FROM order_items
UNION ALL
SELECT
    'Database Statistics',
    'Refresh Tokens',
    COUNT(*),
    COUNT(CASE WHEN is_revoked = false THEN 1 END)
FROM refresh_tokens;

-- =====================================================
-- UNIVERSITY VERIFICATION
-- =====================================================

-- Query 2: Verify university data integrity
SELECT
    'University Verification' as test_name,
    university_id,
    name,
    domain,
    city,
    state,
    is_active,
    CASE
        WHEN domain IS NOT NULL AND domain LIKE '%.%' THEN 'PASS'
        ELSE 'FAIL - Invalid domain format'
    END as domain_check
FROM universities
ORDER BY name;

-- =====================================================
-- USER VERIFICATION
-- =====================================================

-- Query 3: Verify user role and verification status consistency
SELECT
    'User Role Verification' as test_name,
    role,
    verification_status,
    COUNT(*) as user_count,
    CASE
        WHEN role IN ('STUDENT', 'ADMIN') AND verification_status IN ('PENDING', 'VERIFIED', 'SUSPENDED') THEN 'PASS'
        ELSE 'FAIL - Invalid role or status'
    END as validation_status
FROM users
GROUP BY role, verification_status
ORDER BY role, verification_status;

-- Query 4: Verify student-specific fields
SELECT
    'Student Fields Verification' as test_name,
    user_id,
    username,
    role,
    student_id,
    major,
    graduation_year,
    university_email,
    CASE
        WHEN role = 'STUDENT' AND student_id IS NOT NULL THEN 'PASS'
        WHEN role = 'ADMIN' AND student_id IS NULL THEN 'PASS'
        ELSE 'WARN - Student fields mismatch with role'
    END as validation_status
FROM users
ORDER BY role, username;

-- Query 5: Verify user-university relationships
SELECT
    'User-University Relationships' as test_name,
    u.username,
    u.role,
    univ.name as university_name,
    univ.domain,
    CASE
        WHEN u.role = 'STUDENT' AND u.university_id IS NOT NULL THEN 'PASS'
        WHEN u.role = 'ADMIN' THEN 'N/A - Admin'
        ELSE 'WARN - Student without university'
    END as validation_status
FROM users u
LEFT JOIN universities univ ON u.university_id = univ.university_id
ORDER BY u.role, u.username;

-- =====================================================
-- PRODUCT VERIFICATION
-- =====================================================

-- Query 6: Verify product data integrity and moderation status
SELECT
    'Product Verification' as test_name,
    p.product_id,
    p.title,
    u.username as seller,
    p.category,
    p.condition,
    p.price,
    p.quantity,
    p.moderation_status,
    p.is_active,
    CASE
        WHEN p.price >= 0 AND p.quantity >= 0 THEN 'PASS'
        ELSE 'FAIL - Invalid price or quantity'
    END as validation_status
FROM products p
JOIN users u ON p.seller_id = u.user_id
ORDER BY p.created_at DESC;

-- Query 7: Products by category and status
SELECT
    'Products by Category' as test_name,
    category,
    moderation_status,
    COUNT(*) as product_count,
    AVG(price) as avg_price,
    SUM(view_count) as total_views
FROM products
GROUP BY category, moderation_status
ORDER BY category, moderation_status;

-- Query 8: Verify product-seller-university relationships
SELECT
    'Product Relationships' as test_name,
    p.title,
    seller.username as seller_username,
    seller_univ.name as seller_university,
    prod_univ.name as product_university,
    CASE
        WHEN p.university_id = seller.university_id OR p.university_id IS NULL THEN 'PASS'
        ELSE 'WARN - Product university mismatch'
    END as validation_status
FROM products p
JOIN users seller ON p.seller_id = seller.user_id
LEFT JOIN universities seller_univ ON seller.university_id = seller_univ.university_id
LEFT JOIN universities prod_univ ON p.university_id = prod_univ.university_id
ORDER BY p.created_at DESC;

-- =====================================================
-- ORDER VERIFICATION
-- =====================================================

-- Query 9: Verify order lifecycle and status progression
SELECT
    'Order Lifecycle Verification' as test_name,
    o.order_id,
    o.order_number,
    buyer.username as buyer,
    o.status,
    o.total_amount,
    o.delivery_method,
    COUNT(oi.order_item_id) as item_count,
    CASE
        WHEN o.status = 'CART' AND o.ordered_at IS NULL THEN 'PASS - Cart'
        WHEN o.status != 'CART' AND o.ordered_at IS NOT NULL THEN 'PASS - Ordered'
        WHEN o.status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED') AND o.paid_at IS NOT NULL THEN 'PASS - Paid'
        ELSE 'WARN - Status timestamp mismatch'
    END as validation_status
FROM orders o
JOIN users buyer ON o.buyer_id = buyer.user_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY o.order_id, o.order_number, buyer.username, o.status, o.total_amount, o.delivery_method, o.ordered_at, o.paid_at
ORDER BY o.created_at DESC;

-- Query 10: Verify order pricing calculations
SELECT
    'Order Pricing Verification' as test_name,
    o.order_id,
    o.order_number,
    o.subtotal,
    o.tax_amount,
    o.delivery_fee,
    o.platform_fee,
    o.total_amount,
    (o.subtotal + o.tax_amount + o.delivery_fee + o.platform_fee) as calculated_total,
    CASE
        WHEN ABS(o.total_amount - (o.subtotal + o.tax_amount + o.delivery_fee + o.platform_fee)) < 0.01 THEN 'PASS'
        ELSE 'FAIL - Total amount mismatch'
    END as validation_status
FROM orders o
WHERE o.status != 'CART'
ORDER BY o.created_at DESC;

-- Query 11: Order status summary
SELECT
    'Order Status Summary' as test_name,
    status,
    COUNT(*) as order_count,
    SUM(total_amount) as total_revenue,
    AVG(total_amount) as avg_order_value
FROM orders
WHERE status != 'CART'
GROUP BY status
ORDER BY status;

-- =====================================================
-- ORDER ITEM VERIFICATION
-- =====================================================

-- Query 12: Verify order items and product snapshots
SELECT
    'Order Item Verification' as test_name,
    oi.order_item_id,
    o.order_number,
    oi.product_title,
    oi.product_condition,
    oi.unit_price,
    oi.quantity,
    oi.total_price,
    (oi.unit_price * oi.quantity) as calculated_total,
    oi.fulfillment_status,
    CASE
        WHEN ABS(oi.total_price - (oi.unit_price * oi.quantity)) < 0.01 THEN 'PASS'
        ELSE 'FAIL - Total price mismatch'
    END as validation_status
FROM order_items oi
JOIN orders o ON oi.order_id = o.order_id
ORDER BY o.created_at DESC;

-- Query 13: Order items by seller
SELECT
    'Order Items by Seller' as test_name,
    seller.username as seller,
    COUNT(oi.order_item_id) as items_sold,
    SUM(oi.total_price) as total_sales,
    AVG(oi.unit_price) as avg_item_price
FROM order_items oi
JOIN users seller ON oi.seller_id = seller.user_id
GROUP BY seller.username
ORDER BY total_sales DESC;

-- =====================================================
-- REFRESH TOKEN VERIFICATION
-- =====================================================

-- Query 14: Verify refresh token status and expiry
SELECT
    'Refresh Token Verification' as test_name,
    rt.id,
    u.username,
    rt.device_info,
    rt.is_revoked,
    rt.expires_at,
    rt.created_at,
    CASE
        WHEN rt.expires_at > CURRENT_TIMESTAMP AND NOT rt.is_revoked THEN 'VALID'
        WHEN rt.expires_at <= CURRENT_TIMESTAMP THEN 'EXPIRED'
        WHEN rt.is_revoked THEN 'REVOKED'
        ELSE 'UNKNOWN'
    END as token_status,
    CASE
        WHEN rt.expires_at > rt.created_at THEN 'PASS'
        ELSE 'FAIL - Invalid expiry date'
    END as validation_status
FROM refresh_tokens rt
JOIN users u ON rt.user_id = u.user_id
ORDER BY rt.created_at DESC;

-- =====================================================
-- CROSS-TABLE RELATIONSHIP VERIFICATION
-- =====================================================

-- Query 15: Verify referential integrity (orphaned records check)
SELECT
    'Referential Integrity Check' as test_name,
    'Products without sellers' as check_type,
    COUNT(*) as orphaned_count
FROM products p
LEFT JOIN users u ON p.seller_id = u.user_id
WHERE u.user_id IS NULL
UNION ALL
SELECT
    'Referential Integrity Check',
    'Orders without buyers',
    COUNT(*)
FROM orders o
LEFT JOIN users u ON o.buyer_id = u.user_id
WHERE u.user_id IS NULL
UNION ALL
SELECT
    'Referential Integrity Check',
    'Order items without orders',
    COUNT(*)
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL
UNION ALL
SELECT
    'Referential Integrity Check',
    'Order items without products',
    COUNT(*)
FROM order_items oi
LEFT JOIN products p ON oi.product_id = p.product_id
WHERE p.product_id IS NULL
UNION ALL
SELECT
    'Referential Integrity Check',
    'Refresh tokens without users',
    COUNT(*)
FROM refresh_tokens rt
LEFT JOIN users u ON rt.user_id = u.user_id
WHERE u.user_id IS NULL;

-- =====================================================
-- INDEX VERIFICATION
-- =====================================================

-- Query 16: Verify indexes exist for performance
SELECT
    'Index Verification' as test_name,
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
    AND tablename IN ('universities', 'users', 'products', 'orders', 'order_items', 'refresh_tokens')
ORDER BY tablename, indexname;

-- =====================================================
-- DATA QUALITY CHECKS
-- =====================================================

-- Query 17: Check for potential data quality issues
SELECT 'Data Quality Checks' as test_name, 'Users with invalid emails' as check_type, COUNT(*) as issue_count
FROM users WHERE email NOT LIKE '%@%.%'
UNION ALL
SELECT 'Data Quality Checks', 'Products with zero or negative price', COUNT(*) FROM products WHERE price <= 0
UNION ALL
SELECT 'Data Quality Checks', 'Products with negative quantity', COUNT(*) FROM products WHERE quantity < 0
UNION ALL
SELECT 'Data Quality Checks', 'Orders with negative total', COUNT(*) FROM orders WHERE total_amount < 0
UNION ALL
SELECT 'Data Quality Checks', 'Expired but not revoked tokens', COUNT(*)
FROM refresh_tokens WHERE expires_at < CURRENT_TIMESTAMP AND is_revoked = false;

-- =====================================================
-- BUSINESS METRICS
-- =====================================================

-- Query 18: Key business metrics
SELECT
    'Business Metrics' as test_name,
    'Active users' as metric,
    COUNT(*)::TEXT as value
FROM users WHERE is_active = true
UNION ALL
SELECT 'Business Metrics', 'Verified students', COUNT(*)::TEXT
FROM users WHERE role = 'STUDENT' AND verification_status = 'VERIFIED'
UNION ALL
SELECT 'Business Metrics', 'Active products', COUNT(*)::TEXT
FROM products WHERE is_active = true AND moderation_status = 'APPROVED'
UNION ALL
SELECT 'Business Metrics', 'Pending products (need moderation)', COUNT(*)::TEXT
FROM products WHERE moderation_status = 'PENDING'
UNION ALL
SELECT 'Business Metrics', 'Completed orders', COUNT(*)::TEXT
FROM orders WHERE status = 'COMPLETED'
UNION ALL
SELECT 'Business Metrics', 'Total revenue (completed orders)', COALESCE(SUM(total_amount), 0)::TEXT
FROM orders WHERE status = 'COMPLETED'
UNION ALL
SELECT 'Business Metrics', 'Average order value', COALESCE(AVG(total_amount), 0)::TEXT
FROM orders WHERE status IN ('COMPLETED', 'DELIVERED', 'SHIPPED');

-- =====================================================
-- SUMMARY REPORT
-- =====================================================

-- Query 19: Full database summary
SELECT
    'CAMPUS MARKETPLACE DATABASE SUMMARY' as report_title,
    '' as separator,
    CONCAT('Universities: ', (SELECT COUNT(*) FROM universities)) as line1,
    CONCAT('Total Users: ', (SELECT COUNT(*) FROM users)) as line2,
    CONCAT('Products: ', (SELECT COUNT(*) FROM products)) as line3,
    CONCAT('Orders: ', (SELECT COUNT(*) FROM orders WHERE status != 'CART')) as line4,
    CONCAT('Active Carts: ', (SELECT COUNT(*) FROM orders WHERE status = 'CART')) as line5;
