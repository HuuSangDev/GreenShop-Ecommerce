# 🧪 Hướng Dẫn Test Thủ Công OrderService bằng Postman

> **Base URL**: `http://localhost:8080/ecommerce`  
> **Content-Type**: `application/json`  
> **Auth**: Bearer Token (JWT) — lấy từ bước đăng nhập

---

## 📋 TỔNG QUAN LUỒNG TEST

```
[1] Đăng ký Admin + Buyer + Seller
[2] Tạo Category (Admin)
[3] Đăng ký Shop → Approve Shop (Admin)
[4] Seller tạo Product + Variant
[5] Buyer thêm sản phẩm vào Cart
[6] === TEST ORDERSERVICE ===
    [6.1] Preview Checkout
    [6.2] Checkout COD
    [6.3] Checkout SEPAY + giả lập Webhook
```

---

## 🔐 PHASE 1 — SETUP TÀI KHOẢN

### 1.1 Đăng ký tài khoản Admin (hoặc dùng tài khoản có sẵn)

> ⚠️ **Lưu ý**: Tài khoản Admin phải được tạo thủ công trong DB hoặc qua script. Nếu đã có tài khoản Admin, bỏ qua bước này.

**Request — Đăng ký User thường:**
```
POST http://localhost:8080/ecommerce/users/register
Content-Type: application/json
```
```json
{
  "email": "admin@test.com",
  "password": "Admin@123",
  "fullName": "Admin GreenShop",
  "phoneNumber": "0900000001"
}
```

> Sau đó vào DB thêm role ADMIN cho user này:  
> `INSERT INTO user_roles (user_id, role_id) VALUES ('<userId>', <roleId_ADMIN>);`

---

### 1.2 Đăng ký tài khoản Buyer (người mua)

```
POST http://localhost:8080/ecommerce/users/register
Content-Type: application/json
```
```json
{
  "email": "buyer@test.com",
  "password": "Buyer@123",
  "fullName": "Nguyễn Văn Mua",
  "phoneNumber": "0911111111"
}
```

**✅ Lưu lại**: `userId` của buyer từ response.

---

### 1.3 Đăng ký tài khoản Seller (người bán)

```
POST http://localhost:8080/ecommerce/users/register
Content-Type: application/json
```
```json
{
  "email": "seller@test.com",
  "password": "Seller@123",
  "fullName": "Trần Thị Bán",
  "phoneNumber": "0922222222"
}
```

**✅ Lưu lại**: `userId` của seller từ response.

---

## 🔑 PHASE 2 — ĐĂNG NHẬP & LẤY TOKEN

### 2.1 Đăng nhập Admin

```
POST http://localhost:8080/ecommerce/auth/login
Content-Type: application/json
```
```json
{
  "email": "admin@test.com",
  "password": "Admin@123"
}
```

**Response mẫu:**
```json
{
  "code": 200,
  "message": "create user success",
  "result": {
    "token": "eyJhbGciOiJSUzI1NiJ9...",
    "refreshToken": "eyJhbGci..."
  }
}
```

**✅ Lưu lại**: `ADMIN_TOKEN = result.token`

---

### 2.2 Đăng nhập Buyer

```
POST http://localhost:8080/ecommerce/auth/login
Content-Type: application/json
```
```json
{
  "email": "buyer@test.com",
  "password": "Buyer@123"
}
```

**✅ Lưu lại**: `BUYER_TOKEN = result.token`

---

### 2.3 Đăng nhập Seller

```
POST http://localhost:8080/ecommerce/auth/login
Content-Type: application/json
```
```json
{
  "email": "seller@test.com",
  "password": "Seller@123"
}
```

**✅ Lưu lại**: `SELLER_TOKEN = result.token`

---

## 🗂️ PHASE 3 — TẠO CATEGORY (Admin)

```
POST http://localhost:8080/ecommerce/api/v1/categories
Authorization: Bearer {{ADMIN_TOKEN}}
Content-Type: application/json
```
```json
{
  "name": "Điện tử",
  "description": "Các sản phẩm điện tử",
  "parentId": null,
  "sortOrder": 1
}
```

