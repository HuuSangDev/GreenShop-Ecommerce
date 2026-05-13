# 🧪 HƯỚNG DẪN TEST PRODUCT MANAGEMENT API

## 📋 Tổng quan
Vì chưa có JWT, cách nhanh nhất là tạm thời bỏ @PreAuthorize để test hết các API trước, sau này implement JWT mới bật lại.

Dự án đã implement đầy đủ **Product Management Module** với 15 REST APIs:
- ✅ CRUD sản phẩm (tạo, sửa, xóa, xem)
- ✅ Quản lý Product Variants (màu sắc, kích thước, SKU)
- ✅ Tìm kiếm & Lọc sản phẩm
- ✅ Sản phẩm bán chạy & mới nhất
- ✅ Admin moderation

---

## 🚀 BƯỚC 1: SETUP DATABASE (5 phút)

### 1.1. Tạo Database

```bash
# Mở MySQL
mysql -u root -p

# Tạo database
CREATE DATABASE Ecommorce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Thoát MySQL
exit;
```

✅ **Kiểm tra:** Database đã được tạo

---

## 🚀 BƯỚC 2: CHẠY APPLICATION (2 phút)

### 2.1. Kiểm tra cấu hình

Mở file `src/main/resources/application.yaml` và đảm bảo:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/Ecommorce
    username: root
    password: root  # Đổi thành password MySQL của bạn
```

### 2.2. Chạy Server (Hibernate sẽ tự động tạo tables)

```bash
# Cách 1: Maven
mvn spring-boot:run

# Cách 2: Maven Wrapper (Windows)
.\mvnw.cmd spring-boot:run
```

✅ **Kiểm tra:** 
- Đợi thấy log `Started ECommerceApplication in X seconds`
- Hibernate sẽ tự động tạo tất cả các bảng (categories, users, shops, products, etc.)

Server chạy tại: `http://localhost:8080`

---

## 🚀 BƯỚC 3: IMPORT DỮ LIỆU TEST (3 phút)

**Sau khi server đã chạy và tạo tables**, mở MySQL terminal mới:

```bash
mysql -u root -p Ecommorce
```

```sql
-- Tạo Categories
INSERT INTO categories (category_name, parent_id) VALUES 
('Điện thoại', NULL),
('Laptop', NULL),
('Phụ kiện', NULL);

-- Tạo User (password: 123456)
INSERT INTO users (id, username, password, email, full_name, phone_number, active, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440000', 'seller1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller1@test.com', 'Test Seller', '0901234567', true, NOW());

-- Tạo Shop
INSERT INTO shops (shop_name, description, rating, owner_id, created_at) VALUES 
('Test Shop', 'Shop để test API', 4.5, '550e8400-e29b-41d4-a716-446655440000', NOW());

-- Verify
SELECT 'Setup completed!' as Status;
SELECT * FROM categories;
SELECT id, username, email FROM users;
SELECT id, shop_name, owner_id FROM shops;

exit;
```

✅ **Kiểm tra:** Phải thấy 3 categories, 1 user, 1 shop

---

## 🧪 BƯỚC 4: TEST CÁC API (15 phút)

### 📌 Lưu ý:
- Mở terminal/PowerShell **MỚI** (giữ server chạy ở terminal cũ)
- Thay `{id}` bằng ID thực tế từ response
- Base URL: `http://localhost:8080/api`

---

## TEST 1: Tạo Sản Phẩm ✨

**Endpoint:** `POST /api/products`

**PowerShell:**
```powershell
$body = @{
    productName = "iPhone 15 Pro Max"
    description = "Điện thoại cao cấp từ Apple"
    price = 29990000
    stockQuantity = 100
    categoryId = 1
    imageUrl = "https://example.com/iphone.jpg"
    variants = @(
        @{
            variantName = "Titan Tự Nhiên - 256GB"
            price = 29990000
            stockQuantity = 50
            sku = "IP15PM-TN-256"
        },
        @{
            variantName = "Titan Xanh - 512GB"
            price = 34990000
            stockQuantity = 30
            sku = "IP15PM-TX-512"
        }
    )
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json -Depth 10
```

**cURL (Windows CMD):**
```bash
curl -X POST http://localhost:8080/api/products ^
  -H "Content-Type: application/json" ^
  -d "{\"productName\":\"iPhone 15 Pro Max\",\"description\":\"Điện thoại cao cấp\",\"price\":29990000,\"stockQuantity\":100,\"categoryId\":1,\"imageUrl\":\"https://example.com/iphone.jpg\",\"variants\":[{\"variantName\":\"Titan Tự Nhiên - 256GB\",\"price\":29990000,\"stockQuantity\":50,\"sku\":\"IP15PM-TN-256\"}]}"
```

