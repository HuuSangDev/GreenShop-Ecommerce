-- ============================================================
-- GREENSHOP SEED DATA - FINAL VERSION
-- Dựa trên entities thực tế của dự án
-- ============================================================
USE ecommerce;

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================
-- 1. CATEGORIES
-- ============================================================
INSERT INTO categories (name, slug, description, parent_id, level, sort_order, is_active, created_at, updated_at) VALUES
('Điện thoại',      'dien-thoai',       'Điện thoại di động',         NULL, 0, 1, 1, NOW(), NOW()),
('Laptop',          'laptop',           'Máy tính xách tay',          NULL, 0, 2, 1, NOW(), NOW()),
('Thời trang',      'thoi-trang',       'Quần áo, phụ kiện',          NULL, 0, 3, 1, NOW(), NOW()),
('Đồ gia dụng',     'do-gia-dung',      'Thiết bị gia đình',          NULL, 0, 4, 1, NOW(), NOW()),
('Sách',            'sach',             'Sách các thể loại',          NULL, 0, 5, 1, NOW(), NOW()),
('iPhone',          'iphone',           'Điện thoại Apple iPhone',    1,    1, 1, 1, NOW(), NOW()),
('Android',         'android',          'Điện thoại Android',         1,    1, 2, 1, NOW(), NOW()),
('Gaming Laptop',   'gaming-laptop',    'Laptop chơi game',           2,    1, 1, 1, NOW(), NOW()),
('Văn phòng',       'laptop-van-phong', 'Laptop văn phòng',           2,    1, 2, 1, NOW(), NOW()),
('Áo nam',          'ao-nam',           'Áo các loại cho nam',        3,    1, 1, 1, NOW(), NOW());

-- ============================================================
-- 2. USERS (password = BCrypt của "123456")
-- ============================================================
-- BCrypt hash của "123456": $2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.
INSERT INTO users (id, username, email, password, full_name, phone_number, active, created_at) VALUES
('user-admin-03',  'admin_system',     'admin@test3.com',    '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Quản Trị Viên Hệ Thống', '0900000000', 1, NOW()),
('user-seller-01', 'seller_techzone',  'seller1@test.com',  '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Nguyễn Văn Seller',  '0901111111', 1, NOW()),
('user-seller-02', 'seller_fashion',   'seller2@test.com',  '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Trần Thị Fashion',   '0902222222', 1, NOW()),
('user-seller-03', 'seller_books',     'seller3@test.com',  '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Lê Văn Books',       '0903333333', 1, NOW()),
('user-buyer-01',  'buyer_nguyen',     'buyer1@test.com',   '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Phạm Thị Buyer',     '0904444444', 1, NOW()),
('user-buyer-02',  'buyer_tran',       'buyer2@test.com',   '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Hoàng Văn Mua',      '0905555555', 1, NOW()),
('user-buyer-03',  'buyer_le',         'buyer3@test.com',   '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Vũ Thị Khách',       '0906666666', 1, NOW()),
('user-banned-01', 'banned_user',      'banned@test.com',   '$2a$10$d8bIshBQYR6gN4q2MMOgoOirCl5..Q5ftOTOPolo2Wp7FLnjyWXh.', 'Tài Khoản Bị Khóa',  '0907777777', 0, NOW());

-- Gán role ADMIN cho admin
INSERT INTO user_roles (user_id, role_name) VALUES
('user-admin-03', 'ADMIN');


-- Gán role SELLER cho 3 seller
INSERT INTO user_roles (user_id, role_name) VALUES
('user-seller-01', 'SELLER'),
('user-seller-02', 'SELLER'),
('user-seller-03', 'SELLER');

-- Gán role BUYER cho tất cả (kể cả admin, seller)
INSERT INTO user_roles (user_id, role_name) VALUES
('user-admin-03',  'BUYER'),
('user-seller-01', 'BUYER'),
('user-seller-02', 'BUYER'),
('user-seller-03', 'BUYER'),
('user-buyer-01',  'BUYER'),
('user-buyer-02',  'BUYER'),
('user-buyer-03',  'BUYER'),
('user-banned-01', 'BUYER');