**Response mẫu:**
```json
{
  "code": 201,
  "message": "Category created successfully",
  "result": {
    "id": 1,
    "name": "Điện tử"
  }
}
```

**✅ Lưu lại**: `CATEGORY_ID = 1`

---

## 🏪 PHASE 4 — TẠO SHOP (Seller)

### 4.1 Seller nộp đơn đăng ký Shop

```
POST http://localhost:8080/ecommerce/api/v1/shops/applications
Authorization: Bearer {{SELLER_TOKEN}}
Content-Type: application/json
```
```json
{
  "shopName": "Shop Tech Việt",
  "description": "Chuyên bán đồ điện tử chính hãng",
  "taxCode": "0123456789",
  "taxAddress": "123 Nguyễn Văn Linh, TP.HCM",
  "taxFullName": "Trần Thị Bán"
}
```

**Response mẫu:**
```json
{
  "code": 201,
  "message": "Đơn đăng ký gian hàng đã được nộp thành công, vui lòng chờ duyệt",
  "result": {
    "id": "abc-123-def",
    "shopName": "Shop Tech Việt",
    "status": "PENDING"
  }
}
```

**✅ Lưu lại**: `APPLICATION_ID = "abc-123-def"`

---

### 4.2 Admin duyệt đơn Shop

```
POST http://localhost:8080/ecommerce/api/v1/shops/applications/{{APPLICATION_ID}}/approve
Authorization: Bearer {{ADMIN_TOKEN}}
```
*(Không cần body)*

**Response mẫu:**
```json
{
  "message": "Đã duyệt đơn và tạo gian hàng thành công",
  "result": {
    "id": "abc-123-def",
    "shopName": "Shop Tech Việt",
    "status": "APPROVED"
  }
}
```

> Sau khi approve, Seller sẽ tự động được gán role `SELLER`.  
> **Cần re-login Seller** để lấy token mới có role SELLER:

```
POST http://localhost:8080/ecommerce/auth/login
```
```json
{
  "email": "seller@test.com",
  "password": "Seller@123"
}
```

**✅ Cập nhật lại**: `SELLER_TOKEN = result.token` (token mới có role SELLER)

---

### 4.3 Lấy thông tin Shop của Seller

```
GET http://localhost:8080/ecommerce/api/v1/shops/me
Authorization: Bearer {{SELLER_TOKEN}}
```

**Response mẫu:**
```json
{
  "result": {
    "id": 1,
    "shopName": "Shop Tech Việt"
  }
}
```

**✅ Lưu lại**: `SHOP_ID = 1`

---

## 📦 PHASE 5 — TẠO PRODUCT & VARIANT (Seller)

### 5.1 Tạo sản phẩm có sẵn Variant

> ⚠️ **API này dùng `form-data`, KHÔNG phải `application/json`**  
> Vì có `@ModelAttribute` để nhận cả text field lẫn file ảnh trong cùng 1 request.

```
POST http://localhost:8080/ecommerce/api/products
Authorization: Bearer {{SELLER_TOKEN}}
Body: form-data  ← (chọn "form-data" trong Postman, KHÔNG phải "raw JSON")
```

**Các field gửi trong form-data:**

| Key | Type | Value |
|-----|------|-------|
| `productName` | Text | `Tai nghe Sony WH-1000XM5` |
| `description` | Text | `Tai nghe không dây chống ồn cao cấp` |
| `price` | Text | `7990000` |
| `stockQuantity` | Text | `100` |
| `categoryId` | Text | `1` |
| `image` | File | *(chọn file ảnh .jpg/.png — optional, có thể bỏ trống)* |
| `variants[0].variantName` | Text | `Màu Đen` |
| `variants[0].price` | Text | `7990000` |
| `variants[0].stockQuantity` | Text | `50` |
| `variants[0].sku` | Text | `SONY-WH1000XM5-BLACK` |
| `variants[1].variantName` | Text | `Màu Bạc` |
| `variants[1].price` | Text | `7990000` |
| `variants[1].stockQuantity` | Text | `50` |
| `variants[1].sku` | Text | `SONY-WH1000XM5-SILVER` |

