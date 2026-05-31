# Prompt: Tạo cấu trúc thư mục FE cho GreenShop E-Commerce

## Prompt cho AI (Cursor / Copilot / ChatGPT)

---

Create a production-ready React + JavaScript + Tailwind CSS frontend project structure for **GreenShop**, a multi-seller e-commerce marketplace (similar to Shopee/Lazada).

**Tech stack:**
- React 18 (Vite)
- JavaScript (no TypeScript)
- Tailwind CSS v3
- React Router DOM v6
- Axios (HTTP client + interceptors for JWT)
- Zustand (lightweight global state)
- React Query / TanStack Query v5 (server state, caching)
- React Hook Form + Yup (form validation)
- React Hot Toast (notifications)

**Roles in the system:** ADMIN, SELLER, BUYER (from JWT scope claim)

---

Generate the following folder structure with brief comments explaining each folder's purpose:

```
FE/
├── public/
│   └── favicon.ico
│
├── src/
│   ├── main.jsx                  # Entry point
│   ├── App.jsx                   # Root component, router setup
│   │
│   ├── assets/                   # Static assets
│   │   ├── images/
│   │   └── icons/
│   │
│   ├── components/               # Reusable UI components (dumb/presentational)
│   │   ├── common/               # Shared across all roles
│   │   │   ├── Button.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── Modal.jsx
│   │   │   ├── Spinner.jsx
│   │   │   ├── Badge.jsx
│   │   │   ├── Pagination.jsx
│   │   │   ├── StarRating.jsx
│   │   │   ├── EmptyState.jsx
│   │   │   └── ConfirmDialog.jsx
│   │   │
│   │   ├── layout/               # Layout shells
│   │   │   ├── Header.jsx        # Public header (logo, search, cart icon, auth)
│   │   │   ├── Footer.jsx
│   │   │   ├── Sidebar.jsx       # Seller/Admin sidebar nav
│   │   │   ├── PublicLayout.jsx  # Header + Footer wrapper
│   │   │   ├── SellerLayout.jsx  # Sidebar + topbar for seller
│   │   │   └── AdminLayout.jsx   # Sidebar + topbar for admin
│   │   │
│   │   ├── product/              # Product-related components
│   │   │   ├── ProductCard.jsx
│   │   │   ├── ProductGrid.jsx
│   │   │   ├── ProductImageGallery.jsx
│   │   │   ├── VariantSelector.jsx
│   │   │   ├── QuantityStepper.jsx
│   │   │   └── ReviewCard.jsx
│   │   │
│   │   ├── cart/
│   │   │   ├── CartItem.jsx
│   │   │   └── CartSummary.jsx
│   │   │
│   │   ├── order/
│   │   │   ├── OrderCard.jsx
│   │   │   ├── OrderStatusBadge.jsx
│   │   │   └── OrderTimeline.jsx
│   │   │
│   │   └── category/
│   │       ├── CategoryTree.jsx
│   │       └── CategoryChip.jsx
│   │
│   ├── pages/                    # Route-level page components (smart/container)
│   │   ├── auth/
│   │   │   ├── LoginPage.jsx
│   │   │   └── RegisterPage.jsx
│   │   │
│   │   ├── buyer/                # Pages for BUYER role
│   │   │   ├── HomePage.jsx
│   │   │   ├── ProductListPage.jsx
│   │   │   ├── ProductDetailPage.jsx
│   │   │   ├── CartPage.jsx
│   │   │   ├── CheckoutPage.jsx
│   │   │   ├── OrderHistoryPage.jsx
│   │   │   ├── OrderDetailPage.jsx
│   │   │   ├── ProfilePage.jsx
│   │   │   └── ShopProfilePage.jsx
│   │   │
│   │   ├── seller/               # Pages for SELLER role
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── ProductManagePage.jsx
│   │   │   ├── ProductFormPage.jsx
│   │   │   └── OrderManagePage.jsx
│   │   │
│   │   ├── admin/                # Pages for ADMIN role
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── UserManagePage.jsx
│   │   │   ├── CategoryManagePage.jsx
│   │   │   ├── ProductManagePage.jsx
│   │   │   └── ShopManagePage.jsx
│   │   │
│   │   └── error/
│   │       ├── NotFoundPage.jsx  # 404
│   │       └── UnauthorizedPage.jsx # 403
│   │
│   ├── routes/                   # Routing configuration
│   │   ├── index.jsx             # Main router (createBrowserRouter)
│   │   ├── PublicRoute.jsx       # Redirect to home if already logged in
│   │   ├── PrivateRoute.jsx      # Redirect to login if not authenticated
│   │   └── RoleRoute.jsx         # Redirect to 403 if wrong role
│   │
│   ├── store/                    # Zustand global state
│   │   ├── authStore.js          # user, accessToken, isAuthenticated, login(), logout()
│   │   └── cartStore.js          # cartCount (for header badge, synced from server)
│   │
│   ├── services/                 # API call functions (axios)
│   │   ├── axiosInstance.js      # Base axios with baseURL, JWT interceptor, refresh logic
│   │   ├── authService.js        # login, logout, refreshToken
│   │   ├── userService.js        # register, getUsers, acceptRole
│   │   ├── productService.js     # getProduct, searchProducts, filterProducts, CRUD
│   │   ├── categoryService.js    # getCategoryTree, CRUD
│   │   ├── cartService.js        # getCart, addToCart, updateItem, removeItem, validate
│   │   └── orderService.js       # getOrders, getOrderDetail, createOrder
│   │
│   ├── hooks/                    # Custom React hooks
│   │   ├── useAuth.js            # Read from authStore, expose user/role helpers
│   │   ├── useCart.js            # React Query wrapper for cart API
│   │   ├── useProducts.js        # React Query wrapper for product APIs
│   │   ├── useCategories.js      # React Query wrapper for category tree
│   │   └── useDebounce.js        # Debounce hook for search input
│   │
│   ├── utils/                    # Pure utility functions
│   │   ├── formatCurrency.js     # 29990000 → "29.990.000đ"
│   │   ├── formatDate.js         # ISO → "dd/MM/yyyy"
│   │   ├── getRoleFromToken.js   # Decode JWT scope → ["ROLE_BUYER"]
│   │   └── constants.js          # ORDER_STATUS, PAYMENT_METHOD, API_BASE_URL, etc.
│   │
│   └── styles/
│       └── index.css             # Tailwind directives (@tailwind base/components/utilities)
│
├── .env                          # VITE_API_BASE_URL=http://localhost:8080
├── .env.example
├── .gitignore
├── index.html
├── vite.config.js                # Vite config with @ alias → src/
├── tailwind.config.js            # content paths, extend green palette
└── package.json
```

