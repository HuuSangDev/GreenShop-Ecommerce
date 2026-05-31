# Stitch AI UI Prompts — GreenShop E-Commerce

Dự án: **GreenShop** — nền tảng thương mại điện tử đa shop (multi-seller marketplace)  
Stack backend: Spring Boot, JWT, MySQL  
Roles: ADMIN / SELLER / BUYER  
Màu chủ đạo: Xanh lá (green) — tên thương hiệu "GreenShop"

---

## 1. Trang Đăng nhập (Login Page)

```
Design a clean login page for "GreenShop", a green-themed e-commerce marketplace.

Layout: centered card on a light green gradient background (#f0fdf4 to #dcfce7).

Card content (white, rounded-2xl, shadow-lg, max-w-md):
- Top: GreenShop logo (leaf icon + bold green text "GreenShop")
- Heading: "Đăng nhập" (Vietnamese)
- Email input field with label "Email" and placeholder "example@email.com"
- Password input field with label "Mật khẩu", show/hide toggle icon on the right
- "Quên mật khẩu?" link aligned right, green color
- Primary button "Đăng nhập" — full width, solid green (#16a34a), white text, rounded-lg
- Divider "hoặc"
- Secondary button "Đăng ký tài khoản" — full width, outlined green border, green text
- Error state: red border on input + small red error message below

Color palette: primary green #16a34a, light green bg #f0fdf4, text gray-800.
Typography: Inter or system font, clean and modern.
No distracting elements. Mobile-responsive.
```

---

## 2. Trang Đăng ký (Register Page)

```
Design a registration page for "GreenShop" e-commerce platform.

Layout: centered card, same green gradient background as login page.

Card content (white, rounded-2xl, shadow-lg, max-w-lg):
- GreenShop logo at top
- Heading: "Tạo tài khoản mới"
- Form fields (stacked, each with label above):
  1. "Họ và tên" — text input, placeholder "Nguyễn Văn A"
  2. "Tên đăng nhập" — text input, placeholder "username"
  3. "Email" — email input
  4. "Số điện thoại" — tel input, placeholder "09xxxxxxxx"
  5. "Mật khẩu" — password input with strength indicator bar below (weak/medium/strong in red/yellow/green)
  6. "Xác nhận mật khẩu" — password input
- Checkbox: "Tôi đồng ý với Điều khoản dịch vụ" with green link
- Primary button "Đăng ký" — full width, solid green
- Link at bottom: "Đã có tài khoản? Đăng nhập"

Validation states: green border + checkmark icon when valid, red border + error text when invalid.
Mobile-responsive, clean and trustworthy feel.
```

---

## 3. Trang Chủ (Homepage)

```
Design a homepage for "GreenShop", a Vietnamese multi-seller e-commerce marketplace with a green theme.

Header (sticky, white bg, shadow-sm):
- Left: GreenShop logo (leaf + text)
- Center: search bar (rounded-full, gray bg, placeholder "Tìm kiếm sản phẩm...", green search icon button)
- Right: cart icon with badge (item count), user avatar dropdown, "Đăng nhập" button if not logged in

Hero section: full-width banner carousel with 3 slides, green gradient overlays, promotional text and CTA button "Mua ngay"

Category navigation bar (horizontal scroll on mobile):
- Icon + label chips for: Điện thoại, Laptop, Tablet, Phụ kiện, Đồng hồ thông minh
- Active category highlighted in green

Section "Sản phẩm nổi bật" (Top Selling):
- Section title with green left border accent
- Horizontal scroll row of product cards (4 visible on desktop, 2 on mobile)

Section "Hàng mới về" (New Arrivals):
- Same layout as above

Product card design:
- White card, rounded-xl, shadow-sm, hover shadow-md transition
- Product image (aspect-ratio 1:1, object-cover)
- Shop name (small gray text)
- Product name (2 lines max, font-medium)
- Star rating (yellow stars + count)
- Price in bold green, original price strikethrough if discounted
- "Thêm vào giỏ" button (green, small, bottom of card)

Footer: dark green bg, 4 columns (About, Categories, Support, Social links)
```

