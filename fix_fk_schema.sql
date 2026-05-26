-- ============================================================
-- FIX: order_items.shop_order_id FK trỏ sai vào bảng `shops`
-- thay vì `shop_orders`.
-- Nguyên nhân: ShopOrder entity từng bị set @Table(name="shops")
-- ============================================================

USE Ecommerce;

-- BƯỚC 1: Xem tên constraint hiện tại (để drop đúng tên)
SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'Ecommerce'
  AND TABLE_NAME = 'order_items'
  AND REFERENCED_TABLE_NAME = 'shops';

-- BƯỚC 2: Drop FK cũ (tên lấy từ kết quả trên — thường là FK6ic2e2ehfkh3hlno1ochlkqv8)
ALTER TABLE order_items
    DROP FOREIGN KEY FK6ic2e2ehfkh3hlno1ochlkqv8;

-- BƯỚC 3: Đảm bảo bảng shop_orders tồn tại và có đúng cấu trúc
-- (Nếu chưa có — Hibernate sẽ tạo khi restart app với ddl-auto=update)
-- Kiểm tra:
SHOW TABLES LIKE 'shop_orders';

-- BƯỚC 4: Thêm lại FK đúng — trỏ vào shop_orders
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_shop_order
        FOREIGN KEY (shop_order_id) REFERENCES shop_orders (id);

-- BƯỚC 5: Kiểm tra lại kết quả
SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'Ecommerce'
  AND TABLE_NAME = 'order_items';