> 💡 **Lưu ý**: Variants gửi theo kiểu **indexed field** (`variants[0].xxx`, `variants[1].xxx`).  
> Spring Boot tự map vào `List<ProductVariantRequest>` khi dùng `@ModelAttribute`.



**Response mẫu:**
```json
{
  "result": {
    "id": 1,
    "productName": "Tai nghe Sony WH-1000XM5",
    "variants": [
      { "id": 1, "variantName": "Màu Đen", "stockQuantity": 50 },
      { "id": 2, "variantName": "Màu Bạc", "stockQuantity": 50 }
    ]
  }
}
```

**✅ Lưu lại**:
- `PRODUCT_ID = 1`
- `VARIANT_ID_BLACK = 1`
- `VARIANT_ID_SILVER = 2`

---

### 5.2 (Tuỳ chọn) Tạo thêm sản phẩm thứ 2 để test Multi-Vendor

> Tạo thêm 1 Seller khác và 1 sản phẩm để test trường hợp Multi-Shop trong 1 đơn hàng.  
> Bỏ qua nếu chỉ cần test basic.

---

## 🛒 PHASE 6 — THÊM VÀO GIỎ HÀNG (Buyer)

### 6.1 Xem giỏ hàng hiện tại

```
GET http://localhost:8080/ecommerce/api/v1/cart
Authorization: Bearer {{BUYER_TOKEN}}
```

---

### 6.2 Thêm sản phẩm vào giỏ (Variant Đen, qty=2)

```
POST http://localhost:8080/ecommerce/api/v1/cart/items
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "productId": 1,
  "variantId": 1,
  "quantity": 2
}
```

**Response mẫu:**
```json
{
  "message": "Item added to cart",
  "result": {
    "items": [
      {
        "cartItemId": "cart-item-uuid-001",
        "productName": "Tai nghe Sony WH-1000XM5",
        "variantName": "Màu Đen",
        "quantity": 2
      }
    ]
  }
}
```

**✅ Lưu lại**: `CART_ITEM_ID_1 = "cart-item-uuid-001"`

---

### 6.3 Thêm thêm 1 sản phẩm nữa (Variant Bạc, qty=1)

```
POST http://localhost:8080/ecommerce/api/v1/cart/items
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "productId": 1,
  "variantId": 2,
  "quantity": 1
}
```

**✅ Lưu lại**: `CART_ITEM_ID_2 = "cart-item-uuid-002"`

---

### 6.4 Validate giỏ hàng trước khi checkout

```
GET http://localhost:8080/ecommerce/api/v1/cart/validate
Authorization: Bearer {{BUYER_TOKEN}}
```

> Kiểm tra stock và giá trước khi tiến hành checkout.

---

---

## ✅ PHASE 7 — TEST ORDERSERVICE (Mục tiêu chính)

> **⚠️ Đây là phần quan trọng nhất!**  
> Dùng `BUYER_TOKEN` và `cartItemIds` đã lưu từ Phase 6.

---

## 🔍 TEST CASE 1 — CHECKOUT PREVIEW

**Mục đích**: Đọc thông tin đơn hàng TRƯỚC KHI đặt (read-only, không tạo gì).

```
POST http://localhost:8080/ecommerce/api/v1/checkouts/preview
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "cartItemIds": ["cart-item-uuid-001", "cart-item-uuid-002"]
}
```

**Expected Response (200 OK):**
```json
{
  "code": 200,
  "message": "Thông tin đơn hàng",
  "result": {
    "subtotal": 23970000,
    "shippingFee": 0,
    "discountAmount": 0,
    "finalAmount": 23970000,
    "totalItems": 3,
    "totalDistinctItems": 2,
    "availablePaymentMethods": ["COD", "SEPAY"],
    "items": [
      {
        "cartItemId": "cart-item-uuid-001",
        "variantId": 1,
        "variantName": "Màu Đen",
        "productName": "Tai nghe Sony WH-1000XM5",
        "quantity": 2,
        "price": 7990000,
        "subtotal": 15980000,
        "availableStock": 50
      },
      {
        "cartItemId": "cart-item-uuid-002",
        "variantId": 2,
        "variantName": "Màu Bạc",
        "productName": "Tai nghe Sony WH-1000XM5",
        "quantity": 1,
        "price": 7990000,
        "subtotal": 7990000,
        "availableStock": 50
      }
    ]
  }
}
```

