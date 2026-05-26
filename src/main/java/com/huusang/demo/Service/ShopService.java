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

    // ─────────────────────────────────────────────────────────────────────────
    //  SHOP APPLICATION (Đăng ký mở gian hàng)
    // ─────────────────────────────────────────────────────────────────────────

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
    public ShopResponse getMyShop(String userEmail) {
        User user = getUserByEmail(userEmail);
        Shop shop = shopRepository.findByOwnerId(user.getId())
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
                .orElseGet(() -> {
                    log.info("ShopWallet not found for shopId={}, creating a new one", shop.getId());
                    ShopWallet newWallet = ShopWallet.builder()
                            .shop(shop)
                            .balance(BigDecimal.ZERO)
                            .totalEarned(BigDecimal.ZERO)
                            .totalWithdrawn(BigDecimal.ZERO)
                            .build();
                    return shopWalletRepository.save(newWallet);
                });
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
     * SELLER: Tạo yêu cầu rút tiền, kiểm tra số dư đủ không.
     */
    @Transactional
    public WithdrawalResponse requestWithdrawal(String userEmail, WithdrawalRequest request) {
        Shop shop = getShopByOwnerEmail(userEmail);
        ShopWallet wallet = shopWalletRepository.findByShopId(shop.getId())
                .orElseGet(() -> {
                    log.info("ShopWallet not found for shopId={}, creating a new one", shop.getId());
                    ShopWallet newWallet = ShopWallet.builder()
                            .shop(shop)
                            .balance(BigDecimal.ZERO)
                            .totalEarned(BigDecimal.ZERO)
                            .totalWithdrawn(BigDecimal.ZERO)
                            .build();
                    return shopWalletRepository.save(newWallet);
                });

        // Kiểm tra số dư
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ErrorCode.SHOP_INSUFFICIENT_BALANCE);
        }

        // Trừ tiền khỏi balance (tạm giữ chờ duyệt)
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        shopWalletRepository.save(wallet);

        // Tạo Withdrawal request
        Withdrawal withdrawal = Withdrawal.builder()
                .shop(shop)
                .amount(request.getAmount())
                .status(WithdrawalStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        withdrawalRepository.save(withdrawal);

        log.info("Shop {} đã tạo yêu cầu rút {} VND", shop.getShopName(), request.getAmount());
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
     * ADMIN: Duyệt hoặc từ chối yêu cầu rút tiền.
     * - APPROVED: cập nhật totalWithdrawn
     * - REJECTED: hoàn tiền lại balance
     */
    @Transactional
    public WithdrawalResponse processWithdrawal(String withdrawalId, ProcessWithdrawalRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_WITHDRAWAL_NOT_FOUND));

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new AppException(ErrorCode.SHOP_WITHDRAWAL_NOT_PENDING);
        }

        if (request.getStatus() == WithdrawalStatus.APPROVED) {
            // Cập nhật totalWithdrawn trong wallet
            ShopWallet wallet = shopWalletRepository.findByShopId(withdrawal.getShop().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.SHOP_WALLET_NOT_FOUND));
            wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(withdrawal.getAmount()));
            shopWalletRepository.save(wallet);
            log.info("Admin đã DUYỆT rút tiền {} — shop {}", withdrawalId, withdrawal.getShop().getShopName());

        } else if (request.getStatus() == WithdrawalStatus.REJECTED) {
            // Hoàn tiền lại balance
            ShopWallet wallet = shopWalletRepository.findByShopId(withdrawal.getShop().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.SHOP_WALLET_NOT_FOUND));
            wallet.setBalance(wallet.getBalance().add(withdrawal.getAmount()));
            shopWalletRepository.save(wallet);
            log.info("Admin đã TỪ CHỐI rút tiền {} — hoàn lại {} vào ví shop", withdrawalId, withdrawal.getAmount());
        }

        withdrawal.setStatus(request.getStatus());
        withdrawal.setResolvedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
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
}