---

**Additional requirements:**

1. **`vite.config.js`** — configure path alias `@` pointing to `src/`

2. **`tailwind.config.js`** — extend theme with GreenShop brand colors:
   ```js
   colors: {
     primary: { DEFAULT: '#16a34a', light: '#f0fdf4', dark: '#14532d' }
   }
   ```

3. **`axiosInstance.js`** — implement:
   - `baseURL` from `import.meta.env.VITE_API_BASE_URL`
   - Request interceptor: attach `Authorization: Bearer <accessToken>` from Zustand authStore
   - Response interceptor: on 401, call `/auth/refresh` with refreshToken, retry original request once; on second 401, call logout() and redirect to `/login`

4. **`authStore.js`** (Zustand) — persist to `localStorage`:
   ```js
   { user, accessToken, refreshToken, isAuthenticated, login(), logout() }
   ```

5. **`RoleRoute.jsx`** — accept `allowedRoles` prop array, read role from JWT scope, render `<Outlet/>` or redirect to `/unauthorized`

6. **`routes/index.jsx`** — structure:
   - Public routes (no auth): `/`, `/products`, `/products/:id`, `/shop/:shopId`, `/login`, `/register`
   - Buyer private routes (BUYER): `/cart`, `/checkout`, `/orders`, `/orders/:id`, `/profile`
   - Seller private routes (SELLER): `/seller/dashboard`, `/seller/products`, `/seller/products/new`, `/seller/products/:id/edit`, `/seller/orders`
   - Admin private routes (ADMIN): `/admin/dashboard`, `/admin/users`, `/admin/categories`, `/admin/products`, `/admin/shops`

7. **`constants.js`** — define:
   ```js
   export const ORDER_STATUS = { PENDING, PROCESSING, SHIPPING, DELIVERED, CANCELLED }
   export const PAYMENT_METHOD = { COD, BANK_TRANSFER }
   export const ROLES = { ADMIN, SELLER, BUYER }
   ```

Generate all files listed above with minimal but functional skeleton code (imports, exports, placeholder JSX/logic). Do not leave any file empty.