-- ============================================================
-- 3. SHOPS + SHOP WALLETS
-- ============================================================
INSERT INTO shops (owner_id, shop_name, description, rating, status, created_at) VALUES
('user-seller-01', 'TechZone Store',    'Chuyên điện thoại, laptop chính hãng',  4.8, 'ACTIVE', NOW()),
('user-seller-02', 'Fashion Hub',       'Thời trang nam nữ cao cấp',             4.5, 'ACTIVE', NOW()),
('user-seller-03', 'Book World',        'Sách giáo khoa, văn học, kỹ năng',      4.7, 'ACTIVE', NOW());

-- ============================================================
-- 4. PRODUCTS + VARIANTS (shop_id 1 = TechZone, 2 = Fashion, 3 = Book)
-- ============================================================
-- Thêm cột sold_count nếu chưa có
ALTER TABLE products ADD COLUMN IF NOT EXISTS sold_count INT DEFAULT 0;
ALTER TABLE products ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3,2) DEFAULT 0.00;
ALTER TABLE products ADD COLUMN IF NOT EXISTS total_reviews INT DEFAULT 0;

INSERT INTO products (shop_id, category_id, product_name, description, price, stock_quantity, image_url, available, sold_count, average_rating, total_reviews, created_at) VALUES
-- TechZone (shop 1) — Điện thoại
(1, 6,  'iPhone 15 Pro Max',       'iPhone 15 Pro Max 256GB, chip A17 Pro',         34990000, 50,  NULL, 1, 3, 4.67, 3, NOW()),
(1, 6,  'iPhone 14',               'iPhone 14 128GB, màu đen, trắng, đỏ',           22990000, 80,  NULL, 1, 2, 4.00, 2, NOW()),
(1, 7,  'Samsung Galaxy S24 Ultra','Samsung S24 Ultra 512GB, bút S-Pen',            29990000, 30,  NULL, 1, 3, 4.67, 3, NOW()),
(1, 7,  'Xiaomi 14 Pro',           'Xiaomi 14 Pro 256GB, camera Leica',             18990000, 45,  NULL, 1, 0, 0.00, 0, NOW()),
(1, 8,  'ASUS ROG Strix G16',      'Laptop gaming RTX 4070, i9-13900H, 32GB RAM',  45990000, 15,  NULL, 1, 0, 0.00, 0, NOW()),
(1, 9,  'MacBook Air M3',          'MacBook Air 15 inch M3, 16GB RAM, 512GB SSD',   38990000, 20,  NULL, 1, 0, 0.00, 0, NOW()),
-- Fashion Hub (shop 2) — Thời trang
(2, 10, 'Áo Polo Nam Cao Cấp',     'Áo polo cotton 100%, form slim fit',            450000,   200, NULL, 1, 3, 4.67, 3, NOW()),
(2, 10, 'Áo Thun Oversize',        'Áo thun oversize unisex, nhiều màu',            280000,   350, NULL, 1, 1, 0.00, 0, NOW()),
(2, 3,  'Quần Jeans Nam Slim',     'Quần jeans nam slim fit, co giãn 4 chiều',      650000,   150, NULL, 1, 0, 0.00, 0, NOW()),
(2, 3,  'Váy Maxi Nữ Hoa',        'Váy maxi dài hoa nhí, chất liệu lụa mềm',      520000,   120, NULL, 1, 0, 0.00, 0, NOW()),
-- Book World (shop 3) — Sách
(3, 5,  'Đắc Nhân Tâm',           'Dale Carnegie - Nghệ thuật thu phục lòng người', 89000,   500, NULL, 1, 3, 5.00, 3, NOW()),
(3, 5,  'Nhà Giả Kim',            'Paulo Coelho - Tiểu thuyết triết học nổi tiếng', 75000,   400, NULL, 1, 1, 0.00, 0, NOW()),
(3, 5,  'Atomic Habits',          'James Clear - Thói quen nguyên tử',              95000,   300, NULL, 1, 0, 0.00, 0, NOW()),
(3, 5,  'Sapiens',                'Yuval Noah Harari - Lược sử loài người',         120000,  250, NULL, 1, 0, 0.00, 0, NOW());

