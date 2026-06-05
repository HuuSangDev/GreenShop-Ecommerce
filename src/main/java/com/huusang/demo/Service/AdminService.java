package com.huusang.demo.Service;

import com.huusang.demo.Dto.Response.AdminWalletResponse;
import com.huusang.demo.Dto.Response.ShopWalletResponse;
import com.huusang.demo.Dto.Response.OrderResponse;
import com.huusang.demo.Dto.Response.ShopOrderResponse;
import com.huusang.demo.Dto.Response.WithdrawalResponse;
import com.huusang.demo.Dto.Request.WithdrawalRequest;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Enum.PaymentMethod;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminService {

    UserRepository userRepository;
    ShopRepository shopRepository;
    ProductRepository productRepository;
    OrderRepository orderRepository;
    ShopOrderRepository shopOrderRepository;
    PaymentRepository paymentRepository;
    WithdrawalRepository withdrawalRepository;
    AdminWalletRepository adminWalletRepository;
    ShopWalletRepository shopWalletRepository;
    CommissionRepository commissionRepository;

    // ═══════════════════════════════════════════════════════════════════════════
    // ANALYTICS & DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Tổng GMV (Gross Merchandise Value)
        BigDecimal totalGmv = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Đơn hàng hôm nay
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long ordersToday = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfDay))
                .count();

        // Tổng đơn hàng
        long totalOrders = orderRepository.count();

        // Tổng người dùng
        long totalUsers = userRepository.count();

        // Tổng shop
        long totalShops = shopRepository.count();

        // Tính toán top sản phẩm bán chạy từ dữ liệu thực tế
        Map<Product, Integer> productSalesMap = new HashMap<>();
        Map<Product, BigDecimal> productRevenueMap = new HashMap<>();

        shopOrderRepository.findAll().stream()
                .filter(so -> so.getStatus() == OrderStatus.COMPLETED || so.getStatus() == OrderStatus.DELIVERED)
                .forEach(shopOrder -> {
                    if (shopOrder.getOrderItems() != null) {
                        shopOrder.getOrderItems().forEach(item -> {
                            if (item.getProductVariant() != null && item.getProductVariant().getProduct() != null) {
                                Product product = item.getProductVariant().getProduct();
                                productSalesMap.put(product, productSalesMap.getOrDefault(product, 0) + item.getQuantity());
                                BigDecimal revenue = item.getPriceAtBuy().multiply(BigDecimal.valueOf(item.getQuantity()));
                                productRevenueMap.put(product, productRevenueMap.getOrDefault(product, BigDecimal.ZERO).add(revenue));
                            }
                        });
                    }
                });

        List<Map<String, Object>> topProducts = productSalesMap.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Product product = entry.getKey();
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", product.getProductName());
                    map.put("sales", entry.getValue());
                    map.put("revenue", productRevenueMap.get(product));
                    return map;
                })
                .collect(Collectors.toList());

        // Đơn hàng gần đây
        List<Map<String, Object>> recentOrders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(50)
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", "ORD" + order.getId());
                    orderMap.put("customer", order.getBuyer() != null ? order.getBuyer().getFullName() : "Unknown");
                    orderMap.put("status", order.getStatus().name());
                    orderMap.put("amount", order.getFinalAmount());
                    orderMap.put("createdAt", order.getCreatedAt());
                    return orderMap;
                })
                .collect(Collectors.toList());

        overview.put("totalGmv", totalGmv);
        overview.put("totalOrdersToday", ordersToday);
        overview.put("totalOrdersAllTime", totalOrders);
        overview.put("totalUsers", totalUsers);
        overview.put("totalShops", totalShops);
        overview.put("topProducts", topProducts);
        overview.put("recentOrders", recentOrders);

        return overview;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRevenueChart(String period) {
        List<Map<String, Object>> chartData = new ArrayList<>();

        if ("DAILY".equals(period)) {
            // Doanh thu 7 ngày gần nhất
            for (int i = 6; i >= 0; i--) {
                LocalDateTime date = LocalDateTime.now().minusDays(i);
                LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
                LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);

                BigDecimal revenue = orderRepository.findAll().stream()
                        .filter(o -> o.getCreatedAt().isAfter(startOfDay) && o.getCreatedAt().isBefore(endOfDay))
                        .filter(o -> o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.DELIVERED)
                        .map(Order::getFinalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> data = new HashMap<>();
                data.put("label", date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
                data.put("revenue", revenue);
                chartData.add(data);
            }
        } else if ("MONTHLY".equals(period)) {
            // Doanh thu 12 tháng gần nhất
            for (int i = 11; i >= 0; i--) {
                LocalDateTime date = LocalDateTime.now().minusMonths(i);
                LocalDateTime startOfMonth = date.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime endOfMonth = date.withDayOfMonth(date.toLocalDate().lengthOfMonth())
                        .withHour(23).withMinute(59).withSecond(59);

                BigDecimal revenue = orderRepository.findAll().stream()
                        .filter(o -> o.getCreatedAt().isAfter(startOfMonth) && o.getCreatedAt().isBefore(endOfMonth))
                        .filter(o -> o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.DELIVERED)
                        .map(Order::getFinalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> data = new HashMap<>();
                data.put("label", "T" + date.getMonthValue() + "/" + date.getYear());
                data.put("revenue", revenue);
                chartData.add(data);
            }
        }

        return chartData;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // USER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public void banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} has been banned", userId);
    }

    @Transactional
    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setActive(true);
        userRepository.save(user);
        log.info("User {} has been unbanned", userId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRODUCT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public void hideProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setAvailable(false);
        productRepository.save(product);
        log.info("Product {} has been hidden", productId);
    }

    @Transactional
    public void unhideProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setAvailable(true);
        productRepository.save(product);
        log.info("Product {} has been unhidden", productId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ORDER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }

        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    // Buyer info
                    User buyer = order.getBuyer();
                    
                    // Shop orders info
                    List<ShopOrderResponse> shopOrderResponses = order.getShopOrders() != null
                            ? order.getShopOrders().stream()
                                    .map(so -> ShopOrderResponse.builder()
                                            .shopOrderId(so.getId())
                                            .shopId(so.getShop().getId())
                                            .shopName(so.getShop().getShopName())
                                            .status(so.getStatus())
                                            .shopTotalAmount(so.getShopTotalAmount())
                                            .build())
                                    .collect(Collectors.toList())
                            : List.of();
                    
                    return OrderResponse.builder()
                            .orderId(order.getId())
                            .status(order.getStatus())
                            .paymentMethod(PaymentMethod.valueOf(order.getPaymentMethod()))
                            .totalAmount(order.getTotalAmount())
                            .discountAmount(order.getDiscountAmount())
                            .finalAmount(order.getFinalAmount())
                            .createdAt(order.getCreatedAt())
                            .totalShops(shopOrderResponses.size())
                            .totalItems(order.getShopOrders() != null
                                    ? order.getShopOrders().stream()
                                            .flatMap(so -> so.getOrderItems().stream())
                                            .mapToInt(OrderItem::getQuantity).sum()
                                    : 0)
                            .buyerFullName(buyer != null ? buyer.getFullName() : null)
                            .buyerEmail(buyer != null ? buyer.getEmail() : null)
                            .shopOrders(shopOrderResponses)
                            .build();
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), orderResponses.size());
        List<OrderResponse> pageContent = orderResponses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, orderResponses.size());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DISPUTE MANAGEMENT (Mock - cần implement thực tế)
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getDisputes(String status, Pageable pageable) {
        // Mock data - trong thực tế cần có bảng Dispute
        List<Map<String, Object>> disputes = new ArrayList<>();
        return new PageImpl<>(disputes, pageable, 0);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDisputeDetail(Long id) {
        // Mock data - trong thực tế cần có bảng Dispute
        Map<String, Object> dispute = new HashMap<>();
        dispute.put("id", id);
        dispute.put("status", "PENDING");
        dispute.put("reason", "Sản phẩm lỗi");
        return dispute;
    }

    @Transactional
    public void resolveDispute(Long id, String verdict, String adminNote) {
        // Mock - trong thực tế cần update bảng Dispute
        log.info("Dispute {} resolved with verdict: {}, note: {}", id, verdict, adminNote);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WITHDRAWALS MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════════════════════
    // WALLET MANAGEMENT (Mock - cần implement thực tế)
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public AdminWalletResponse getAdminWallet() {
        AdminWallet wallet = getOrCreateAdminWallet();
        return toAdminWalletResponse(wallet);
    }

    @Transactional
    public AdminWalletResponse withdrawFromAdminWallet(WithdrawalRequest request) {
        AdminWallet wallet = getOrCreateAdminWallet();
        
        // Kiểm tra số dư
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ErrorCode.SHOP_INSUFFICIENT_BALANCE);
        }

        // Trừ tiền khỏi balance
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(request.getAmount()));
        adminWalletRepository.save(wallet);

        // Lưu lịch sử giao dịch
        Withdrawal transaction = Withdrawal.builder()
                .type(com.huusang.demo.Enum.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .status(com.huusang.demo.Enum.WithdrawalStatus.APPROVED)
                .note(request.getNote() != null ? request.getNote() : "Rút tiền từ ví Admin")
                .createdAt(LocalDateTime.now())
                .build();
        withdrawalRepository.save(transaction);

        log.info("Admin withdrew {} VND", request.getAmount());
        return toAdminWalletResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<ShopWalletResponse> getAllShopWallets() {
        return shopWalletRepository.findAll().stream()
                .map(wallet -> {
                    // Eager load shop để tránh lazy loading exception
                    Shop shop = wallet.getShop();
                    return ShopWalletResponse.builder()
                            .id(wallet.getId())
                            .shopId(shop.getId())
                            .shopName(shop.getShopName())
                            .balance(wallet.getBalance())
                            .totalEarned(wallet.getTotalEarned())
                            .totalWithdrawn(wallet.getTotalWithdrawn())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Withdrawal> getAdminTransactions(Pageable pageable) {
        return withdrawalRepository.findByShopIsNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<WithdrawalResponse> getSellerWithdrawals(Pageable pageable) {
        Page<Withdrawal> withdrawals = withdrawalRepository.findAll(pageable);
        return withdrawals.map(withdrawal -> {
            // Eager load shop để tránh lazy loading exception
            Shop shop = withdrawal.getShop();
            return WithdrawalResponse.builder()
                    .id(withdrawal.getId())
                    .shopId(shop.getId())
                    .shopName(shop.getShopName())
                    .amount(withdrawal.getAmount())
                    .status(withdrawal.getStatus() != null ? withdrawal.getStatus().name() : null)
                    .createdAt(withdrawal.getCreatedAt())
                    .note(withdrawal.getNote())
                    .type(withdrawal.getType() != null ? withdrawal.getType().name() : null)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public Page<com.huusang.demo.Dto.Response.CommissionResponse> getCommissions(Pageable pageable) {
        Page<Commission> commissions = commissionRepository.findAll(pageable);
        return commissions.map(commission -> {
            Shop shop = commission.getShop();
            ShopOrder shopOrder = commission.getShopOrder();
            return com.huusang.demo.Dto.Response.CommissionResponse.builder()
                    .id(commission.getId())
                    .shopOrderId(shopOrder.getId())
                    .shopId(shop.getId())
                    .shopName(shop.getShopName())
                    .grossAmount(commission.getGrossAmount())
                    .commissionRate(commission.getCommissionRate())
                    .commissionAmt(commission.getCommissionAmt())
                    .netAmount(commission.getNetAmount())
                    .createdAt(shopOrder.getOrder().getCreatedAt())
                    .build();
        });
    }

    private AdminWallet getOrCreateAdminWallet() {
        return adminWalletRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    AdminWallet newWallet = AdminWallet.builder()
                            .balance(BigDecimal.ZERO)
                            .totalEarned(BigDecimal.ZERO)
                            .totalWithdrawn(BigDecimal.ZERO)
                            .build();
                    return adminWalletRepository.save(newWallet);
                });
    }

    private AdminWalletResponse toAdminWalletResponse(AdminWallet wallet) {
        long deposits = withdrawalRepository.countByShopIsNullAndType(com.huusang.demo.Enum.TransactionType.DEPOSIT);
        long withdrawals = withdrawalRepository.countByShopIsNullAndType(com.huusang.demo.Enum.TransactionType.WITHDRAWAL);

        return AdminWalletResponse.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .totalDepositCount(deposits)
                .totalWithdrawCount(withdrawals)
                .build();
    }
}