**Điều cần kiểm tra:**
- [ ] `subtotal = 2×7,990,000 + 1×7,990,000 = 23,970,000`
- [ ] `totalItems = 3` (2 + 1)
- [ ] `totalDistinctItems = 2`
- [ ] `availablePaymentMethods` chứa cả `COD` và `SEPAY`
- [ ] Không có gì thay đổi trong DB (cart vẫn còn)

---

## 🏦 TEST CASE 2 — CHECKOUT COD

**Mục đích**: Đặt hàng thanh toán khi nhận hàng — stock bị trừ ngay, cart bị xóa.

```
POST http://localhost:8080/ecommerce/api/v1/orders
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "cartItemIds": ["cart-item-uuid-001", "cart-item-uuid-002"],
  "paymentMethod": "COD"
}
```

**Expected Response (200 OK):**
```json
{
  "code": 200,
  "message": "Đặt hàng thành công",
  "result": {
    "orderId": 1,
    "status": "PENDING",
    "paymentMethod": "COD",
    "totalAmount": 23970000,
    "discountAmount": 0,
    "finalAmount": 23970000,
    "totalShops": 1,
    "totalItems": 3,
    "paymentUrl": null,
    "shopOrders": [
      {
        "shopOrderId": 1,
        "shopId": 1,
        "shopName": "Shop Tech Việt",
        "status": "PENDING",
        "shopTotalAmount": 23970000,
        "items": [
          {
            "orderItemId": 1,
            "variantId": 1,
            "variantName": "Màu Đen",
            "sku": "SONY-WH1000XM5-BLACK",
            "productName": "Tai nghe Sony WH-1000XM5",
            "quantity": 2,
            "priceAtBuy": 7990000,
            "subtotal": 15980000
          },
          {
            "orderItemId": 2,
            "variantId": 2,
            "variantName": "Màu Bạc",
            "sku": "SONY-WH1000XM5-SILVER",
            "productName": "Tai nghe Sony WH-1000XM5",
            "quantity": 1,
            "priceAtBuy": 7990000,
            "subtotal": 7990000
          }
        ]
      }
    ]
  }
}
```

**Điều cần kiểm tra:**
- [ ] `status = "PENDING"` (COD → PENDING)
- [ ] `paymentUrl = null` (COD không cần QR)
- [ ] Stock đã bị trừ: Variant Đen: 50→48, Variant Bạc: 50→49
- [ ] Giỏ hàng bị xóa (gọi GET /api/v1/cart → items rỗng)
- [ ] Không có bản ghi Payment nào được tạo

**Kiểm tra stock sau COD:**
```
GET http://localhost:8080/ecommerce/api/products/1
```
> Xem `variants[].stockQuantity` đã giảm chưa.

**Kiểm tra cart đã clear:**
```
GET http://localhost:8080/ecommerce/api/v1/cart
Authorization: Bearer {{BUYER_TOKEN}}
```
> Kết quả phải là cart rỗng.

---

> ⚠️ **Trước khi test SEPAY**: Cần thêm lại sản phẩm vào giỏ hàng (vì cart đã bị xóa sau COD).  
> Lặp lại **Phase 6.2 và 6.3** để thêm lại sản phẩm và lưu `cartItemIds` mới.

---

## 💳 TEST CASE 3 — CHECKOUT SEPAY

**Mục đích**: Đặt hàng thanh toán online — tạo QR, stock CHƯA bị trừ, cart CHƯA bị xóa.

> Giả sử sau khi thêm lại cart: `CART_ITEM_ID_3`, `CART_ITEM_ID_4`

```
POST http://localhost:8080/ecommerce/api/v1/orders
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "cartItemIds": ["cart-item-uuid-003", "cart-item-uuid-004"],
  "paymentMethod": "SEPAY"
}
```

**Expected Response (200 OK):**
```json
{
  "code": 200,
  "message": "Đơn hàng đã tạo — vui lòng quét QR để thanh toán",
  "result": {
    "orderId": 2,
    "status": "PENDING_PAYMENT",
    "paymentMethod": "SEPAY",
    "totalAmount": 23970000,
    "finalAmount": 23970000,
    "paymentUrl": "https://qr.sepay.vn/img?...",
    "shopOrders": [ ... ]
  }
}
```