-- ============================================================
-- 5. PRODUCT VARIANTS
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, price, stock_quantity, sku, weight, version) VALUES
-- iPhone 15 Pro Max (product 1)
(1, '256GB - Titan Đen',    34990000, 20, 'IP15PM-256-BLK', 221, 0),
(1, '512GB - Titan Trắng',  37990000, 15, 'IP15PM-512-WHT', 221, 0),
(1, '1TB - Titan Xanh',     42990000, 15, 'IP15PM-1TB-BLU', 221, 0),
-- iPhone 14 (product 2)
(2, '128GB - Đen',          22990000, 30, 'IP14-128-BLK', 172, 0),
(2, '256GB - Trắng',        25990000, 30, 'IP14-256-WHT', 172, 0),
(2, '128GB - Đỏ',           22990000, 20, 'IP14-128-RED', 172, 0),
-- Samsung S24 Ultra (product 3)
(3, '256GB - Đen Titanium', 29990000, 15, 'S24U-256-BLK', 232, 0),
(3, '512GB - Xám Titanium', 33990000, 15, 'S24U-512-GRY', 232, 0),
-- Xiaomi 14 Pro (product 4)
(4, '256GB - Đen',          18990000, 25, 'MI14P-256-BLK', 223, 0),
(4, '512GB - Trắng',        21990000, 20, 'MI14P-512-WHT', 223, 0),
-- ASUS ROG (product 5)
(5, 'RTX 4070 - 16GB RAM',  45990000, 8,  'ROG-G16-4070', 2500, 0),
(5, 'RTX 4080 - 32GB RAM',  55990000, 7,  'ROG-G16-4080', 2500, 0),
-- MacBook Air M3 (product 6)
(6, '16GB RAM - 512GB SSD', 38990000, 10, 'MBA-M3-16-512', 1510, 0),
(6, '24GB RAM - 1TB SSD',   45990000, 10, 'MBA-M3-24-1TB', 1510, 0),
-- Áo Polo (product 7)
(7, 'Size S - Trắng',  450000, 50, 'POLO-S-WHT', 200, 0),
(7, 'Size M - Trắng',  450000, 60, 'POLO-M-WHT', 200, 0),
(7, 'Size L - Đen',    450000, 50, 'POLO-L-BLK', 200, 0),
(7, 'Size XL - Xanh',  450000, 40, 'POLO-XL-BLU', 200, 0),
-- Áo Thun Oversize (product 8)
(8, 'Size M - Trắng',  280000, 80, 'OVER-M-WHT', 180, 0),
(8, 'Size L - Đen',    280000, 90, 'OVER-L-BLK', 180, 0),
(8, 'Size XL - Xám',   280000, 70, 'OVER-XL-GRY', 180, 0),
-- Quần Jeans (product 9)
(9, 'Size 30 - Xanh đậm', 650000, 40, 'JEAN-30-DBL', 500, 0),
(9, 'Size 32 - Xanh nhạt',650000, 40, 'JEAN-32-LBL', 500, 0),
(9, 'Size 34 - Đen',      650000, 30, 'JEAN-34-BLK', 500, 0),
-- Váy Maxi (product 10)
(10, 'Size S - Hoa Hồng',  520000, 30, 'MAXI-S-PNK', 300, 0),
(10, 'Size M - Hoa Xanh',  520000, 35, 'MAXI-M-BLU', 300, 0),
(10, 'Size L - Hoa Vàng',  520000, 25, 'MAXI-L-YLW', 300, 0),
-- Sách (product 11-14, mỗi sách 1 variant)
(11, 'Bìa mềm',  89000, 200, 'BOOK-DNT-001', 300, 0),
(12, 'Bìa mềm',  75000, 180, 'BOOK-NHK-001', 250, 0),
(13, 'Bìa cứng', 95000, 150, 'BOOK-AH-001',  350, 0),
(14, 'Bìa mềm', 120000, 100, 'BOOK-SAP-001', 400, 0);

-- ============================================================
-- 6. ORDERS + SHOP_ORDERS + ORDER_ITEMS (nhiều trạng thái để test)
-- ============================================================
-- Fix cột status: đảm bảo VARCHAR để chứa tất cả enum values
ALTER TABLE orders MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE shop_orders MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Lấy shop IDs thực tế (auto_increment có thể khác 1,2,3)
SET @shop1 = (SELECT id FROM shops WHERE owner_id = 'user-seller-01' LIMIT 1);
SET @shop2 = (SELECT id FROM shops WHERE owner_id = 'user-seller-02' LIMIT 1);
SET @shop3 = (SELECT id FROM shops WHERE owner_id = 'user-seller-03' LIMIT 1);

