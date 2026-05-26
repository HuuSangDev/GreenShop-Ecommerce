package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Response.CommissionResponse;
import com.huusang.demo.Dto.Response.CommissionStatsResponse;
import com.huusang.demo.Service.CommissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommissionController {

    CommissionService commissionService;

    // ─────────────────────────────────────────────────────────────────────────
    //  SELLER/ADMIN: Xem chi tiết hoa hồng của 1 shop_order
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /commissions/shop-order/{shopOrderId}
     * Xem chi tiết hoa hồng của 1 shop_order cụ thể.
     * Quyền: SELLER (chỉ xem shop của mình) hoặc ADMIN (xem tất cả)
     */
    @GetMapping("/shop-order/{shopOrderId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<CommissionResponse> getCommissionByShopOrder(
            @PathVariable Long shopOrderId,
            @AuthenticationPrincipal Jwt jwt) {

        CommissionResponse response = commissionService.getCommissionByShopOrder(
                shopOrderId, getEmail(jwt));

        return ApiResponse.<CommissionResponse>builder()
                .message("Chi tiết hoa hồng của shop order")
                .result(response)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ADMIN: Xem tổng hoa hồng sàn đã thu
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /commissions/stats?period=DAILY|MONTHLY
     * Admin xem tổng hoa hồng sàn đã thu, theo ngày/tháng.
     * Quyền: ADMIN
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CommissionStatsResponse> getCommissionStats(
            @RequestParam(required = false) String period) {

        CommissionStatsResponse stats = commissionService.getCommissionStats(period);

        return ApiResponse.<CommissionStatsResponse>builder()
                .message("Thống kê hoa hồng sàn")
                .result(stats)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SELLER: Xem lịch sử hoa hồng bị trừ
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /commissions/my-shop
     * Shop xem lịch sử hoa hồng bị trừ trên từng đơn của mình.
     * Quyền: SELLER
     */
    @GetMapping("/my-shop")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<List<CommissionResponse>> getShopCommissions(
            @AuthenticationPrincipal Jwt jwt) {

        List<CommissionResponse> commissions = commissionService.getShopCommissions(getEmail(jwt));

        return ApiResponse.<List<CommissionResponse>>builder()
                .message("Lịch sử hoa hồng của shop")
                .result(commissions)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private String getEmail(Jwt jwt) {
        return jwt.getSubject();
    }
}