---

## 4. Trang Danh sách Sản phẩm / Tìm kiếm (Product Listing Page)

```
Design a product listing / search results page for "GreenShop" e-commerce.

Two-column layout (desktop): left sidebar filters (w-64) + right product grid.

Left sidebar "Bộ lọc":
- Section "Danh mục" — tree structure with expand/collapse, parent categories (Điện thoại, Laptop...) with child subcategories (iPhone, Samsung, Xiaomi...)
- Section "Khoảng giá" — dual-handle range slider, min/max price inputs below
- Section "Đánh giá" — star rating checkboxes (5 sao, 4 sao trở lên...)
- Section "Tình trạng" — checkboxes: Còn hàng, Hết hàng
- "Áp dụng bộ lọc" green button, "Xóa bộ lọc" text link

Right content:
- Top bar: result count text "Tìm thấy 24 sản phẩm", sort dropdown (Phổ biến nhất / Mới nhất / Giá tăng dần / Giá giảm dần), grid/list view toggle
- Product grid: 3 columns desktop, 2 tablet, 1 mobile
- Each product card: image, shop name, product name, variants count badge, price, rating, "Thêm vào giỏ" button
- Pagination at bottom (numbered, green active state)

Mobile: filter in a slide-in drawer triggered by "Bộ lọc" button.
```

---

## 5. Trang Chi tiết Sản phẩm (Product Detail Page)

```
Design a product detail page for "GreenShop" e-commerce marketplace.

Layout: 2-column on desktop (image left, info right), stacked on mobile.

Left column — Image gallery:
- Large main image (square, rounded-xl)
- Thumbnail row below (4-5 small images, active has green border)

Right column — Product info:
- Shop name with shop avatar (small, clickable link)
- Product name (text-2xl, font-bold)
- Star rating row: yellow stars, "(128 đánh giá)", "Đã bán 500+"
- Price section: large bold green price, original price strikethrough, discount badge (e.g. "-15%")
- Variant selector "Phiên bản":
  - Chip buttons for each variant (e.g. "Titan Tự Nhiên - 256GB", "Titan Xanh - 256GB", "Titan Đen - 512GB")
  - Selected variant has green bg + white text, others have gray border
  - Show selected variant price update dynamically
- Stock indicator: green dot "Còn 50 sản phẩm" or red "Hết hàng"
- Quantity selector: minus button, number input, plus button
- Action buttons row:
  - "Thêm vào giỏ hàng" — outlined green, full width
  - "Mua ngay" — solid green, full width
- Divider
- Product description section (expandable, "Xem thêm" link)

Below the fold:
- Tab bar: "Mô tả" | "Thông số" | "Đánh giá (128)"
- Reviews section: overall rating (large number + stars), rating breakdown bars (5★ to 1★), list of review cards (avatar, name, stars, date, comment text)

Sticky "Thêm vào giỏ" bar on mobile bottom.
```

---

## 6. Trang Giỏ hàng (Cart Page)

```
Design a shopping cart page for "GreenShop" e-commerce.

Layout: 2-column desktop (cart items left 65%, order summary right 35%), stacked mobile.

Left — Cart items list:
- Page title "Giỏ hàng (3 sản phẩm)"
- "Chọn tất cả" checkbox at top
- Each cart item row (white card, rounded-xl, shadow-sm):
  - Checkbox on left
  - Product image (80x80, rounded-lg)
  - Product info: product name (bold), variant name (gray small text e.g. "Titan Xanh - 256GB"), shop name
  - Price: current price (green bold), if price changed show warning "Giá đã thay đổi" with old/new price
  - Quantity stepper: minus / number / plus buttons
  - Stock warning badge if insufficient stock (orange "Chỉ còn X sản phẩm")
  - Delete icon (trash, red hover) on far right
  - Out-of-stock item: grayed out with "Hết hàng" badge overlay
- "Xóa tất cả" text button at bottom of list

Right — Order summary card (sticky on desktop):
- Title "Tóm tắt đơn hàng"
- Line items: "Tạm tính (3 sản phẩm)" + amount
- Shipping: "Miễn phí vận chuyển" in green
- Voucher input: text field + "Áp dụng" button
- Divider
- "Tổng cộng" in large bold + green amount
- "Tiến hành thanh toán" — large solid green button, full width
- Warning banner if cart has issues (price changed / out of stock) in yellow

Empty cart state: centered illustration, "Giỏ hàng trống" text, "Tiếp tục mua sắm" green button.
```