-- Shop Wallets
INSERT INTO shopWallets (id, shop_id, balance, total_earned, total_withdrawn) VALUES
(UUID(), @shop1, 15500000.00, 45000000.00, 29500000.00),
(UUID(), @shop2, 8200000.00,  22000000.00, 13800000.00),
(UUID(), @shop3, 3750000.00,  9500000.00,  5750000.00);

-- Admin Wallet (ví của admin hệ thống)
INSERT INTO adminWallets (id, balance, total_earned, total_withdrawn) VALUES
(UUID(), 25000000.00, 150000000.00, 125000000.00);

-- Orders
INSERT INTO orders (buyer_id, total_amount, discount_amount, final_amount, status, payment_method, created_at) VALUES
('user-buyer-01', 34990000, 0,       34990000, 'COMPLETED',  'COD',   DATE_SUB(NOW(), INTERVAL 10 DAY)),
('user-buyer-01', 22990000, 0,       22990000, 'DELIVERED',  'COD',   DATE_SUB(NOW(), INTERVAL 7 DAY)),
('user-buyer-02', 29990000, 0,       29990000, 'SHIPPED',    'SEPAY', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('user-buyer-02', 730000,   0,       730000,   'PREPARING',  'COD',   DATE_SUB(NOW(), INTERVAL 2 DAY)),
('user-buyer-03', 164000,   0,       164000,   'PENDING',    'COD',   DATE_SUB(NOW(), INTERVAL 1 DAY)),
('user-buyer-03', 45990000, 0,       45990000, 'CANCELLED',  'COD',   DATE_SUB(NOW(), INTERVAL 5 DAY)),
('user-buyer-01', 38990000, 1000000, 37990000, 'PREPARING',  'SEPAY', DATE_SUB(NOW(), INTERVAL 4 DAY)),
('user-buyer-02', 89000,    0,       89000,    'PENDING',    'COD',   NOW());

-- Shop Orders (dùng biến @shop1,2,3)
SET @o1 = (SELECT id FROM orders WHERE buyer_id='user-buyer-01' AND status='COMPLETED' LIMIT 1);
SET @o2 = (SELECT id FROM orders WHERE buyer_id='user-buyer-01' AND status='DELIVERED' LIMIT 1);
SET @o3 = (SELECT id FROM orders WHERE buyer_id='user-buyer-02' AND status='SHIPPED'   LIMIT 1);
SET @o4 = (SELECT id FROM orders WHERE buyer_id='user-buyer-02' AND status='PREPARING' LIMIT 1);
SET @o5 = (SELECT id FROM orders WHERE buyer_id='user-buyer-03' AND status='PENDING' ORDER BY created_at DESC LIMIT 1);
SET @o6 = (SELECT id FROM orders WHERE buyer_id='user-buyer-03' AND status='CANCELLED' LIMIT 1);
SET @o7 = (SELECT id FROM orders WHERE buyer_id='user-buyer-01' AND status='PREPARING' LIMIT 1);
SET @o8 = (SELECT id FROM orders WHERE buyer_id='user-buyer-02' AND status='PENDING'   LIMIT 1);

INSERT INTO shop_orders (order_id, shop_id, status, shipping_fee, shop_total_amount, user_id) VALUES
(@o1, @shop1, 'COMPLETED', 30000, 34990000, 'user-seller-01'),
(@o2, @shop1, 'DELIVERED', 30000, 22990000, 'user-seller-01'),
(@o3, @shop1, 'SHIPPED',   30000, 29990000, 'user-seller-01'),
(@o4, @shop2, 'PREPARING', 25000, 730000,   'user-seller-02'),
(@o5, @shop3, 'PENDING',   20000, 164000,   'user-seller-03'),
(@o6, @shop1, 'CANCELLED', 30000, 45990000, 'user-seller-01'),
(@o7, @shop1, 'PREPARING', 30000, 38990000, 'user-seller-01'),
(@o8, @shop3, 'PENDING',   20000, 89000,    'user-seller-03');

-- Order Items
SET @so1 = (SELECT id FROM shop_orders WHERE order_id=@o1 LIMIT 1);
SET @so2 = (SELECT id FROM shop_orders WHERE order_id=@o2 LIMIT 1);
SET @so3 = (SELECT id FROM shop_orders WHERE order_id=@o3 LIMIT 1);
SET @so4 = (SELECT id FROM shop_orders WHERE order_id=@o4 LIMIT 1);
SET @so5 = (SELECT id FROM shop_orders WHERE order_id=@o5 LIMIT 1);
SET @so6 = (SELECT id FROM shop_orders WHERE order_id=@o6 LIMIT 1);
SET @so7 = (SELECT id FROM shop_orders WHERE order_id=@o7 LIMIT 1);
SET @so8 = (SELECT id FROM shop_orders WHERE order_id=@o8 LIMIT 1);

SET @v1  = (SELECT id FROM product_variants WHERE sku='IP15PM-256-BLK' LIMIT 1);
SET @v4  = (SELECT id FROM product_variants WHERE sku='IP14-128-BLK'   LIMIT 1);
SET @v7  = (SELECT id FROM product_variants WHERE sku='S24U-256-BLK'   LIMIT 1);
SET @v15 = (SELECT id FROM product_variants WHERE sku='POLO-S-WHT'     LIMIT 1);
SET @v19 = (SELECT id FROM product_variants WHERE sku='OVER-M-WHT'     LIMIT 1);
SET @v29 = (SELECT id FROM product_variants WHERE sku='BOOK-DNT-001'   LIMIT 1);
SET @v31 = (SELECT id FROM product_variants WHERE sku='BOOK-NHK-001'   LIMIT 1);
SET @v13 = (SELECT id FROM product_variants WHERE sku='ROG-G16-4070'   LIMIT 1);
SET @v14 = (SELECT id FROM product_variants WHERE sku='MBA-M3-16-512'  LIMIT 1);

INSERT INTO order_items (shop_order_id, product_variant_id, quantity, price_at_buy, discount_amount) VALUES
(@so1, @v1,  1, 34990000, 0),
(@so2, @v4,  1, 22990000, 0),
(@so3, @v7,  1, 29990000, 0),
(@so4, @v15, 1, 450000,   0),
(@so4, @v19, 1, 280000,   0),
(@so5, @v29, 1, 89000,    0),
(@so5, @v31, 1, 75000,    0),
(@so6, @v13, 1, 45990000, 0),
(@so7, @v14, 1, 38990000, 0),
(@so8, @v29, 1, 89000,    0);

-- ============================================================
-- 7. VOUCHERS
-- ============================================================
INSERT INTO vouchers (id, code, type, value, max_discount, min_order_amt, max_usage, used_count, starts_at, expires_at, active, shop_id) VALUES
(UUID(), 'WELCOME10',  'PERCENT', 10, 50000,   0,       100, 5,  NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NULL),
(UUID(), 'SALE20',     'PERCENT', 20, 200000,  500000,  50,  12, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 1, NULL),
(UUID(), 'FREESHIP',   'FIXED',   30000, NULL, 200000,  200, 30, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1, NULL),
(UUID(), 'TECH50K',    'FIXED',   50000, NULL, 1000000, 30,  8,  NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), 1, 1),
(UUID(), 'FASHION15',  'PERCENT', 15, 100000,  300000,  80,  3,  NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY), 1, 2),
(UUID(), 'EXPIRED',    'PERCENT', 10, 50000,   0,       100, 0,  DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0, NULL),
(UUID(), 'DISABLED',   'FIXED',   100000, NULL, 500000, 20,  0,  NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 0, NULL);