**Điều cần kiểm tra:**
- [ ] `status = "PENDING_PAYMENT"` (SEPAY → chờ thanh toán)
- [ ] `paymentUrl` khác `null` (có URL QR)
- [ ] Stock CHƯA thay đổi (Variant Đen vẫn = 48, Variant Bạc vẫn = 49)
- [ ] Cart CHƯA bị xóa (gọi GET /api/v1/cart vẫn thấy items)
- [ ] Trong DB có bản ghi `Payment` với `status = PENDING`, `transactionRef = "ORDER_2"`

**✅ Lưu lại**: `ORDER_ID_SEPAY = 2`

---

## 🔔 TEST CASE 4 — GIẢ LẬP SEPAY WEBHOOK (Thanh toán thành công)

**Mục đích**: Mô phỏng SePay gọi webhook về khi user đã chuyển khoản thành công.  
**⚠️ Endpoint này là PUBLIC — KHÔNG cần JWT.**

```
POST http://localhost:8080/ecommerce/api/v1/payments/sepay/webhook
Content-Type: application/json
```
```json
{
  "id": 999001,
  "gateway": "VietinBank",
  "transactionDate": "2026-05-25 10:30:00",
  "accountNumber": "113366668888",
  "content": "ORDER_2",
  "transferAmount": 23970000,
  "referenceCode": "SEPAY-TXN-20260525-001",
  "description": "Chuyen khoan thanh toan ORDER_2"
}
```

> **Quan trọng**: `"content"` phải = `"ORDER_{{ORDER_ID_SEPAY}}"` để hệ thống match được.  
> `"transferAmount"` phải ≥ số tiền của order.

**Expected Response (200 OK, body rỗng)**

**Điều cần kiểm tra SAU webhook:**
- [ ] `Order.status = "PAID"`
- [ ] `Payment.status = "SUCCESS"`, `transactionId = "SEPAY-TXN-20260525-001"`
- [ ] Stock đã bị trừ: Variant Đen 48→46, Variant Bạc 49→48
- [ ] Cart đã bị xóa (GET /api/v1/cart → rỗng)

```
GET http://localhost:8080/ecommerce/api/products/1
```
> Kiểm tra stock đã giảm.

```
GET http://localhost:8080/ecommerce/api/v1/cart
Authorization: Bearer {{BUYER_TOKEN}}
```
> Kiểm tra cart đã rỗng.

---

## 🧨 TEST CASE 5 — SEPAY WEBHOOK IDEMPOTENCY

**Mục đích**: Gọi webhook lần 2 (SePay retry) → hệ thống phải xử lý an toàn, không trừ stock 2 lần.

```
POST http://localhost:8080/ecommerce/api/v1/payments/sepay/webhook
Content-Type: application/json
```
```json
{
  "id": 999001,
  "gateway": "VietinBank",
  "transactionDate": "2026-05-25 10:30:00",
  "accountNumber": "113366668888",
  "content": "ORDER_2",
  "transferAmount": 23970000,
  "referenceCode": "SEPAY-TXN-20260525-001",
  "description": "Chuyen khoan thanh toan ORDER_2 (retry)"
}
```

**Expected**: HTTP 200 OK (không lỗi).  
**Điều cần kiểm tra**: Stock KHÔNG bị trừ thêm lần nữa (idempotency).

---

## ❌ TEST CASE 6 — CÁC TRƯỜNG HỢP LỖI

### 6.1 Checkout với cartItemIds rỗng

```
POST http://localhost:8080/ecommerce/api/v1/orders
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "cartItemIds": [],
  "paymentMethod": "COD"
}
```
**Expected**: `400 Bad Request` — "Danh sách sản phẩm không được trống"

---

### 6.2 Checkout với cartItemId không tồn tại

```
POST http://localhost:8080/ecommerce/api/v1/orders
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "cartItemIds": ["fake-cart-item-id-9999"],
  "paymentMethod": "COD"
}
```
**Expected**: `4xx Error` — CART_ITEM_NOT_FOUND

