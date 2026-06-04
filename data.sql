SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_items;
TRUNCATE TABLE shop_orders;
TRUNCATE TABLE orders;
TRUNCATE TABLE product_variants;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE shops;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE roles;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- Thêm Roles
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Quản trị viên');
INSERT INTO roles (name, description) VALUES ('SELLER', 'Người bán');
INSERT INTO roles (name, description) VALUES ('BUYER', 'Người mua');

-- Thêm Users
INSERT INTO users (id, active, created_at, email, full_name, password, phone_number, username) VALUES 
('u1', 1, '2023-01-01 10:00:00', 'admin@gmail.com', 'Admin System', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '0900000001', 'admin'),
('u2', 1, '2023-01-02 10:00:00', 'seller1@gmail.com', 'Nguyen Van Seller', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '0900000002', 'seller1'),
('u3', 1, '2023-01-03 10:00:00', 'seller2@gmail.com', 'Tran Thi Seller', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '0900000003', 'seller2'),
('u4', 1, '2023-01-04 10:00:00', 'buyer1@gmail.com', 'Le Van Buyer', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '0900000004', 'buyer1'),
('u5', 1, '2023-01-05 10:00:00', 'buyer2@gmail.com', 'Pham Thi Buyer', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '0900000005', 'buyer2');

-- Thêm User_Roles
INSERT INTO user_roles (user_id, role_name) VALUES 
('u1', 'ADMIN'), ('u1', 'BUYER'),
('u2', 'SELLER'), ('u2', 'BUYER'),
('u3', 'SELLER'), ('u3', 'BUYER'),
('u4', 'BUYER'),
('u5', 'BUYER');

-- Thêm Shops
INSERT INTO shops (id, owner_id, shop_name, description, banner_url, logo_url, rating, status, created_at) VALUES 
(1, 'u2', 'Green Shop Official', 'Chuyên cung cấp đồ gia dụng xanh', 'https://placehold.co/1200x300', 'https://placehold.co/200x200', 4.8, 'ACTIVE', '2023-02-01 10:00:00'),
(2, 'u3', 'Tech Store VN', 'Đồ công nghệ uy tín', 'https://placehold.co/1200x300', 'https://placehold.co/200x200', 4.5, 'ACTIVE', '2023-02-02 10:00:00');

-- Thêm Categories
INSERT INTO categories (id, name, description, image_url, parent_id) VALUES 
(1, 'Điện thoại', 'Các dòng smartphone', 'https://placehold.co/100x100', NULL),
(2, 'Gia dụng', 'Đồ gia dụng thông minh', 'https://placehold.co/100x100', NULL),
(3, 'Thời trang', 'Quần áo thời trang', 'https://placehold.co/100x100', NULL);

-- Thêm Products
INSERT INTO products (id, created_at, description, image_url, price, product_name, stock_quantity, category_id, shop_id, updated_at) VALUES 
(1, '2023-03-01 10:00:00', 'Bình nước giữ nhiệt 500ml', 'https://placehold.co/400x400', 150000, 'Bình nước giữ nhiệt inox', 100, 2, 1, '2023-03-01 10:00:00'),
(2, '2023-03-02 10:00:00', 'Hộp đựng cơm văn phòng', 'https://placehold.co/400x400', 80000, 'Hộp cơm 3 tầng', 200, 2, 1, '2023-03-02 10:00:00'),
(3, '2023-03-03 10:00:00', 'iPhone 15 Pro Max', 'https://placehold.co/400x400', 30000000, 'iPhone 15 Pro Max 256GB', 50, 1, 2, '2023-03-03 10:00:00'),
(4, '2023-03-04 10:00:00', 'Samsung Galaxy S24 Ultra', 'https://placehold.co/400x400', 25000000, 'Samsung S24 Ultra', 30, 1, 2, '2023-03-04 10:00:00');

-- Thêm Product Variants
INSERT INTO product_variants (id, color, image_url, price, size, stock_quantity, product_id) VALUES 
(1, 'Đen', 'https://placehold.co/400x400', 150000, '500ml', 50, 1),
(2, 'Trắng', 'https://placehold.co/400x400', 150000, '500ml', 50, 1),
(3, 'Xanh', 'https://placehold.co/400x400', 80000, 'Free', 200, 2),
(4, 'Titan Tự nhiên', 'https://placehold.co/400x400', 30000000, '256GB', 25, 3),
(5, 'Titan Đen', 'https://placehold.co/400x400', 30000000, '256GB', 25, 3),
(6, 'Xám', 'https://placehold.co/400x400', 25000000, '256GB', 15, 4),
(7, 'Đen', 'https://placehold.co/400x400', 25000000, '256GB', 15, 4);

-- Thêm Orders
INSERT INTO orders (id, created_at, discount_amount, final_amount, payment_method, status, total_amount, buyer_id, shipping_address_id, voucher_id) VALUES 
(1, '2023-04-01 10:00:00', 0, 30180000, 'COD', 'COMPLETED', 30180000, 'u4', NULL, NULL),
(2, '2023-04-02 10:00:00', 0, 25030000, 'VNPay', 'DELIVERED', 25030000, 'u5', NULL, NULL),
(3, '2023-04-03 10:00:00', 0, 410000, 'COD', 'PENDING', 410000, 'u4', NULL, NULL);

-- Thêm Shop Orders
INSERT INTO shop_orders (id, shipping_fee, shop_total_amount, status, order_id, shop_id, user_id) VALUES 
(1, 30000, 150000, 'COMPLETED', 1, 1, 'u4'),
(2, 0, 30000000, 'COMPLETED', 1, 2, 'u4'),
(3, 30000, 25000000, 'DELIVERED', 2, 2, 'u5'),
(4, 30000, 380000, 'PENDING', 3, 1, 'u4');

-- Thêm Order Items
INSERT INTO order_items (id, price_at_buy, quantity, product_variant_id, shop_order_id, discount_amount) VALUES 
(1, 150000, 1, 1, 1, 0),
(2, 30000000, 1, 4, 2, 0),
(3, 25000000, 1, 6, 3, 0),
(4, 150000, 2, 2, 4, 0),
(5, 80000, 1, 3, 4, 0);

-- Update Wallet / Commissions for Completed Orders (ShopOrder 1 and 2)
INSERT INTO admin_wallets (id, balance, total_earned, total_withdrawn) VALUES ('aw1', 603000, 603000, 0);
INSERT INTO shop_wallets (id, balance, total_earned, total_withdrawn, shop_id) VALUES 
('sw1', 147000, 147000, 0, 1),
('sw2', 29400000, 29400000, 0, 2);

INSERT INTO commissions (id, commission_amt, commission_rate, gross_amount, net_amount, shop_id, shop_order_id) VALUES 
('c1', 3000, 0.02, 150000, 147000, 1, 1),
('c2', 600000, 0.02, 30000000, 29400000, 2, 2);

