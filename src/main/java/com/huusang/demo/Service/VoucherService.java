package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.ApplyVoucherRequest;
import com.huusang.demo.Dto.Request.VoucherCreateRequest;
import com.huusang.demo.Dto.Request.VoucherUpdateRequest;
import com.huusang.demo.Dto.Response.ApplyVoucherResponse;
import com.huusang.demo.Dto.Response.VoucherResponse;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Enum.VoucherType;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    // ==================== HELPER ====================

    private String getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }

    private Voucher getVoucherOrThrow(String voucherId) {
        return voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
    }

    // Kiểm tra quyền sở hữu:
    // - Voucher toàn sàn (shop = null): chỉ Admin mới thao tác → dùng @PreAuthorize ở controller
    // - Voucher của shop: chỉ chủ shop mới thao tác
    private void checkVoucherOwnership(Voucher voucher, String userId) {
        if (voucher.getShop() != null && !voucher.getShop().getOwner().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_VOUCHER);
        }
    }

    private VoucherResponse toResponse(Voucher v) {
        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .type(v.getType())
                .value(v.getValue())
                .maxDiscount(v.getMaxDiscount())
                .minOrderAmt(v.getMinOrderAmt())
                .maxUsage(v.getMaxUsage())
                .usedCount(v.getUsedCount())
                .remainingUsage(v.getMaxUsage() != null ? v.getMaxUsage() - v.getUsedCount() : null)
                .startsAt(v.getStartsAt())
                .expiresAt(v.getExpiresAt())
                .active(v.isActive())
                .shopId(v.getShop() != null ? v.getShop().getId() : null)
                .shopName(v.getShop() != null ? v.getShop().getShopName() : null)
                .build();
    }

    // ==================== CRUD ====================

    /**
     * Tạo voucher
     * - Admin: shopId = null → voucher toàn sàn
     * - Seller: shopId = id shop của mình → voucher riêng shop
     */
    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request) {
        String userId = getCurrentUserId();

        if (voucherRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
        }

        if (request.getExpiresAt().isBefore(request.getStartsAt())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED);
        }

        Shop shop = null;
        if (request.getShopId() != null) {
            shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
            // Seller chỉ được tạo voucher cho shop của mình
            if (!shop.getOwner().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED_VOUCHER);
            }
        }

        Voucher voucher = Voucher.builder()
                .shop(shop)
                .code(request.getCode().toUpperCase())
                .type(request.getType())
                .value(request.getValue())
                .maxDiscount(request.getMaxDiscount())
                .minOrderAmt(request.getMinOrderAmt() != null ? request.getMinOrderAmt() : BigDecimal.ZERO)
                .maxUsage(request.getMaxUsage())
                .usedCount(0)
                .startsAt(request.getStartsAt())
                .expiresAt(request.getExpiresAt())
                .active(true)
                .build();

        return toResponse(voucherRepository.save(voucher));
    }

    /**
     * Cập nhật voucher — không cho sửa nếu đã có người dùng
     */
    @Transactional
    public VoucherResponse updateVoucher(String voucherId, VoucherUpdateRequest request) {
        String userId = getCurrentUserId();
        Voucher voucher = getVoucherOrThrow(voucherId);
        checkVoucherOwnership(voucher, userId);

        if (voucher.getUsedCount() > 0) {
            throw new AppException(ErrorCode.VOUCHER_CANNOT_UPDATE);
        }

        if (request.getValue() != null)      voucher.setValue(request.getValue());
        if (request.getMaxDiscount() != null) voucher.setMaxDiscount(request.getMaxDiscount());
        if (request.getMinOrderAmt() != null) voucher.setMinOrderAmt(request.getMinOrderAmt());
        if (request.getMaxUsage() != null)   voucher.setMaxUsage(request.getMaxUsage());
        if (request.getStartsAt() != null)   voucher.setStartsAt(request.getStartsAt());
        if (request.getExpiresAt() != null)  voucher.setExpiresAt(request.getExpiresAt());

        return toResponse(voucherRepository.save(voucher));
    }

    /**
     * Vô hiệu hóa voucher trước hạn
     */
    @Transactional
    public VoucherResponse deactivateVoucher(String voucherId) {
        String userId = getCurrentUserId();
        Voucher voucher = getVoucherOrThrow(voucherId);
        checkVoucherOwnership(voucher, userId);

        if (!voucher.isActive()) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }

        voucher.setActive(false);
        return toResponse(voucherRepository.save(voucher));
    }

    /**
     * Kích hoạt lại voucher đã bị vô hiệu hóa
     */
    @Transactional
    public VoucherResponse activateVoucher(String voucherId) {
        String userId = getCurrentUserId();
        Voucher voucher = getVoucherOrThrow(voucherId);
        checkVoucherOwnership(voucher, userId);

        if (voucher.isActive()) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_ACTIVE);
        }
        if (LocalDateTime.now().isAfter(voucher.getExpiresAt())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED);
        }

        voucher.setActive(true);
        return toResponse(voucherRepository.save(voucher));
    }

    /**
     * Danh sách voucher của 1 shop (Seller xem)
     */
    public List<VoucherResponse> getVouchersByShop(Long shopId) {
        String userId = getCurrentUserId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        if (!shop.getOwner().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_VOUCHER);
        }

        return voucherRepository.findByShopId(shopId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Voucher user có thể dùng cho đơn hàng hiện tại
     * Bao gồm: voucher toàn sàn + voucher của shop đang mua
     */
    public List<VoucherResponse> getAvailableVouchers(BigDecimal orderAmount, Long shopId) {
        String userId = getCurrentUserId();
        return voucherRepository
                .findAvailableVouchers(LocalDateTime.now(), orderAmount, shopId, userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ==================== CORE LOGIC ====================

    /**
     * Validate voucher thuần túy — dùng nội bộ, không tính discount
     * Ném exception nếu không hợp lệ
     */
    public Voucher validateVoucher(String code, BigDecimal orderAmount, String userId) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        if (!voucher.isActive()) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }
        if (now.isBefore(voucher.getStartsAt())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_STARTED);
        }
        if (now.isAfter(voucher.getExpiresAt())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED);
        }
        if (voucher.getMaxUsage() != null && voucher.getUsedCount() >= voucher.getMaxUsage()) {
            throw new AppException(ErrorCode.VOUCHER_USAGE_LIMIT_REACHED);
        }
        if (orderAmount.compareTo(voucher.getMinOrderAmt()) < 0) {
            throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET);
        }
        if (voucherUsageRepository.existsByVoucherIdAndUserId(voucher.getId(), userId)) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
        }

        return voucher;
    }

    /**
     * Apply voucher — validate + tính discount
     */
    public ApplyVoucherResponse applyVoucher(ApplyVoucherRequest request) {
        String userId = getCurrentUserId();
        Voucher voucher = validateVoucher(request.getCode(), request.getOrderAmount(), userId);

        BigDecimal discount = calculateDiscount(voucher, request.getOrderAmount());
        BigDecimal finalAmount = request.getOrderAmount().subtract(discount).max(BigDecimal.ZERO);

        return ApplyVoucherResponse.builder()
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .originalAmount(request.getOrderAmount())
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .build();
    }

    /**
     * Đánh dấu voucher đã dùng — gọi sau khi order được commit
     */
    @Transactional
    public void markVoucherUsed(String voucherId, String userId, Order order) {
        Voucher voucher = getVoucherOrThrow(voucherId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tăng used_count
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        // Insert voucher_usages
        VoucherUsage usage = VoucherUsage.builder()
                .voucher(voucher)
                .user(user)
                .order(order)
                .usedAt(LocalDateTime.now())
                .build();
        voucherUsageRepository.save(usage);
    }

    /**
     * Tính số tiền discount
     * PERCENT: value% của orderAmount, tối đa maxDiscount nếu có
     * FIXED: giảm thẳng value đồng
     */
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount) {
        BigDecimal discount;

        if (voucher.getType() == VoucherType.PERCENT) {
            discount = orderAmount
                    .multiply(voucher.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (voucher.getMaxDiscount() != null) {
                discount = discount.min(voucher.getMaxDiscount());
            }
        } else {
            discount = voucher.getValue();
        }

        return discount.min(orderAmount);
    }
}