-- ============================================================
-- 8. DISPUTES (khiếu nại để test)
-- ============================================================
INSERT INTO disputes (id, shop_order_id, buyer_id, reason, status, admin_note, created_at, resolved_at) VALUES
(UUID(), @so1, 'user-buyer-01', 'Sản phẩm nhận được bị trầy xước, không đúng mô tả', 'OPEN', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
(UUID(), @so2, 'user-buyer-01', 'Giao hàng chậm hơn 5 ngày so với cam kết', 'RESOLVED_SELLER_WIN', 'Đã xem xét, thời gian giao hàng nằm trong khung cho phép', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(UUID(), @so3, 'user-buyer-02', 'Nhận được hàng giả, không phải Samsung chính hãng', 'OPEN', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL);

-- ============================================================
-- 9. WITHDRAWALS (yêu cầu rút tiền)
-- ============================================================
INSERT INTO withdrawals (id, shop_id, amount, status, requested_at, resolved_at) VALUES
(UUID(), @shop1, 5000000,  'APPROVED', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
(UUID(), @shop1, 10000000, 'APPROVED', DATE_SUB(NOW(), INTERVAL 8 DAY),  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(UUID(), @shop2, 3000000,  'APPROVED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
(UUID(), @shop1, 8000000,  'PENDING',  DATE_SUB(NOW(), INTERVAL 1 DAY),  NULL),
(UUID(), @shop2, 2500000,  'PENDING',  NOW(),                            NULL),
(UUID(), @shop3, 1500000,  'PENDING',  NOW(),                            NULL),
(UUID(), @shop1, 3000000,  'REJECTED', DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 4 DAY));

-- ============================================================
-- 10. SHOP APPLICATIONS (đơn đăng ký mở shop đang chờ duyệt)
-- ============================================================
INSERT INTO shop_applications (id, user_id, shop_name, description, tax_code, tax_address, tax_full_name, status, reject_reason, submitted_at, resolved_at) VALUES
(UUID(), 'user-buyer-01', 'Buyer1 Shop',    'Shop bán đồ điện tử', '0123456789', 'Hà Nội', 'Phạm Thị Buyer',  'PENDING',  NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),
(UUID(), 'user-buyer-02', 'Hoang Shop',     'Shop thời trang',     '9876543210', 'TP.HCM', 'Hoàng Văn Mua',   'PENDING',  NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),
(UUID(), 'user-buyer-03', 'Vu Store',       'Shop sách & văn phòng','1122334455', 'Đà Nẵng','Vũ Thị Khách',   'REJECTED', 'Thông tin thuế không hợp lệ', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));

-- ============================================================
-- 11. REVIEWS (Đánh giá sản phẩm)
-- ============================================================
-- Lấy order_item IDs để gắn với reviews
SET @oi1 = (SELECT id FROM order_items WHERE shop_order_id = @so1 LIMIT 1);
SET @oi2 = (SELECT id FROM order_items WHERE shop_order_id = @so2 LIMIT 1);
SET @oi3 = (SELECT id FROM order_items WHERE shop_order_id = @so3 LIMIT 1);
SET @oi4 = (SELECT id FROM order_items WHERE shop_order_id = @so4 LIMIT 1);
SET @oi5 = (SELECT id FROM order_items WHERE shop_order_id = @so5 LIMIT 1);

INSERT INTO reviews (product_id, user_id, order_item_id, rating, comment, verified_purchase, created_at, updated_at) VALUES
-- iPhone 15 Pro Max (product 1) - review từ user-buyer-01
(1, 'user-buyer-01', @oi1, 5, 'Sản phẩm tuyệt vời! Màn hình rất sáng, camera chất lượng cực tốt. Giao hàng nhanh chóng, đóng gói cẩn thận. Rất hài lòng với mua hàng lần này.', 1, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- iPhone 14 (product 2) - review từ user-buyer-01
(2, 'user-buyer-01', @oi2, 4, 'iPhone 14 rất tốt, hiệu năng mạnh, pin dùng được cả ngày. Giá hợp lý so với hiệu năng. Chỉ hơi tiếc là không có sạc trong hộp.', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Samsung Galaxy S24 Ultra (product 3) - review từ user-buyer-02
(3, 'user-buyer-02', @oi3, 5, 'Siêu phẩm! Màn hình 6.8 inch tuyệt đẹp, camera zoom 100x thực sự ấn tượng. Bút S-Pen rất tiện lợi. Chắc chắn sẽ giới thiệu cho bạn bè.', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Áo Polo (product 7) - review từ user-buyer-02
(7, 'user-buyer-02', @oi4, 5, 'Áo polo đẹp lắm, vải cotton 100% mềm mại, form fit vừa vặn. Màu sắc bền, giặt nhiều lần vẫn đẹp như mới. Giá cửa hàng cực kỳ cạnh tranh.', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Đắc Nhân Tâm (product 11) - review từ user-buyer-03
(11, 'user-buyer-03', @oi5, 5, 'Cuốn sách kinh điển tuyệt vời! Nội dung sâu sắc, dễ hiểu, rất bổ ích. In ấn chất lượng cao, bìa cứng bền. Sẽ tặng cho bạn và gia đình.', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Thêm reviews từ các user khác cho cùng sản phẩm
INSERT INTO reviews (id, product_id, user_id, order_item_id, rating, comment, verified_purchase, created_at, updated_at) VALUES
(101, 1, 'user-buyer-02', NULL, 4, 'iPhone 15 Pro Max rất mạnh, gaming mượt mà. Thiếu cáp USB-C để tặng kèm là điều không tốt.', 0, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
(102, 1, 'user-buyer-03', NULL, 5, 'Tôi rất thích sản phẩm này. Giá hơi cao nhưng chất lượng xứng đáng. Khuyến khích mọi người mua!', 0, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(103, 2, 'user-buyer-02', NULL, 4, 'iPhone 14 tốt, nhưng Pin không thì êm như iPhone 15. Nhưng giá rẻ hơn nhiều.', 0, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
(104, 3, 'user-buyer-01', NULL, 5, 'Samsung S24 Ultra vượt trội! Camera thực sự là đỉnh cao công nghệ smartphone hiện nay.', 0, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(105, 3, 'user-buyer-03', NULL, 4, 'Điện thoại chất lượng cao, giá hợp lý. Giao hàng nhanh từ shop TechZone.', 0, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(106, 7, 'user-buyer-01', NULL, 5, 'Áo polo rất đẹp, mặc thoải mái cả ngày. Form fit chuẩn, không bị co rút sau giặt.', 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(107, 7, 'user-buyer-03', NULL, 4, 'Chất lượng tốt, giá phải chăng. Chỉ hơi hẹp một chút ở vai.', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(108, 11, 'user-buyer-01', NULL, 5, 'Đắc Nhân Tâm là cuốn sách mọi người nên đọc. Nó thay đổi cách tôi nhìn nhân tạo.', 0, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
(109, 11, 'user-buyer-02', NULL, 5, 'Sách hay, in đẹp, chứa đựng rất nhiều bài học quý báu.', 0, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ============================================================
-- RE-ENABLE FOREIGN KEY CHECKS
-- ============================================================
SET FOREIGN_KEY_CHECKS=1;

-- ============================================================
-- CẬP NHẬT AVERAGE_RATING VÀ TOTAL_REVIEWS CHO PRODUCTS
-- ============================================================
UPDATE products p
SET 
    total_reviews = (SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.id),
    average_rating = COALESCE((SELECT AVG(r.rating) FROM reviews r WHERE r.product_id = p.id), 0);

-- ============================================================
-- CẬP NHẬT SOLD_COUNT DỰA TRÊN ORDER_ITEMS (CHỈ ĐƠN DELIVERED/COMPLETED)
-- ============================================================
UPDATE products p
SET sold_count = COALESCE((
    SELECT SUM(oi.quantity)
    FROM order_items oi
    JOIN shop_orders so ON oi.shop_order_id = so.id
    WHERE oi.product_variant_id IN (
        SELECT pv.id FROM product_variants pv WHERE pv.product_id = p.id
    )
    AND so.status IN ('DELIVERED', 'COMPLETED')
), 0);

-- ============================================================
-- DONE — Kiểm tra dữ liệu
-- ============================================================
SELECT 'Categories' AS tbl, COUNT(*) AS cnt FROM categories
UNION ALL SELECT 'Users',       COUNT(*) FROM users
UNION ALL SELECT 'Shops',       COUNT(*) FROM shops
UNION ALL SELECT 'Products',    COUNT(*) FROM products
UNION ALL SELECT 'Variants',    COUNT(*) FROM product_variants
UNION ALL SELECT 'Orders',      COUNT(*) FROM orders
UNION ALL SELECT 'ShopOrders',  COUNT(*) FROM shop_orders
UNION ALL SELECT 'OrderItems',  COUNT(*) FROM order_items
UNION ALL SELECT 'Vouchers',    COUNT(*) FROM vouchers
UNION ALL SELECT 'Disputes',    COUNT(*) FROM disputes
UNION ALL SELECT 'Withdrawals', COUNT(*) FROM withdrawals
UNION ALL SELECT 'ShopApps',    COUNT(*) FROM shop_applications
UNION ALL SELECT 'Reviews',     COUNT(*) FROM reviews;
