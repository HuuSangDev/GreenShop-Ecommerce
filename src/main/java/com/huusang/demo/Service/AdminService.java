package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.DisputeCreateRequest;
import com.huusang.demo.Dto.Request.DisputeResolveRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminService {

    // ─── Repositories ─────────────────────────────────────────────────────────
    DisputeRepository     disputeRepository;
    ShopOrderRepository   shopOrderRepository;
    ShopWalletRepository  shopWalletRepository;
    OrderRepository       orderRepository;
    ShopRepository        shopRepository;
    UserRepository        userRepository;
    WithdrawalRepository  withdrawalRepository;

    // ═══════════════════════════════════════════════════════════════════════════
    // DISPUTE — Khiếu nại
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * BUYER: Tạo khiếu nại cho một ShopOrder.
     * Chỉ cho phép khiếu nại khi ShopOrder ở trạng thái DELIVERED hoặc COMPLETED.
     */
    @Transactional
    public DisputeResponse createDispute(String buyerEmail, DisputeCreateRequest request) {
        User buyer = getUserByEmail(buyerEmail);

        ShopOrder shopOrder = shopOrderRepository.findById(request.getShopOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_SHOP_ORDER_NOT_FOUND));

        // Kiểm tra buyer có phải người đặt hàng không
        if (!shopOrder.getOrder().getBuyer().getId().equals(buyer.getId())) {
            throw new AppException(ErrorCode.ORDER_CART_ITEM_NOT_OWNED);
        }

        // Kiểm tra đã có dispute chưa
        if (disputeRepository.existsByShopOrderId(shopOrder.getId())) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_EXISTS);
        }

        Dispute dispute = Dispute.builder()
                .shopOrder(shopOrder)
                .buyer(buyer)
                .reason(request.getReason())
                .build();
        dispute = disputeRepository.save(dispute);

        log.info("Dispute created: id={}, shopOrderId={}, buyer={}",
                dispute.getId(), shopOrder.getId(), buyerEmail);
        return toDisputeResponse(dispute);
    }

    /**
     * ADMIN: Lấy danh sách tất cả disputes, lọc theo status.
     * status = null → lấy tất cả.
     */
    public Page<DisputeResponse> getDisputes(DisputeStatus status, int page, int size) {
        return disputeRepository
                .findAllFiltered(status, PageRequest.of(page, size))
                .map(this::toDisputeResponse);
    }

    /**
     * ADMIN: Giải quyết khiếu nại với phán quyết.
     *
     * RESOLVED_BUYER_WIN  → Trừ ShopWallet.balance tương đương shopTotalAmount của ShopOrder
     *                        (hoàn tiền ngầm định cho người mua).
     * RESOLVED_SELLER_WIN → Không thay đổi tài chính.
     */
    @Transactional
    public DisputeResponse resolveDispute(String disputeId, DisputeResolveRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));

        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new AppException(ErrorCode.DISPUTE_ALREADY_RESOLVED);
        }

        // Chỉ chấp nhận 2 verdict hợp lệ
        DisputeStatus verdict = request.getVerdict();
        if (verdict != DisputeStatus.RESOLVED_BUYER_WIN && verdict != DisputeStatus.RESOLVED_SELLER_WIN) {
            throw new AppException(ErrorCode.DISPUTE_INVALID_VERDICT);
        }

        ShopOrder shopOrder = dispute.getShopOrder();

        if (verdict == DisputeStatus.RESOLVED_BUYER_WIN) {
            // Trừ balance của ShopWallet (tương đương hoàn tiền cho người mua)
            ShopWallet wallet = shopWalletRepository.findByShopId(shopOrder.getShop().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.SHOP_WALLET_NOT_FOUND));

            BigDecimal refundAmount = shopOrder.getShopTotalAmount();
            BigDecimal newBalance = wallet.getBalance().subtract(refundAmount);

            // Không cho phép balance âm — set về 0 nếu nhỏ hơn
            wallet.setBalance(newBalance.max(BigDecimal.ZERO));
            wallet.setTotalEarned(wallet.getTotalEarned().subtract(refundAmount).max(BigDecimal.ZERO));
            shopWalletRepository.save(wallet);

            log.info("Dispute BUYER_WIN: shopOrderId={}, refund={}, shopId={}",
                    shopOrder.getId(), refundAmount, shopOrder.getShop().getId());
        } else {
            log.info("Dispute SELLER_WIN: shopOrderId={}, no financial change", shopOrder.getId());
        }

        dispute.setStatus(verdict);
        dispute.setAdminNote(request.getAdminNote());
        dispute.setResolvedAt(LocalDateTime.now());
        dispute = disputeRepository.save(dispute);

        return toDisputeResponse(dispute);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ORDERS — Quản lý đơn hàng toàn hệ thống
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * ADMIN: Lấy danh sách đơn hàng toàn hệ thống.
     * Hỗ trợ lọc theo status và tìm kiếm theo email/tên người mua.
     */
    public Page<AdminOrderResponse> getAdminOrders(OrderStatus status, String keyword, int page, int size) {
        // keyword rỗng → null để bỏ qua điều kiện filter trong JPQL
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return orderRepository
                .findAdminOrders(status, kw, PageRequest.of(page, size))
                .map(this::toAdminOrderResponse);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANALYTICS — Dashboard
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * ADMIN: Dashboard overview — các con số tổng kết nhanh.
     */
    public AdminOverviewResponse getOverview() {
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

        BigDecimal gmv = orderRepository.sumGmv();
        long ordersToday = orderRepository.countByCreatedAtAfter(startOfToday);
        long totalOrders = orderRepository.count();
        long shopsToday = shopRepository.countByCreatedAtAfter(startOfToday);
        long totalShops = shopRepository.count();
        long openDisputes = disputeRepository.countByStatus(DisputeStatus.OPEN);
        long pendingWithdrawals = withdrawalRepository.findByStatus(WithdrawalStatus.PENDING).size();
        long totalUsers = userRepository.count();

        return AdminOverviewResponse.builder()
                .totalGmv(gmv != null ? gmv : BigDecimal.ZERO)
                .totalOrdersToday(ordersToday)
                .totalOrdersAllTime(totalOrders)
                .newShopsToday(shopsToday)
                .totalShopsAllTime(totalShops)
                .openDisputes(openDisputes)
                .pendingWithdrawals(pendingWithdrawals)
                .totalUsers(totalUsers)
                .build();
    }

    /**
     * ADMIN: Dữ liệu biểu đồ doanh thu theo ngày/tháng/năm.
     * period: "DAILY" | "MONTHLY" | "YEARLY"
     */
    public RevenueChartResponse getRevenueChart(String period) {
        LocalDateTime since = switch (period.toUpperCase()) {
            case "MONTHLY" -> LocalDateTime.now().minusMonths(12);   // 12 tháng gần nhất
            case "YEARLY"  -> LocalDateTime.now().minusYears(5);     // 5 năm gần nhất
            default        -> LocalDateTime.now().minusDays(30);     // 30 ngày gần nhất
        };

        List<Object[]> rawData = switch (period.toUpperCase()) {
            case "MONTHLY" -> orderRepository.sumRevenueGroupByMonth(since);
            case "YEARLY"  -> orderRepository.sumRevenueGroupByYear(since);
            default        -> orderRepository.sumRevenueGroupByDay(since);
        };

        List<RevenueChartResponse.ChartPoint> points = rawData.stream()
                .map(row -> RevenueChartResponse.ChartPoint.builder()
                        .label(row[0] != null ? row[0].toString() : "")
                        .revenue(row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = points.stream()
                .map(RevenueChartResponse.ChartPoint::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueChartResponse.builder()
                .period(period.toUpperCase())
                .data(points)
                .totalRevenue(total)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // USER MANAGEMENT — Ban / Unban
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * ADMIN: Khóa tài khoản người dùng.
     * Sau khi bị ban, SecurityConfig sẽ từ chối JWT của user này (check user.active trong decoder).
     */
    @Transactional
    public UserResponse banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_ALREADY_BANNED);
        }

        user.setActive(false);
        user = userRepository.save(user);
        log.info("Admin BANNED user: id={}, email={}", userId, user.getEmail());
        return toUserResponse(user);
    }

    /**
     * ADMIN: Kích hoạt lại tài khoản người dùng.
     */
    @Transactional
    public UserResponse unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.isActive()) {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        user.setActive(true);
        user = userRepository.save(user);
        log.info("Admin UNBANNED user: id={}, email={}", userId, user.getEmail());
        return toUserResponse(user);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private DisputeResponse toDisputeResponse(Dispute dispute) {
        ShopOrder so = dispute.getShopOrder();
        return DisputeResponse.builder()
                .id(dispute.getId())
                .shopOrderId(so.getId())
                .shopId(so.getShop().getId())
                .shopName(so.getShop().getShopName())
                .buyerId(dispute.getBuyer().getId())
                .buyerEmail(dispute.getBuyer().getEmail())
                .buyerFullName(dispute.getBuyer().getFullName())
                .reason(dispute.getReason())
                .status(dispute.getStatus())
                .adminNote(dispute.getAdminNote())
                .shopTotalAmount(so.getShopTotalAmount())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }

    private AdminOrderResponse toAdminOrderResponse(Order order) {
        List<AdminOrderResponse.ShopSummary> shops = order.getShopOrders() == null ? List.of() :
                order.getShopOrders().stream()
                        .map(so -> AdminOrderResponse.ShopSummary.builder()
                                .shopOrderId(so.getId())
                                .shopId(so.getShop().getId())
                                .shopName(so.getShop().getShopName())
                                .shopOrderStatus(so.getStatus())
                                .shopTotalAmount(so.getShopTotalAmount())
                                .build())
                        .collect(Collectors.toList());

        int totalItems = order.getShopOrders() == null ? 0 :
                order.getShopOrders().stream()
                        .flatMap(so -> so.getOrderItems().stream())
                        .mapToInt(OrderItem::getQuantity)
                        .sum();

        return AdminOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .buyerId(order.getBuyer().getId())
                .buyerEmail(order.getBuyer().getEmail())
                .buyerFullName(order.getBuyer().getFullName())
                .totalShops(shops.size())
                .totalItems(totalItems)
                .shops(shops)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .active(user.isActive())
                .build();
    }
}
