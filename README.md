🛒 GreenShop — E-Commerce Marketplace Platform
A multi-vendor e-commerce platform similar to Shopee, where customers can shop and sellers can run their own stores within a single system.

🚀 Tech Stack
LayerTechnologyFrontendReact.jsBackendSpring Boot 3DatabaseMySQLAuthenticationSpring Security + JWTORMSpring Data JPA / Hibernate

✨ Key Features

Multi-vendor support — Multiple shops can sell on the same platform; each order is automatically split by shop
Product variants — Each product supports multiple combinations of color and size
Voucher system — Platform-wide and shop-specific discount codes with prorated distribution
Order lifecycle — Full status tracking: PENDING → PREPARING → SHIPPED → DELIVERED → COMPLETED
Commission & revenue — Platform automatically calculates commission per completed order and manages shop wallets
Role-based access control — Separate roles for Customer, Shop Owner, and Admin
Real-time chat — Direct messaging between buyers and sellers


🗂️ System Actors
ActorDescriptionGuestBrowse and search products without logging inCustomerPlace orders, manage cart, track shipments, write reviewsShop OwnerManage products, process orders, view revenue (inherits Customer)AdminManage users, categories, approve shops, handle platform settings

🗄️ Database Overview
22 tables across 6 functional groups:

Account & Auth — users, roles, permissions, user_roles, role_permissions
Product & Shop — shops, products, product_variants, categories
Order & Payment — orders, order_items, shop_orders, payments, addresses
Promotion — vouchers, voucher_usages
Revenue — shop_wallets, commissions, withdrawals
Support — conversations, messages, reviews