---

## 7. Trang Thanh toán / Checkout (Checkout Page)

```
Design a checkout page for "GreenShop" e-commerce platform.

Multi-step progress bar at top: Step 1 "Địa chỉ" → Step 2 "Thanh toán" → Step 3 "Xác nhận"
Active step in green, completed steps with green checkmark.

Layout: 2-column (form left 60%, order summary right 40%).

Step 1 — Shipping address form:
- Section "Địa chỉ giao hàng"
- Saved addresses list (radio cards): each shows full name, phone, address, "Mặc định" badge
- "Thêm địa chỉ mới" button (outlined green)
- Add address form (collapsible): Họ tên, Số điện thoại, Tỉnh/Thành, Quận/Huyện, Phường/Xã, Địa chỉ cụ thể, checkbox "Đặt làm mặc định"

Step 2 — Payment method:
- Radio cards: "Thanh toán khi nhận hàng (COD)" with cash icon, "Chuyển khoản ngân hàng" with bank icon, "Ví điện tử" with wallet icon
- Selected card has green border + green radio dot

Right — Order summary (same as cart summary but read-only):
- Items list (image + name + variant + qty + price)
- Subtotal, shipping, discount, total
- "Đặt hàng" solid green button

Step 3 — Confirmation:
- Success illustration (green checkmark circle)
- "Đặt hàng thành công!" heading
- Order code: #ORD-2024-001
- Estimated delivery date
- "Xem đơn hàng" and "Tiếp tục mua sắm" buttons
```

---

## 8. Trang Lịch sử Đơn hàng — Buyer (Order History Page)

```
Design an order history page for buyers on "GreenShop" e-commerce.

Layout: full-width with max-w-4xl centered.

Tab filter bar: "Tất cả" | "Chờ xác nhận" | "Đang xử lý" | "Đang giao" | "Đã giao" | "Đã hủy"
Active tab underlined in green.

Each order card (white, rounded-xl, shadow-sm, mb-4):
- Header row: Order code "#ORD-2024-001", date "10/05/2024", status badge (color-coded: yellow=Pending, blue=Processing, orange=Shipping, green=Delivered, red=Cancelled)
- Shop name with shop icon
- Items preview: up to 2 items shown (image + name + variant + qty), "+2 sản phẩm khác" if more
- Footer row: "Tổng tiền: 29.990.000đ" (bold green), action buttons on right:
  - "Xem chi tiết" (outlined)
  - "Mua lại" (green, if delivered)
  - "Đánh giá" (green, if delivered and not reviewed)
  - "Hủy đơn" (red outlined, if pending)

Empty state: illustration + "Bạn chưa có đơn hàng nào" + "Mua sắm ngay" button.
```

---

## 9. Trang Chi tiết Đơn hàng — Buyer (Order Detail Page)

