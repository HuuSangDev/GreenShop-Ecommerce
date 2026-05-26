package com.huusang.demo.Service;

import com.huusang.demo.Dto.Response.CommissionResponse;
import com.huusang.demo.Dto.Response.CommissionStatsResponse;
import com.huusang.demo.Entity.Commission;
import com.huusang.demo.Entity.Shop;
import com.huusang.demo.Entity.ShopOrder;
import com.huusang.demo.Entity.ShopWallet;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommissionService {

    CommissionRepository commissionRepository;
    ShopOrderRepository shopOrderRepository;
    ShopWalletRepository shopWalletRepository;
    ShopRepository shopRepository;
    UserRepository userRepository;

    // Commission rate: 2% — sàn thu trên mỗi đơn hoàn thành
    static final BigDecimal COMMISSION_RATE = new BigDecimal("2.00");

    // ═══════════════════════════════════════════════════════════════════════════
    // SYSTEM: Tính hoa hồng khi shop_order → COMPLETED
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Tính hoa hồng cho shop_order khi status = COMPLETED.
     * - Tạo record Commission
     * - Cộng net_amount vào shop_wallet.balance và totalEarned
     * 
     * Được gọi từ OrderService khi seller cập nhật shop_order status → COMPLETED
     * 
     * @param shopOrderId ID của shop_order đã hoàn thành
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CommissionResponse calculateCommission(Long shopOrderId) {
        log.info("Calculating commission for shopOrderId={}", shopOrderId);

        // 1. Lấy ShopOrder
        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // 2. Kiểm tra status phải là COMPLETED
        if (shopOrder.getStatus() != OrderStatus.COMPLETED) {
            log.warn("ShopOrder {} is not COMPLETED, current status: {}", shopOrderId, shopOrder.getStatus());
            throw new AppException(ErrorCode.COMMISSION_ORDER_NOT_COMPLETED);
        }

        // 3. Kiểm tra đã tính commission chưa (idempotency)
        if (commissionRepository.existsByShopOrderId(shopOrderId)) {
            log.warn("Commission already calculated for shopOrderId={}", shopOrderId);
            // Trả về commission đã tồn tại
            Commission existing = commissionRepository.findByShopOrderId(shopOrderId)
                    .orElseThrow(() -> new AppException(ErrorCode.COMMISSION_NOT_FOUND));
            return toCommissionResponse(existing);
        }

        Shop shop = shopOrder.getShop();
        BigDecimal grossAmount = shopOrder.getShopTotalAmount(); // Doanh thu gốc

        // 4. Tính commission
        // commissionAmt = grossAmount * commissionRate / 100
        BigDecimal commissionAmt = grossAmount
                .multiply(COMMISSION_RATE)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // netAmount = grossAmount - commissionAmt (tiền shop thực nhận)
        BigDecimal netAmount = grossAmount.subtract(commissionAmt);

        // 5. Tạo Commission record
        Commission commission = Commission.builder()
                .shopOrder(shopOrder)
                .shop(shop)
                .grossAmount(grossAmount)
                .commissionRate(COMMISSION_RATE)
                .commissionAmt(commissionAmt)
                .netAmount(netAmount)
                .build();

        commissionRepository.save(commission);
        log.info("Commission created: id={}, shopOrderId={}, gross={}, commission={}, net={}",
                commission.getId(), shopOrderId, grossAmount, commissionAmt, netAmount);

        // 6. Cộng netAmount vào ShopWallet
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

        wallet.setBalance(wallet.getBalance().add(netAmount));
        wallet.setTotalEarned(wallet.getTotalEarned().add(netAmount));
        shopWalletRepository.save(wallet);

        log.info("ShopWallet updated: shopId={}, newBalance={}, totalEarned={}",
                shop.getId(), wallet.getBalance(), wallet.getTotalEarned());

        return toCommissionResponse(commission);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SELLER/ADMIN: Xem chi tiết hoa hồng của 1 shop_order
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Xem chi tiết hoa hồng của 1 shop_order cụ thể.
     * Quyền: SELLER (chỉ xem shop của mình) hoặc ADMIN (xem tất cả)
     * 
     * @param shopOrderId ID của shop_order
     * @param userEmail Email của user (để validate quyền nếu là SELLER)
     * @return CommissionResponse
     */
    @Transactional(readOnly = true)
    public CommissionResponse getCommissionByShopOrder(Long shopOrderId, String userEmail) {
        Commission commission = commissionRepository.findByShopOrderId(shopOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMISSION_NOT_FOUND));

        // Validate quyền: nếu không phải ADMIN, phải là owner của shop
        User user = getUserByEmail(userEmail);
        if (!isAdmin(user) && !commission.getShop().getOwner().getId().equals(user.getId())) {
            log.warn("User {} tried to access commission of shopOrderId={} without permission",
                    userEmail, shopOrderId);
            throw new AppException(ErrorCode.UNAUTHORIZED_VOUCHER); // Hoặc tạo error code mới
        }

        return toCommissionResponse(commission);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADMIN: Xem tổng hoa hồng sàn đã thu
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Admin xem tổng hoa hồng sàn đã thu, có thể filter theo ngày/tháng.
     * 
     * @param period "DAILY" | "MONTHLY" | null (tất cả)
     * @return CommissionStatsResponse
     */
    @Transactional(readOnly = true)
    public CommissionStatsResponse getCommissionStats(String period) {
        List<Commission> allCommissions = commissionRepository.findAll();

        BigDecimal totalCommission = allCommissions.stream()
                .map(Commission::getCommissionAmt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGrossAmount = allCommissions.stream()
                .map(Commission::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNetAmount = allCommissions.stream()
                .map(Commission::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalOrders = (long) allCommissions.size();

        // Group by period
        Map<String, BigDecimal> commissionByPeriod = groupCommissionByPeriod(allCommissions, period);

        return CommissionStatsResponse.builder()
                .totalCommission(totalCommission)
                .totalGrossAmount(totalGrossAmount)
                .totalNetAmount(totalNetAmount)
                .totalOrders(totalOrders)
                .commissionByPeriod(commissionByPeriod)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SELLER: Xem lịch sử hoa hồng bị trừ trên từng đơn
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Shop xem lịch sử hoa hồng bị trừ trên từng đơn của mình.
     * 
     * @param userEmail Email của seller
     * @return List<CommissionResponse>
     */
    @Transactional(readOnly = true)
    public List<CommissionResponse> getShopCommissions(String userEmail) {
        User user = getUserByEmail(userEmail);
        Shop shop = shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        List<Commission> commissions = commissionRepository.findByShopIdOrderByCreatedAtDesc(shop.getId());

        return commissions.stream()
                .map(this::toCommissionResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }

    private Map<String, BigDecimal> groupCommissionByPeriod(List<Commission> commissions, String period) {
        if (period == null || period.isBlank()) {
            return Collections.emptyMap();
        }

        Map<String, BigDecimal> result = new TreeMap<>();
        DateTimeFormatter formatter;

        switch (period.toUpperCase()) {
            case "MONTHLY" -> formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            case "DAILY" -> formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            default -> {
                return Collections.emptyMap();
            }
        }

        for (Commission commission : commissions) {
            LocalDateTime createdAt = commission.getShopOrder().getOrder().getCreatedAt();
            if (createdAt != null) {
                String key = createdAt.format(formatter);
                result.merge(key, commission.getCommissionAmt(), BigDecimal::add);
            }
        }

        return result;
    }

    private CommissionResponse toCommissionResponse(Commission commission) {
        return CommissionResponse.builder()
                .id(commission.getId())
                .shopOrderId(commission.getShopOrder().getId())
                .shopId(commission.getShop().getId())
                .shopName(commission.getShop().getShopName())
                .grossAmount(commission.getGrossAmount())
                .commissionRate(commission.getCommissionRate())
                .commissionAmt(commission.getCommissionAmt())
                .netAmount(commission.getNetAmount())
                .build();
    }
}