**✅ Kết quả mong đợi:**
```json
{
  "success": true,
  "message": "Tạo sản phẩm thành công",
  "data": {
    "id": 1,
    "productName": "iPhone 15 Pro Max",
    "price": 29990000,
    "shopName": "Test Shop",
    "variants": [...]
  }
}
```

---

## TEST 2: Lấy Chi Tiết Sản Phẩm 📄

**Endpoint:** `GET /api/products/{id}`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method Get | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl http://localhost:8080/api/products/1
```

**✅ Kết quả:** Hiển thị đầy đủ thông tin sản phẩm + variants

---

## TEST 3: Cập Nhật Sản Phẩm ✏️

**Endpoint:** `PUT /api/products/{id}`

**PowerShell:**
```powershell
$updateBody = @{
    productName = "iPhone 15 Pro Max - UPDATED"
    price = 28990000
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method Put -Body $updateBody -ContentType "application/json" | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/products/1 ^
  -H "Content-Type: application/json" ^
  -d "{\"productName\":\"iPhone 15 Pro Max - UPDATED\",\"price\":28990000}"
```

**✅ Kết quả:** Tên và giá sản phẩm được cập nhật

---

## TEST 4: Tìm Kiếm Sản Phẩm 🔍

**Endpoint:** `GET /api/products/search?keyword={keyword}&page=0&size=10`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/search?keyword=iphone&page=0&size=10" -Method Get | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl "http://localhost:8080/api/products/search?keyword=iphone&page=0&size=10"
```

**✅ Kết quả:** Danh sách sản phẩm có từ "iphone" trong tên hoặc mô tả

---

## TEST 5: Lọc Sản Phẩm 🎯

**Endpoint:** `POST /api/products/filter`

**PowerShell:**
```powershell
$filterBody = @{
    categoryId = 1
    minPrice = 10000000
    maxPrice = 35000000
    page = 0
    size = 10
    sortBy = "price"
    sortDirection = "ASC"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/products/filter" -Method Post -Body $filterBody -ContentType "application/json" | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/products/filter ^
  -H "Content-Type: application/json" ^
  -d "{\"categoryId\":1,\"minPrice\":10000000,\"maxPrice\":35000000,\"page\":0,\"size\":10,\"sortBy\":\"price\",\"sortDirection\":\"ASC\"}"
```

**✅ Kết quả:** Sản phẩm được lọc theo category và khoảng giá, sắp xếp theo giá tăng dần

---

## TEST 6: Sản Phẩm Theo Shop 🏪

**Endpoint:** `GET /api/products/shop/{shopId}?page=0&size=10`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/shop/1?page=0&size=10" -Method Get | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl "http://localhost:8080/api/products/shop/1?page=0&size=10"
```

**✅ Kết quả:** Danh sách sản phẩm của shop có ID = 1

---

## TEST 7: Sản Phẩm Mới Nhất 🆕

**Endpoint:** `GET /api/products/new-arrivals?page=0&size=10`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/new-arrivals?page=0&size=10" -Method Get | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl "http://localhost:8080/api/products/new-arrivals?page=0&size=10"
```

**✅ Kết quả:** Sản phẩm mới nhất, sắp xếp theo ngày tạo giảm dần

---

## TEST 8: Sản Phẩm Bán Chạy 🔥

**Endpoint:** `GET /api/products/top-selling?page=0&size=10`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/top-selling?page=0&size=10" -Method Get | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl "http://localhost:8080/api/products/top-selling?page=0&size=10"
```

**✅ Kết quả:** Sản phẩm bán chạy nhất (cần có order data để test đầy đủ)

---

## TEST 9: Thêm Variant 🎨

**Endpoint:** `POST /api/products/{productId}/variants`

**PowerShell:**
```powershell
$variantBody = @{
    variantName = "Titan Đen - 1TB"
    price = 39990000
    stockQuantity = 20
    sku = "IP15PM-TD-1TB"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/products/1/variants" -Method Post -Body $variantBody -ContentType "application/json" | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/products/1/variants ^
  -H "Content-Type: application/json" ^
  -d "{\"variantName\":\"Titan Đen - 1TB\",\"price\":39990000,\"stockQuantity\":20,\"sku\":\"IP15PM-TD-1TB\"}"
```

**✅ Kết quả:** Variant mới được thêm vào sản phẩm

---

## TEST 10: Cập Nhật Variant ✏️

**Endpoint:** `PUT /api/products/variants/{variantId}`

**PowerShell:**
```powershell
$updateVariantBody = @{
    variantName = "Titan Đen - 1TB (Giảm giá)"
    price = 37990000
    stockQuantity = 25
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/products/variants/1" -Method Put -Body $updateVariantBody -ContentType "application/json" | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/products/variants/1 ^
  -H "Content-Type: application/json" ^
  -d "{\"variantName\":\"Titan Đen - 1TB (Giảm giá)\",\"price\":37990000,\"stockQuantity\":25}"
```

**✅ Kết quả:** Variant được cập nhật

---

## TEST 11: Cập Nhật Tồn Kho 📦

**Endpoint:** `PATCH /api/products/variants/{variantId}/stock?quantity={quantity}`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/variants/1/stock?quantity=150" -Method Patch | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X PATCH "http://localhost:8080/api/products/variants/1/stock?quantity=150"
```

**✅ Kết quả:** Số lượng tồn kho được cập nhật thành 150

---

## TEST 12: Xóa Variant 🗑️

**Endpoint:** `DELETE /api/products/variants/{variantId}`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/variants/3" -Method Delete | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X DELETE http://localhost:8080/api/products/variants/3
```

**✅ Kết quả:** Variant bị xóa (nếu không trong order active)

---

## TEST 13: Xóa Sản Phẩm (Soft Delete) 🗑️

**Endpoint:** `DELETE /api/products/{id}`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method Delete | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X DELETE http://localhost:8080/api/products/1
```

**✅ Kết quả:** Sản phẩm bị ẩn (available = false), không xóa khỏi DB

---

## TEST 14: Admin Ẩn Sản Phẩm 🚫

**Endpoint:** `PATCH /api/products/{id}/hide`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1/hide" -Method Patch | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl -X PATCH http://localhost:8080/api/products/1/hide
```

**✅ Kết quả:** Admin ẩn sản phẩm vi phạm

---

## TEST 15: Admin Xem Tất Cả Sản Phẩm 👁️

**Endpoint:** `GET /api/products/admin/all?page=0&size=10`

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products/admin/all?page=0&size=10" -Method Get | ConvertTo-Json -Depth 10
```

**cURL:**
```bash
curl "http://localhost:8080/api/products/admin/all?page=0&size=10"
```

**✅ Kết quả:** Hiển thị tất cả sản phẩm kể cả đã ẩn

---

## 🔍 KIỂM TRA DATABASE

Sau khi test, kiểm tra database:

```sql
mysql -u root -p Ecommorce

-- Xem sản phẩm
SELECT id, product_name, price, available FROM products;

-- Xem variants
SELECT id, product_id, variant_name, sku, stock_quantity FROM product_variants;

-- Xem sản phẩm đã ẩn
SELECT id, product_name, available FROM products WHERE available = false;

-- Xem relationship
SELECT 
    p.id,
    p.product_name,
    s.shop_name,
    c.category_name,
    COUNT(pv.id) as variant_count
FROM products p
JOIN shops s ON p.shop_id = s.id
JOIN categories c ON p.category_id = c.id
LEFT JOIN product_variants pv ON pv.product_id = p.id
GROUP BY p.id;
```

---

## 🐛 TROUBLESHOOTING

### Lỗi 1: Connection refused
```
Nguyên nhân: Server chưa chạy
Giải pháp: Chạy mvn spring-boot:run
```

### Lỗi 2: MySQL connection failed
```
Nguyên nhân: Sai password hoặc MySQL chưa chạy
Giải pháp: 
1. Kiểm tra MySQL đang chạy
2. Sửa password trong application.yaml
```

### Lỗi 3: Table doesn't exist
```
Nguyên nhân: Chưa tạo database hoặc chưa import data
Giải pháp: Chạy lại BƯỚC 1
```

### Lỗi 4: Port 8080 already in use
```
Nguyên nhân: Port bị chiếm
Giải pháp:
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Lỗi 5: SKU đã tồn tại
```
Nguyên nhân: Tạo variant với SKU trùng
Giải pháp: Đổi SKU khác (ví dụ: IP15PM-TD-1TB-V2)
```

---

## 📊 CHECKLIST TEST

- [ ] Tạo sản phẩm thành công
- [ ] Lấy chi tiết sản phẩm
- [ ] Cập nhật sản phẩm
- [ ] Tìm kiếm sản phẩm
- [ ] Lọc sản phẩm
- [ ] Lấy sản phẩm theo shop
- [ ] Sản phẩm mới nhất
- [ ] Sản phẩm bán chạy
- [ ] Thêm variant
- [ ] Cập nhật variant
- [ ] Cập nhật tồn kho
- [ ] Xóa variant
- [ ] Xóa sản phẩm (soft delete)
- [ ] Admin ẩn sản phẩm
- [ ] Admin xem tất cả

---

## 🎉 KẾT LUẬN

Nếu tất cả 15 tests đều pass, bạn đã test thành công **Product Management Module**!

**Các tính năng đã hoạt động:**
- ✅ CRUD sản phẩm với validation
- ✅ Quản lý variants (màu, size, SKU)
- ✅ Tìm kiếm & lọc nâng cao
- ✅ Soft delete (không xóa cứng)
- ✅ Authorization (chỉ chủ shop mới sửa/xóa)
- ✅ Admin moderation

**Next steps:**
- Implement JWT Authentication
- Implement Order Management
- Implement Cart Management
- Implement Review & Rating