```
Design an order detail page for "GreenShop" e-commerce.

Page title: "Chi tiết đơn hàng #ORD-2024-001"

Order status timeline (horizontal stepper):
- Steps: Đặt hàng → Xác nhận → Đang giao → Đã giao
- Completed steps: green filled circle + green line
- Current step: green pulsing circle
- Future steps: gray

Section "Địa chỉ giao hàng" (card):
- Name, phone, full address

Section "Sản phẩm" (card):
- Shop name header with shop rating
- Each item row: image (60x60) + product name + variant + qty + price
- Subtotal for this shop

Section "Thông tin thanh toán" (card):
- Tạm tính, Phí vận chuyển, Giảm giá voucher, Tổng cộng (bold green)
- Phương thức: COD / Bank Transfer

Action buttons:
- "Hủy đơn hàng" (red outlined) — only if PENDING
- "Đánh giá sản phẩm" (green) — only if DELIVERED
- "Liên hệ shop" (gray outlined)
```

---

## 10. Trang Hồ sơ Người dùng (User Profile Page)

```
Design a user profile page for "GreenShop" e-commerce.

Layout: sidebar left (w-64) + main content right.

Left sidebar (white card, rounded-xl):
- User avatar (circle, 80px) with edit overlay on hover
- User full name (bold)
- Email (gray small)
- Navigation menu items (with icons):
  - "Thông tin cá nhân" (active = green bg + green text)
  - "Đơn hàng của tôi"
  - "Địa chỉ giao hàng"
  - "Đổi mật khẩu"
  - "Đăng xuất" (red text)

Main content — "Thông tin cá nhân":
- Section title
- Form fields (2-column grid on desktop):
  - Họ và tên, Tên đăng nhập, Email (disabled), Số điện thoại
- "Cập nhật" green button
- Avatar upload section: current avatar + "Thay đổi ảnh" button

Address management tab:
- List of saved addresses (cards with edit/delete icons)
- "Thêm địa chỉ mới" button
- Default address badge in green

Mobile: sidebar becomes bottom tab bar.
```

---

## 11. Dashboard Seller — Tổng quan (Seller Dashboard)

```
Design a seller dashboard for "GreenShop" marketplace. Clean, data-focused, green accent theme.

Layout: left sidebar navigation (w-56, dark green #14532d) + main content area.

Sidebar nav items (white icons + text):
- Dashboard (active)
- Sản phẩm
- Đơn hàng
- Doanh thu
- Cửa hàng
- Cài đặt

Main content — Dashboard overview:
- Page title "Xin chào, Apple Store VN 👋"
- Stats cards row (4 cards, green gradient variants):
  1. "Doanh thu hôm nay" — 5.200.000đ, +12% vs yesterday (green up arrow)
  2. "Đơn hàng mới" — 8 orders, badge
  3. "Sản phẩm đang bán" — 24 products
  4. "Đánh giá trung bình" — ⭐ 4.8/5

- Revenue chart: line chart (last 7 days), green line, light green fill below
- Recent orders table: Order ID | Customer | Product | Amount | Status | Action
- Top selling products: ranked list with product image, name, sold count, revenue

Color: sidebar dark green, cards white with green accent borders/icons, charts green.
```

---

## 12. Trang Quản lý Sản phẩm — Seller (Seller Product Management)

```
Design a product management page for sellers on "GreenShop" marketplace.

Header: "Quản lý sản phẩm" title + "Thêm sản phẩm mới" green button (top right)

Filter/search bar:
- Search input "Tìm theo tên sản phẩm..."
- Status filter dropdown: Tất cả / Đang bán / Đã ẩn
- Category filter dropdown

Products table (white card, rounded-xl):
Columns: Checkbox | Hình ảnh | Tên sản phẩm | Danh mục | Giá | Tồn kho | Trạng thái | Hành động

Each row:
- Product thumbnail (40x40, rounded)
- Product name + variant count badge (e.g. "3 phiên bản")
- Category name
- Price range (if variants have different prices: "24.990.000đ - 34.990.000đ")
- Stock quantity (red if < 10)
- Status toggle switch (green=active, gray=hidden)
- Action icons: Edit (pencil), Delete (trash red)

Bulk actions bar (appears when rows selected): "Ẩn đã chọn" | "Xóa đã chọn"
Pagination at bottom.
```

---

