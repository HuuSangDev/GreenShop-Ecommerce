-- ============================================
-- SAMPLE DATA FOR E-COMMERCE PROJECT
-- ============================================

-- 1. Tạo Categories
INSERT INTO categories (category_name, parent_id) VALUES 
('Điện thoại', NULL),
('Laptop', NULL),
('Phụ kiện', NULL),
('Tablet', NULL),
('Đồng hồ thông minh', NULL);

-- Danh mục con
INSERT INTO categories (category_name, parent_id) VALUES 
('iPhone', 1),
('Samsung', 1),
('Xiaomi', 1),
('MacBook', 2),
('Dell', 2),
('Asus', 2);

-- 2. Tạo Permissions
INSERT INTO permissions (name, description) VALUES 
('CREATE_PRODUCT', 'Tạo sản phẩm mới'),
('UPDATE_PRODUCT', 'Cập nhật sản phẩm'),
('DELETE_PRODUCT', 'Xóa sản phẩm'),
('VIEW_ORDER', 'Xem đơn hàng'),
('MANAGE_USER', 'Quản lý người dùng'),
('MANAGE_SHOP', 'Quản lý cửa hàng'),
('APPROVE_PRODUCT', 'Duyệt sản phẩm'),
('VIEW_REPORT', 'Xem báo cáo');

-- 3. Tạo Roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Quản trị viên hệ thống'),
('SELLER', 'Người bán hàng'),
('BUYER', 'Người mua hàng');

-- 4. Gán Permissions cho Roles
-- Admin có tất cả quyền
INSERT INTO role_permissions (role_name, permission_name) VALUES 
('ADMIN', 'CREATE_PRODUCT'),
('ADMIN', 'UPDATE_PRODUCT'),
('ADMIN', 'DELETE_PRODUCT'),
('ADMIN', 'VIEW_ORDER'),
('ADMIN', 'MANAGE_USER'),
('ADMIN', 'MANAGE_SHOP'),
('ADMIN', 'APPROVE_PRODUCT'),
('ADMIN', 'VIEW_REPORT');

-- Seller có quyền quản lý sản phẩm và xem đơn hàng
INSERT INTO role_permissions (role_name, permission_name) VALUES 
('SELLER', 'CREATE_PRODUCT'),
('SELLER', 'UPDATE_PRODUCT'),
('SELLER', 'DELETE_PRODUCT'),
('SELLER', 'VIEW_ORDER'),
('SELLER', 'MANAGE_SHOP');

-- Buyer chỉ xem đơn hàng của mình
INSERT INTO role_permissions (role_name, permission_name) VALUES 
('BUYER', 'VIEW_ORDER');

