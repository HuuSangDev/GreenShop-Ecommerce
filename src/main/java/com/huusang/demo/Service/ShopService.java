package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.*;
import com.huusang.demo.Dto.Response.*;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Enum.*;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopService {

    UserRepository userRepository;
    ShopRepository shopRepository;
    ShopWalletRepository shopWalletRepository;
    ShopApplicationRepository shopApplicationRepository;
    WithdrawalRepository withdrawalRepository;
    RoleRepository roleRepository;
    OrderItemRepository orderItemRepository;
    PaymentRepository paymentRepository;
    ShopOrderRepository shopOrderRepository;
    CommissionRepository commissionRepository;
    OrderRepository orderRepository;

    /** Tỷ lệ commission sàn thu (2%) */
    static final BigDecimal COMMISSION_RATE = new BigDecimal("2.00");
    static final BigDecimal HUNDRED = new BigDecimal("100");

    // ─────────────────────────────────────────────────────────────────────────
    //  SHOP APPLICATION (Đăng ký mở gian hàng)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * ADMIN: Lấy danh sách đơn đăng ký, filter theo status.
     */
    public List<ShopApplicationResponse> getApplications(ShopApplicationStatus status) {
        List<ShopApplication> applications = (status != null)
                ? shopApplicationRepository.findByStatus(status)
                : shopApplicationRepository.findAll();
        return applications.stream().map(this::toApplicationResponse).collect(Collectors.toList());
    }

    /**
     * CUSTOMER: Nộp đơn đăng ký mở gian hàng.
     * - Ngăn nộp 2 đơn PENDING cùng lúc.
     * - Ngăn user đã có shop nộp đơn lại.
     */
    @Transactional
    public ShopApplicationResponse applyForShop(String userEmail, ShopApplicationRequest request) {
        User user = getUserByEmail(userEmail);

        // Kiểm tra user đã có shop chưa
        if (shopRepository.existsByOwnerId(user.getId())) {
            throw new AppException(ErrorCode.SHOP_ALREADY_EXISTS);
        }

        // Kiểm tra đã có đơn PENDING chưa
        shopApplicationRepository.findByUserIdAndStatus(user.getId(), ShopApplicationStatus.PENDING)
                .ifPresent(app -> { throw new AppException(ErrorCode.SHOP_APPLICATION_ALREADY_PENDING); });

        ShopApplication application = ShopApplication.builder()
                .user(user)
                .shopName(request.getShopName())
                .description(request.getDescription())
                .taxCode(request.getTaxCode())
                .taxAddress(request.getTaxAddress())
                .taxFullName(request.getTaxFullName())
                .status(ShopApplicationStatus.PENDING)
                .build();

        shopApplicationRepository.save(application);
        log.info("User {} đã nộp đơn đăng ký shop: {}", userEmail, application.getShopName());
        return toApplicationResponse(application);
    }

    /**
     * ADMIN: Duyệt đơn đăng ký → tạo Shop + ShopWallet, gán role SELLER cho user.
     */
    @Transactional
    public ShopApplicationResponse approveShop(String applicationId) {
        ShopApplication application = shopApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_APPLICATION_NOT_FOUND));

        if (application.getStatus() != ShopApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.SHOP_APPLICATION_NOT_PENDING);
        }

        User user = application.getUser();

        // 1. Tạo Shop
        Shop shop = Shop.builder()
                .owner(user)
                .shopName(application.getShopName())
                .description(application.getDescription())
                .status(ShopStatus.ACTIVE)
                .rating(0.0)
                .build();
        shopRepository.save(shop);

        // 2. Tạo ShopWallet cho Shop vừa tạo
        ShopWallet wallet = ShopWallet.builder()
                .shop(shop)
                .balance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .totalWithdrawn(BigDecimal.ZERO)
                .build();
        shopWalletRepository.save(wallet);

        // 3. Gán role SELLER cho user
        Role sellerRole = roleRepository.findById(RoleName.SELLER.name())
                .orElseThrow(() -> new RuntimeException("SELLER role not found in DB"));
        Set<Role> roles = user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>();
        roles.add(sellerRole);
        user.setRoles(roles);
        userRepository.save(user);

        // 4. Cập nhật trạng thái đơn
        application.setStatus(ShopApplicationStatus.APPROVED);
        application.setResolvedAt(LocalDateTime.now());
        shopApplicationRepository.save(application);

        log.info("Admin đã duyệt đơn {} → tạo Shop #{} cho user {}", applicationId, shop.getId(), user.getEmail());
        return toApplicationResponse(application);
    }

    /**
     * ADMIN: Từ chối đơn đăng ký kèm lý do.
     */
    @Transactional
    public ShopApplicationResponse rejectShop(String applicationId, RejectShopRequest request) {
        ShopApplication application = shopApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_APPLICATION_NOT_FOUND));

        if (application.getStatus() != ShopApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.SHOP_APPLICATION_NOT_PENDING);
        }

        application.setStatus(ShopApplicationStatus.REJECTED);
        application.setRejectReason(request.getReason());
        application.setResolvedAt(LocalDateTime.now());
        shopApplicationRepository.save(application);

        log.info("Admin đã từ chối đơn {} với lý do: {}", applicationId, request.getReason());
        return toApplicationResponse(application);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SHOP MANAGEMENT (Quản lý gian hàng)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * SELLER: Lấy thông tin gian hàng của chính mình.
     */
    @Transactional(readOnly = true)
    public ShopResponse getMyShop(String userEmail) {
        User user = getUserByEmail(userEmail);
        Shop shop = shopRepository.findByOwnerIdWithOwner(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
        return toShopResponse(shop);
    }

    /**
     * SELLER: Cập nhật tên, mô tả, banner, logo gian hàng.
     */
    @Transactional
    public ShopResponse updateShop(String userEmail, UpdateShopRequest request) {
        User user = getUserByEmail(userEmail);
        Shop shop = shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        if (request.getShopName() != null && !request.getShopName().isBlank()) {
            shop.setShopName(request.getShopName());
        }
        if (request.getDescription() != null) {
            shop.setDescription(request.getDescription());
        }
        if (request.getBannerUrl() != null) {
            shop.setBannerUrl(request.getBannerUrl());
        }
        if (request.getLogoUrl() != null) {
            shop.setLogoUrl(request.getLogoUrl());
        }

        shopRepository.save(shop);
        return toShopResponse(shop);
    }

    /**
     * PUBLIC (Customer): Lấy thông tin public của một shop theo ID.
     */
    public ShopResponse getShopById(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
        return toShopResponse(shop);
    }

    /**
     * ADMIN: Lấy tất cả shops, có thể filter theo status.
     * status = null → lấy tất cả.
     */
    public List<ShopResponse> getAllShops(ShopStatus status) {
        List<Shop> shops = (status != null)
                ? shopRepository.findByStatus(status)
                : shopRepository.findAll();
        return shops.stream().map(this::toShopResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  REVENUE & WALLET (Ví & Doanh thu)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * SELLER: Xem số dư ví, tổng doanh thu, tổng đã rút.
     */
    public ShopWalletResponse getWallet(String userEmail) {
        Shop shop = getShopByOwnerEmail(userEmail);
        ShopWallet wallet = shopWalletRepository.findByShopId(shop.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_WALLET_NOT_FOUND));
        return toWalletResponse(wallet);
    }

    /**
     * SELLER: Thống kê doanh thu theo ngày/tuần/tháng.
     * period: "DAILY" | "WEEKLY" | "MONTHLY"
     */
    public RevenueStatsResponse getRevenueStats(String userEmail, String period) {
        Shop shop = getShopByOwnerEmail(userEmail);

        // Lấy tất cả OrderItems thuộc shop này (đã hoàn thành)
        List<OrderItem> items = orderItemRepository.findDeliveredItemsByShopId(shop.getId());

        // Tổng doanh thu & tổng đơn
        BigDecimal totalRevenue = items.stream()
                .map(item -> item.getPriceAtBuy().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Nhóm doanh thu theo khoảng thời gian
        Map<String, BigDecimal> revenueByPeriod = groupRevenueByPeriod(items, period);

        // Top sản phẩm bán chạy
        List<RevenueStatsResponse.TopProductResponse> topProducts = buildTopProducts(items);

        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(BigDecimal.valueOf(items.size()))
                .revenueByPeriod(revenueByPeriod)
                .topProducts(topProducts)
                .build();
    }

    /**
     * SELLER: Rút tiền từ ví (tự động, không cần duyệt).
     * Trừ balance ngay và cập nhật totalWithdrawn.
     */
    @Transactional
    public WithdrawalResponse requestWithdrawal(String userEmail, WithdrawalRequest request) {
        Shop shop = getShopByOwnerEmail(userEmail);
        ShopWallet wallet = shopWalletRepository.findByShopId(shop.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_WALLET_NOT_FOUND));

        // Kiểm tra số dư
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ErrorCode.SHOP_INSUFFICIENT_BALANCE);
        }

        // Trừ tiền khỏi balance và cập nhật totalWithdrawn ngay lập tức
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(request.getAmount()));
        shopWalletRepository.save(wallet);

        // Tạo Withdrawal record với trạng thái APPROVED (đã rút thành công)
        Withdrawal withdrawal = Withdrawal.builder()
                .shop(shop)
                .amount(request.getAmount())
                .status(WithdrawalStatus.APPROVED)
                .requestedAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
        withdrawalRepository.save(withdrawal);

        log.info("Shop {} đã rút {} VND thành công (tự động)", shop.getShopName(), request.getAmount());
        return toWithdrawalResponse(withdrawal);
    }

    /**
     * SELLER: Lịch sử các lần rút tiền và trạng thái.
     */
    public List<WithdrawalResponse> getWithdrawalHistory(String userEmail) {
        Shop shop = getShopByOwnerEmail(userEmail);
        return withdrawalRepository.findByShopIdOrderByRequestedAtDesc(shop.getId())
                .stream().map(this::toWithdrawalResponse).collect(Collectors.toList());
    }

    /**
     * SELLER: Lấy danh sách các khoản thanh toán đơn hàng (doanh thu) liên quan đến shop.
     */
    public List<PaymentResponse> getPayments(String userEmail) {
        Shop shop = getShopByOwnerEmail(userEmail);
        List<Payment> payments = paymentRepository.findByShopId(shop.getId());
        
        return payments.stream().map(p -> {
            BigDecimal shopAmount = BigDecimal.ZERO;
            if (p.getOrder() != null && p.getOrder().getShopOrders() != null) {
                shopAmount = p.getOrder().getShopOrders().stream()
                        .filter(so -> so.getShop().getId().equals(shop.getId()))
                        .map(so -> so.getShopTotalAmount().add(so.getShippingFee() != null ? so.getShippingFee() : BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            
            return PaymentResponse.builder()
                    .id(p.getId())
                    .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                    .transactionId(p.getTransactionId())
                    .transactionRef(p.getTransactionRef())
                    .method(p.getMethod() != null ? p.getMethod().name() : null)
                    .status(p.getStatus() != null ? p.getStatus().name() : null)
                    .amount(p.getAmount())
                    .shopAmount(shopAmount)
                    .createdAt(p.getCreatedAt())
                    .paidAt(p.getPaidAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * [DEPRECATED] Method này không còn được sử dụng vì rút tiền tự động.
     * Giữ lại để tương thích API cũ nếu cần.
     */
    @Deprecated
    @Transactional
    public WithdrawalResponse processWithdrawal(String withdrawalId, ProcessWithdrawalRequest request) {
        // Tất cả withdrawal đều tự động APPROVED rồi, method này không còn ý nghĩa
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_WITHDRAWAL_NOT_FOUND));
        
        log.warn("processWithdrawal called but withdrawals are now auto-approved. withdrawalId={}", withdrawalId);
        return toWithdrawalResponse(withdrawal);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Shop getShopByOwnerEmail(String email) {
        User user = getUserByEmail(email);
        return shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }

    private Map<String, BigDecimal> groupRevenueByPeriod(List<OrderItem> items, String period) {
        Map<String, BigDecimal> result = new TreeMap<>();
        DateTimeFormatter formatter;
        switch (period.toUpperCase()) {
            case "WEEKLY"  -> formatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
            case "MONTHLY" -> formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            default        -> formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }
        for (OrderItem item : items) {
            if (item.getShopOrder() != null && item.getShopOrder().getOrder() != null
                    && item.getShopOrder().getOrder().getCreatedAt() != null) {
                String key = item.getShopOrder().getOrder().getCreatedAt().format(formatter);
                BigDecimal lineTotal = item.getPriceAtBuy().multiply(BigDecimal.valueOf(item.getQuantity()));
                result.merge(key, lineTotal, BigDecimal::add);
            }
        }
        return result;
    }

    private List<RevenueStatsResponse.TopProductResponse> buildTopProducts(List<OrderItem> items) {
        // Nhóm theo productId, tính tổng số lượng và doanh thu
        Map<Long, long[]> aggregated = new HashMap<>();
        Map<Long, String> names = new HashMap<>();
        Map<Long, BigDecimal> revenues = new HashMap<>();

        for (OrderItem item : items) {
            if (item.getProductVariant() == null || item.getProductVariant().getProduct() == null) continue;
            Long productId = item.getProductVariant().getProduct().getId();
            String productName = item.getProductVariant().getProduct().getProductName();
            BigDecimal lineTotal = item.getPriceAtBuy().multiply(BigDecimal.valueOf(item.getQuantity()));

            aggregated.merge(productId, new long[]{item.getQuantity()},
                    (old, n) -> new long[]{old[0] + n[0]});
            names.put(productId, productName);
            revenues.merge(productId, lineTotal, BigDecimal::add);
        }

        return aggregated.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> RevenueStatsResponse.TopProductResponse.builder()
                        .productId(e.getKey())
                        .productName(names.get(e.getKey()))
                        .totalSold(e.getValue()[0])
                        .totalRevenue(revenues.get(e.getKey()))
                        .build())
                .collect(Collectors.toList());
    }

    private ShopApplicationResponse toApplicationResponse(ShopApplication app) {
        return ShopApplicationResponse.builder()
                .id(app.getId())
                .shopName(app.getShopName())
                .description(app.getDescription())
                .taxCode(app.getTaxCode())
                .taxAddress(app.getTaxAddress())
                .taxFullName(app.getTaxFullName())
                .status(app.getStatus())
                .rejectReason(app.getRejectReason())
                .submittedAt(app.getSubmittedAt())
                .resolvedAt(app.getResolvedAt())
                .userId(app.getUser().getId())
                .userEmail(app.getUser().getEmail())
                .userFullName(app.getUser().getFullName())
                .build();
    }

    private ShopResponse toShopResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .description(shop.getDescription())
                .bannerUrl(shop.getBannerUrl())
                .logoUrl(shop.getLogoUrl())
                .rating(shop.getRating())
                .createdAt(shop.getCreatedAt())
                .ownerEmail(shop.getOwner().getEmail())
                .ownerFullName(shop.getOwner().getFullName())
                .build();
    }

    private ShopWalletResponse toWalletResponse(ShopWallet wallet) {
        return ShopWalletResponse.builder()
                .id(wallet.getId())
                .shopId(wallet.getShop().getId())
                .shopName(wallet.getShop().getShopName())
                .balance(wallet.getBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .build();
    }

    private WithdrawalResponse toWithdrawalResponse(Withdrawal w) {
        return WithdrawalResponse.builder()
                .id(w.getId())
                .amount(w.getAmount())
                .status(w.getStatus())
                .requestedAt(w.getRequestedAt())
                .resolvedAt(w.getResolvedAt())
                .shopId(w.getShop().getId())
                .shopName(w.getShop().getShopName())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SELLER — SHOP ORDERS (Đơn hàng từ khách)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * SELLER: Lấy danh sách ShopOrder của shop mình, filter theo status tuỳ chọn.
     * GET /shops/me/orders?status=PENDING
     */
    public List<SellerShopOrderResponse> getMyShopOrders(String userEmail, OrderStatus status) {
        Shop shop = getShopByOwnerEmail(userEmail);
        List<ShopOrder> shopOrders = shopOrderRepository.findByShopIdOrderByIdDesc(shop.getId());

        return shopOrders.stream()
                .filter(so -> status == null || so.getStatus() == status)
                .map(this::toSellerShopOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * SELLER: Cập nhật trạng thái ShopOrder (xác nhận, giao hàng, hủy...).
     * PUT /shops/me/orders/{shopOrderId}/status
     *
     * Luật chuyển trạng thái hợp lệ:
     *   PENDING / PAID     → PREPARING     (xác nhận đơn)
     *   PREPARING          → READY_TO_SHIP | CANCELLED
     *   READY_TO_SHIP      → SHIPPED
     *   SHIPPED            → DELIVERED     ← TẠI ĐÂY: settle tiền vào ví + tạo Commission
     *   DELIVERED / CANCELLED  → không cho thay đổi
     */
    @Transactional
    public SellerShopOrderResponse updateShopOrderStatus(String userEmail, Long shopOrderId,
                                                         UpdateShopOrderStatusRequest request) {
        Shop shop = getShopByOwnerEmail(userEmail);

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!shopOrder.getShop().getId().equals(shop.getId())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        OrderStatus current = shopOrder.getStatus();
        OrderStatus next    = request.getStatus();

        Map<OrderStatus, Set<OrderStatus>> allowedTransitions = new HashMap<>();
        allowedTransitions.put(OrderStatus.PENDING,        EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        allowedTransitions.put(OrderStatus.PAID,           EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        allowedTransitions.put(OrderStatus.PREPARING,      EnumSet.of(OrderStatus.READY_TO_SHIP, OrderStatus.CANCELLED));
        allowedTransitions.put(OrderStatus.READY_TO_SHIP,  EnumSet.of(OrderStatus.SHIPPED));
        allowedTransitions.put(OrderStatus.SHIPPED,        EnumSet.of(OrderStatus.DELIVERED));

        Set<OrderStatus> allowed = allowedTransitions.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new AppException(ErrorCode.ORDER_INVALID_PAYMENT_METHOD);
        }

        shopOrder.setStatus(next);
        shopOrderRepository.save(shopOrder);
        log.info("Seller {} cập nhật ShopOrder #{} từ {} → {}", userEmail, shopOrderId, current, next);

        // ── Khi giao hàng thành công: settle tiền vào ví và trừ commission ──────
        if (next == OrderStatus.DELIVERED) {
            settleDeliveredOrder(shopOrder);
        }

        // ── Khi seller hủy đơn: kiểm tra tất cả ShopOrder của Order đó ─────────
        if (next == OrderStatus.CANCELLED) {
            syncOrderStatusOnShopOrderCancelled(shopOrder);
        }

        return toSellerShopOrderResponse(shopOrder);
    }

    /**
     * Khi ShopOrder chuyển sang DELIVERED:
     * 1. Tính gross = shopTotalAmount (đã trừ voucher discount từ lúc tạo đơn)
     * 2. Tính commission 2% cho sàn
     * 3. netAmount = gross - commission
     * 4. Cộng netAmount vào balance + totalEarned của ShopWallet
     * 5. Lưu bản ghi Commission (idempotent — skip nếu đã tồn tại)
     */
    private void settleDeliveredOrder(ShopOrder shopOrder) {
        // Idempotency: nếu đã settle rồi thì bỏ qua
        if (commissionRepository.findByShopOrderId(shopOrder.getId()).isPresent()) {
            log.warn("settleDeliveredOrder: shopOrderId={} đã được settle, bỏ qua", shopOrder.getId());
            return;
        }

        BigDecimal gross = shopOrder.getShopTotalAmount() != null
                ? shopOrder.getShopTotalAmount() : BigDecimal.ZERO;

        // Commission = gross * 2 / 100
        BigDecimal commissionAmt = gross
                .multiply(COMMISSION_RATE)
                .divide(HUNDRED, 2, java.math.RoundingMode.HALF_UP);

        BigDecimal netAmount = gross.subtract(commissionAmt).max(BigDecimal.ZERO);

        // Lưu Commission record
        commissionRepository.save(Commission.builder()
                .shopOrder(shopOrder)
                .shop(shopOrder.getShop())
                .grossAmount(gross)
                .commissionRate(COMMISSION_RATE)
                .commissionAmt(commissionAmt)
                .netAmount(netAmount)
                .build());

        // Cộng tiền vào ví shop
        ShopWallet wallet = shopWalletRepository.findByShopId(shopOrder.getShop().getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance().add(netAmount));
        wallet.setTotalEarned(wallet.getTotalEarned().add(netAmount));
        shopWalletRepository.save(wallet);

        log.info("Settle ShopOrder #{}: gross={}, commission={}({}%), net={} → credited to shop {}",
                shopOrder.getId(), gross, commissionAmt, COMMISSION_RATE,
                netAmount, shopOrder.getShop().getShopName());
    }

    /**
     * Khi seller hủy 1 ShopOrder → kiểm tra tất cả ShopOrder của Order đó.
     * Nếu TẤT CẢ các ShopOrder đều CANCELLED → cập nhật Order chính sang CANCELLED.
     */
    private void syncOrderStatusOnShopOrderCancelled(ShopOrder cancelledShopOrder) {
        Order order = cancelledShopOrder.getOrder();
        if (order == null) return;

        // Lấy tất cả ShopOrder của Order này
        List<ShopOrder> allShopOrders = order.getShopOrders();
        if (allShopOrders == null || allShopOrders.isEmpty()) return;

        // Kiểm tra xem tất cả ShopOrder có phải CANCELLED không
        boolean allCancelled = allShopOrders.stream()
                .allMatch(so -> so.getStatus() == OrderStatus.CANCELLED);

        if (allCancelled && order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order #{} đã được cập nhật sang CANCELLED vì tất cả ShopOrder đều bị hủy", order.getId());
        }
    }

    private SellerShopOrderResponse toSellerShopOrderResponse(ShopOrder so) {
        List<OrderItemResponse> items = so.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .orderItemId(item.getId())
                        .variantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
                        .variantName(item.getProductVariant() != null ? item.getProductVariant().getVariantName() : null)
                        .sku(item.getProductVariant() != null ? item.getProductVariant().getSku() : null)
                        .productName(item.getProductVariant() != null && item.getProductVariant().getProduct() != null
                                ? item.getProductVariant().getProduct().getProductName() : null)
                        .productImageUrl(item.getProductVariant() != null && item.getProductVariant().getProduct() != null
                                ? item.getProductVariant().getProduct().getImageUrl() : null)
                        .quantity(item.getQuantity())
                        .priceAtBuy(item.getPriceAtBuy())
                        .subtotal(item.getPriceAtBuy() != null
                                ? item.getPriceAtBuy().multiply(BigDecimal.valueOf(item.getQuantity()))
                                : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        Order order = so.getOrder();
        User buyer  = order != null ? order.getBuyer() : null;

        // Tính discount đã phân bổ = sum(item.discountAmount) trong shopOrder
        BigDecimal shopDiscount = so.getOrderItems().stream()
                .map(item -> item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy commission nếu đã DELIVERED
        BigDecimal commissionAmt = null;
        BigDecimal netEarned     = null;
        if (so.getStatus() == OrderStatus.DELIVERED) {
            commissionRepository.findByShopOrderId(so.getId()).ifPresent(c -> {
                // không thể gán trực tiếp vào local var → dùng array wrapper
            });
            var commOpt = commissionRepository.findByShopOrderId(so.getId());
            if (commOpt.isPresent()) {
                commissionAmt = commOpt.get().getCommissionAmt();
                netEarned     = commOpt.get().getNetAmount();
            }
        }

        return SellerShopOrderResponse.builder()
                .shopOrderId(so.getId())
                .status(so.getStatus())
                .shopTotalAmount(so.getShopTotalAmount())
                .shippingFee(so.getShippingFee())
                .items(items)
                .orderId(order != null ? order.getId() : null)
                .paymentMethod(order != null ? order.getPaymentMethod() : null)
                .createdAt(order != null ? order.getCreatedAt() : null)
                .discountAmount(shopDiscount)
                .buyerName(buyer != null ? buyer.getFullName() : null)
                .buyerEmail(buyer != null ? buyer.getEmail() : null)
                .commissionAmt(commissionAmt)
                .netEarned(netEarned)
                .build();
    }
}