## 13. Form Thêm / Sửa Sản phẩm — Seller (Product Create/Edit Form)

```
Design a product creation/editing form for sellers on "GreenShop" marketplace.

Page title: "Thêm sản phẩm mới" with breadcrumb "Dashboard > Sản phẩm > Thêm mới"

Layout: single column, max-w-3xl centered, sections as white cards.

Section 1 "Thông tin cơ bản":
- "Tên sản phẩm" — text input (required)
- "Danh mục" — cascading select (parent → child, e.g. Điện thoại → iPhone)
- "Mô tả sản phẩm" — rich text editor (bold/italic/list toolbar)

Section 2 "Hình ảnh sản phẩm":
- Drag-and-drop upload zone (dashed border, upload icon, "Kéo thả hoặc click để tải ảnh")
- Image preview grid (3 columns), each with delete X button
- Primary image indicator (green star badge)

Section 3 "Giá & Tồn kho" (base product):
- "Giá cơ bản" — number input with "đ" suffix
- "Số lượng tồn kho" — number input

Section 4 "Phiên bản sản phẩm (Variants)":
- Toggle switch "Sản phẩm có nhiều phiên bản"
- When enabled: table with columns Tên phiên bản | SKU | Giá | Tồn kho | Xóa
- "Thêm phiên bản" button (outlined green, + icon)
- Each variant row is editable inline

Action buttons (sticky bottom bar):
- "Lưu nháp" (gray outlined)
- "Đăng bán" (solid green)
```

---

## 14. Trang Quản lý Đơn hàng — Seller (Seller Order Management)

```
Design an order management page for sellers on "GreenShop" marketplace.

Header: "Quản lý đơn hàng"

Tab bar: Tất cả | Chờ xác nhận (badge:3) | Đang xử lý | Đang giao | Đã giao | Đã hủy

Filter row: date range picker, search by order ID or customer name

Orders table:
Columns: Mã đơn | Khách hàng | Sản phẩm | Tổng tiền | Thanh toán | Trạng thái | Ngày đặt | Hành động

Each row:
- Order ID (monospace, clickable link)
- Customer name + avatar
- Product count "3 sản phẩm" with first product image
- Total amount (green bold)
- Payment method badge: COD (orange) / Bank Transfer (blue)
- Status badge (color-coded)
- Date
- Action: "Xem chi tiết" button, status update dropdown (Xác nhận → Đang giao → Đã giao)

Order detail modal/drawer (slide from right):
- Full order info, items list, shipping address, timeline
- "Cập nhật trạng thái" button
```

---

## 15. Dashboard Admin — Tổng quan (Admin Dashboard)

```
Design an admin dashboard for "GreenShop" marketplace platform. Professional, data-rich, dark green sidebar.

Sidebar (dark green #14532d, w-60):
- GreenShop logo (white)
- Nav sections:
  - Tổng quan: Dashboard
  - Quản lý: Người dùng, Cửa hàng, Sản phẩm, Đơn hàng, Danh mục
  - Tài chính: Doanh thu, Hoa hồng, Rút tiền
  - Hệ thống: Cài đặt, Phân quyền

Main content:
- Welcome bar: "Admin Dashboard" + current date

KPI cards (2 rows × 4 cards):
Row 1: Tổng doanh thu (revenue), Đơn hàng hôm nay, Người dùng mới, Sản phẩm mới
Row 2: Tổng shop, Đơn chờ duyệt, Hoa hồng thu được, Tỷ lệ hoàn đơn

Charts section (2 columns):
- Left: Revenue line chart (30 days), green
- Right: Order status donut chart (Pending/Processing/Delivered/Cancelled)

Tables section (2 columns):
- Left: "Đơn hàng gần đây" — 5 rows with status badges
- Right: "Shop mới đăng ký" — 5 rows with approve/reject buttons

All cards white, rounded-xl, shadow-sm. Green accent throughout.
```

---

## 16. Trang Quản lý Người dùng — Admin (Admin User Management)