-- 5. Tạo Users
-- Password: 123456 (đã mã hóa bằng BCrypt)
INSERT INTO users (id, username, password, email, full_name, phone_number, active, created_at) VALUES 
(UUID(), 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@ecommerce.com', 'Admin System', '0900000000', true, NOW()),
(UUID(), 'seller1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller1@example.com', 'Nguyễn Văn A', '0901234567', true, NOW()),
(UUID(), 'seller2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller2@example.com', 'Trần Thị B', '0902345678', true, NOW()),
(UUID(), 'buyer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'buyer1@example.com', 'Lê Văn C', '0903456789', true, NOW());

-- 6. Gán Roles cho Users
SET @admin_id = (SELECT id FROM users WHERE username = 'admin');
SET @seller1_id = (SELECT id FROM users WHERE username = 'seller1');
SET @seller2_id = (SELECT id FROM users WHERE username = 'seller2');
SET @buyer1_id = (SELECT id FROM users WHERE username = 'buyer1');

INSERT INTO user_roles (user_id, role_name) VALUES 
(@admin_id, 'ADMIN'),
(@seller1_id, 'SELLER'),
(@seller2_id, 'SELLER'),
(@buyer1_id, 'BUYER');

-- 7. Tạo Shops
INSERT INTO shops (shop_name, description, rating, owner_id, created_at) VALUES 
('Apple Store VN', 'Cửa hàng Apple chính hãng tại Việt Nam', 4.8, @seller1_id, NOW()),
('Samsung Official Store', 'Cửa hàng Samsung chính hãng', 4.7, @seller2_id, NOW());

-- 8. Tạo Products
SET @shop1_id = (SELECT id FROM shops WHERE shop_name = 'Apple Store VN');
SET @shop2_id = (SELECT id FROM shops WHERE shop_name = 'Samsung Official Store');
SET @category_iphone = (SELECT id FROM categories WHERE category_name = 'iPhone');
SET @category_samsung = (SELECT id FROM categories WHERE category_name = 'Samsung');

-- Products của Apple Store
INSERT INTO products (product_name, description, price, stock_quantity, image_url, available, shop_id, category_id, created_at) VALUES 
('iPhone 15 Pro Max', 'Điện thoại cao cấp với chip A17 Pro, camera 48MP, màn hình Super Retina XDR 6.7 inch', 29990000, 100, 'https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-blue-1.jpg', true, @shop1_id, @category_iphone, NOW()),
('iPhone 15 Pro', 'Điện thoại cao cấp với chip A17 Pro, camera 48MP, màn hình 6.1 inch', 24990000, 150, 'https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-black.jpg', true, @shop1_id, @category_iphone, NOW()),
('iPhone 15', 'Điện thoại với chip A16 Bionic, camera 48MP, màn hình 6.1 inch', 19990000, 200, 'https://cdn.tgdd.vn/Products/Images/42/303891/iphone-15-pink.jpg', true, @shop1_id, @category_iphone, NOW());

-- Products của Samsung Store
INSERT INTO products (product_name, description, price, stock_quantity, image_url, available, shop_id, category_id, created_at) VALUES 
('Samsung Galaxy S24 Ultra', 'Flagship với chip Snapdragon 8 Gen 3, camera 200MP, màn hình Dynamic AMOLED 6.8 inch', 27990000, 80, 'https://cdn.tgdd.vn/Products/Images/42/320721/samsung-galaxy-s24-ultra-grey-1.jpg', true, @shop2_id, @category_samsung, NOW()),
('Samsung Galaxy S24+', 'Điện thoại cao cấp với chip Snapdragon 8 Gen 3, camera 50MP', 22990000, 120, 'https://cdn.tgdd.vn/Products/Images/42/320722/samsung-galaxy-s24-plus-violet.jpg', true, @shop2_id, @category_samsung, NOW());

-- 9. Tạo Product Variants
SET @product_ip15pm = (SELECT id FROM products WHERE product_name = 'iPhone 15 Pro Max');
SET @product_ip15p = (SELECT id FROM products WHERE product_name = 'iPhone 15 Pro');
SET @product_s24u = (SELECT id FROM products WHERE product_name = 'Samsung Galaxy S24 Ultra');

-- Variants cho iPhone 15 Pro Max
INSERT INTO product_variants (product_id, variant_name, price, stock_quantity, sku) VALUES 
(@product_ip15pm, 'Titan Tự Nhiên - 256GB', 29990000, 50, 'IP15PM-TN-256'),
(@product_ip15pm, 'Titan Xanh - 256GB', 29990000, 30, 'IP15PM-TX-256'),
(@product_ip15pm, 'Titan Đen - 512GB', 34990000, 20, 'IP15PM-TD-512');

-- Variants cho iPhone 15 Pro
INSERT INTO product_variants (product_id, variant_name, price, stock_quantity, sku) VALUES 
(@product_ip15p, 'Titan Trắng - 128GB', 24990000, 60, 'IP15P-TT-128'),
(@product_ip15p, 'Titan Đen - 256GB', 27990000, 50, 'IP15P-TD-256');

-- Variants cho Samsung S24 Ultra
INSERT INTO product_variants (product_id, variant_name, price, stock_quantity, sku) VALUES 
(@product_s24u, 'Xám - 256GB', 27990000, 40, 'S24U-X-256'),
(@product_s24u, 'Tím - 512GB', 31990000, 30, 'S24U-T-512'),
(@product_s24u, 'Đen - 1TB', 37990000, 10, 'S24U-D-1TB');

-- 10. Tạo Orders mẫu
INSERT INTO orders (buyer_id, total_amount, status, shipping_address, payment_method, created_at) VALUES 
(@buyer1_id, 29990000, 'DELIVERED', '123 Nguyễn Huệ, Q1, TP.HCM', 'COD', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(@buyer1_id, 54980000, 'PROCESSING', '123 Nguyễn Huệ, Q1, TP.HCM', 'BANK_TRANSFER', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 11. Tạo Order Items
SET @order1_id = (SELECT id FROM orders WHERE buyer_id = @buyer1_id ORDER BY created_at LIMIT 1);
SET @order2_id = (SELECT id FROM orders WHERE buyer_id = @buyer1_id ORDER BY created_at DESC LIMIT 1);

INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES 
(@order1_id, @product_ip15pm, 1, 29990000),
(@order2_id, @product_ip15p, 1, 24990000),
(@order2_id, @product_ip15pm, 1, 29990000);

-- 12. Tạo Reviews
INSERT INTO reviews (product_id, user_id, rating, comment, created_at) VALUES 
(@product_ip15pm, @buyer1_id, 5, 'Sản phẩm tuyệt vời, camera đẹp, pin trâu!', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(@product_ip15p, @buyer1_id, 4, 'Máy đẹp nhưng hơi nóng khi chơi game', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(@product_s24u, @buyer1_id, 5, 'Camera 200MP quá đỉnh, màn hình sắc nét', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 13. Tạo Cart items
INSERT INTO carts (user_id, product_id, quantity) VALUES 
(@buyer1_id, @product_s24u, 1),
(@buyer1_id, @product_ip15p, 2);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Kiểm tra số lượng records
SELECT 'Categories' as Table_Name, COUNT(*) as Count FROM categories
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Shops', COUNT(*) FROM shops
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Product Variants', COUNT(*) FROM product_variants
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items
UNION ALL
SELECT 'Reviews', COUNT(*) FROM reviews
UNION ALL
SELECT 'Carts', COUNT(*) FROM carts;

-- Xem thông tin shops và số sản phẩm
SELECT 
    s.id,
    s.shop_name,
    u.full_name as owner_name,
    COUNT(p.id) as total_products,
    s.rating
FROM shops s
JOIN users u ON s.owner_id = u.id
LEFT JOIN products p ON p.shop_id = s.id
GROUP BY s.id, s.shop_name, u.full_name, s.rating;

-- Xem sản phẩm và variants
SELECT 
    p.id,
    p.product_name,
    p.price as base_price,
    s.shop_name,
    c.category_name,
    COUNT(pv.id) as variant_count
FROM products p
JOIN shops s ON p.shop_id = s.id
JOIN categories c ON p.category_id = c.id
LEFT JOIN product_variants pv ON pv.product_id = p.id
GROUP BY p.id, p.product_name, p.price, s.shop_name, c.category_name;