---

### 6.3 Checkout với stock không đủ

> Cập nhật stock về 0:
```
PATCH http://localhost:8080/ecommerce/api/products/variants/1/stock?quantity=0
Authorization: Bearer {{SELLER_TOKEN}}
```
> Sau đó thêm sản phẩm vào cart và checkout:

```
POST http://localhost:8080/ecommerce/api/v1/orders
Authorization: Bearer {{BUYER_TOKEN}}
Content-Type: application/json
```
```json
{
  "cartItemIds": ["new-cart-item-id"],
  "paymentMethod": "COD"
}
```
**Expected**: `4xx Error` — OutOfStockException

---

### 6.4 Webhook với amount nhỏ hơn order amount

```
POST http://localhost:8080/ecommerce/api/v1/payments/sepay/webhook
Content-Type: application/json
```
```json
{
  "id": 999002,
  "gateway": "VietinBank",
  "transactionDate": "2026-05-25 11:00:00",
  "accountNumber": "113366668888",
  "content": "ORDER_2",
  "transferAmount": 1000,
  "referenceCode": "SEPAY-TXN-WRONG",
  "description": "So tien sai"
}
```
**Expected**: HTTP 200 OK (không throw, nhưng Payment.status → FAILED)

---

### 6.5 Checkout không có JWT

```
POST http://localhost:8080/ecommerce/api/v1/orders
Content-Type: application/json
```
```json
{
  "cartItemIds": ["cart-item-uuid-001"],
  "paymentMethod": "COD"
}
```
**Expected**: `401 Unauthorized`

---

## 📊 TỔNG HỢP ENDPOINTS ORDERSERVICE

| # | Method | URL | Auth | Mô tả |
|---|--------|-----|------|-------|
| 1 | POST | `/api/v1/checkouts/preview` | JWT (Buyer) | Xem trước đơn hàng |
| 2 | POST | `/api/v1/orders` | JWT (Buyer) | Đặt hàng (COD / SEPAY) |
| 3 | POST | `/api/v1/payments/sepay/webhook` | Public | Callback từ SePay |

---

## 🗄️ KIỂM TRA DATABASE (Tham khảo)

Sau mỗi bước, bạn có thể query DB để xác nhận:

```sql
-- Xem danh sách Orders
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10;

-- Xem ShopOrders theo orderId
SELECT * FROM shop_orders WHERE order_id = 2;

-- Xem OrderItems theo orderId
SELECT oi.* FROM order_items oi
JOIN shop_orders so ON oi.shop_order_id = so.id
WHERE so.order_id = 2;

-- Xem Payment của order
SELECT * FROM payments WHERE order_id = 2;

-- Kiểm tra stock hiện tại
SELECT id, variant_name, stock_quantity FROM product_variants WHERE product_id = 1;

-- Xem CartItems còn lại của buyer
SELECT ci.* FROM cart_items ci
JOIN carts c ON ci.cart_id = c.id
JOIN users u ON c.user_id = u.id
WHERE u.email = 'buyer@test.com';
```

---

## ⚙️ POSTMAN COLLECTION VARIABLES (Khuyến nghị)

Tạo các biến môi trường trong Postman:

| Variable | Value |
|----------|-------|
| `BASE_URL` | `http://localhost:8080/ecommerce` |
| `ADMIN_TOKEN` | *(paste từ login response)* |
| `BUYER_TOKEN` | *(paste từ login response)* |
| `SELLER_TOKEN` | *(paste từ login response)* |
| `CATEGORY_ID` | `1` |
| `PRODUCT_ID` | `1` |
| `VARIANT_ID_BLACK` | `1` |
| `VARIANT_ID_SILVER` | `2` |
| `CART_ITEM_ID_1` | *(paste từ add-to-cart response)* |
| `CART_ITEM_ID_2` | *(paste từ add-to-cart response)* |
| `ORDER_ID_SEPAY` | *(paste từ SEPAY checkout response)* |

---

*Generated for GreenShop E-Commerce — OrderService Testing Guide*  
*Base URL: `http://localhost:8080/ecommerce` | Port: 8080*