```
Design a user management page for admins on "GreenShop" marketplace.

Header: "Quản lý người dùng" + "Thêm người dùng" button

Filter bar:
- Search input (by name, email, username)
- Role filter: Tất cả / ADMIN / SELLER / BUYER
- Status filter: Tất cả / Đang hoạt động / Bị khóa

Users table:
Columns: Avatar + Tên | Username | Email | Số điện thoại | Vai trò | Trạng thái | Ngày tạo | Hành động

Each row:
- Avatar circle (40px) + full name
- Username (gray)
- Email
- Phone
- Role badge: ADMIN (purple), SELLER (blue), BUYER (green)
- Status toggle (active=green, locked=red)
- Created date
- Actions: "Xem" icon, "Phân quyền" icon, "Khóa/Mở" toggle

Special action: "Duyệt thành Seller" button for BUYER accounts (green, with shop icon) — calls /users/acceptRole/{userId}

Pagination + "Hiển thị X / tổng Y người dùng"
```

---

## 17. Trang Quản lý Danh mục — Admin (Admin Category Management)

```
Design a category management page for admins on "GreenShop" marketplace.

Layout: 2-column (category tree left, edit form right).

Left panel "Cây danh mục" (white card):
- Tree view with expand/collapse arrows
- Parent categories (bold): Điện thoại, Laptop, Tablet, Phụ kiện, Đồng hồ thông minh
- Child categories indented with dash: — iPhone, — Samsung, — Xiaomi
- Each item: category name + product count badge + edit icon + delete icon
- "Thêm danh mục gốc" green button at bottom

Right panel "Thêm / Sửa danh mục" (white card):
- Form title: "Thêm danh mục mới" or "Sửa: iPhone"
- "Tên danh mục" — text input (required)
- "Danh mục cha" — select dropdown (None = root category, or select parent)
- "Mô tả" — textarea (optional)
- "Icon/Hình ảnh" — image upload
- Action buttons: "Lưu" (green) + "Hủy" (gray)

Delete confirmation modal: "Xóa danh mục này sẽ ảnh hưởng đến X sản phẩm. Bạn có chắc không?"
```

---

## 18. Trang Cửa hàng (Shop Profile Page — Public)

```
Design a public shop profile page for "GreenShop" marketplace.

Shop header banner (full-width, green gradient or custom banner image):
- Shop logo/avatar (circle, 80px, white border)
- Shop name (text-2xl bold white)
- Rating: ⭐ 4.8 (128 đánh giá)
- Stats row: "245 Sản phẩm" | "1.2k Đã bán" | "Tham gia 2 năm"
- "Theo dõi" button (outlined white) + "Chat" button (white bg)

Below header — 2 column layout:
Left sidebar (w-56):
- Search within shop input
- Category filter (checkboxes for this shop's categories)
- Price range slider

Right — Products grid:
- Sort bar: "Phổ biến" | "Mới nhất" | "Giá tăng" | "Giá giảm"
- Product grid (3 columns desktop, 2 mobile)
- Standard product cards with shop name hidden (already in shop context)

Shop info tab (below products):
- "Giới thiệu" tab: shop description text
- "Đánh giá shop" tab: overall rating + review list
```

---

## Ghi chú chung cho tất cả màn hình

- **Brand color**: Primary green `#16a34a` (Tailwind `green-600`), light bg `#f0fdf4` (green-50)
- **Font**: Inter hoặc system-ui, clean sans-serif
- **Border radius**: rounded-xl cho cards, rounded-lg cho buttons/inputs
- **Shadow**: shadow-sm mặc định, shadow-md khi hover
- **Language**: Vietnamese (tiếng Việt) cho tất cả labels và placeholder
- **Responsive**: Mobile-first, breakpoints sm/md/lg
- **Currency format**: `29.990.000đ` (dấu chấm phân cách nghìn, đ suffix)
- **Date format**: `dd/MM/yyyy`
