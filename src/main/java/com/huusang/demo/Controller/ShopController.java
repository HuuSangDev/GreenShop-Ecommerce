package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.*;
import com.huusang.demo.Dto.Response.*;
import com.huusang.demo.Enum.ShopStatus;
import com.huusang.demo.Service.ShopService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopController {

    ShopService shopService;

    // ─────────────────────────────────────────────────────────────────────────
    //  SHOP APPLICATION — Đăng ký mở gian hàng
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /shops/applications
     * Khách hàng nộp đơn đăng ký mở gian hàng (tên, mô tả, thông tin thuế).
     * Quyền: CUSTOMER (bất kỳ user đã đăng nhập)
     */
    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ShopApplicationResponse> applyForShop(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ShopApplicationRequest request) {

        ShopApplicationResponse response = shopService.applyForShop(getEmail(jwt), request);
        return ApiResponse.<ShopApplicationResponse>builder()
                .code(201)
                .message("Đơn đăng ký gian hàng đã được nộp thành công, vui lòng chờ duyệt")
                .result(response)
                .build();
    }

    /**
     * POST /shops/applications/{id}/approve
     * Admin duyệt đơn → tạo Shop + ShopWallet, gán role SELLER.
     * Quyền: ADMIN
     */
    @PostMapping("/applications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShopApplicationResponse> approveShop(@PathVariable String id) {
        ShopApplicationResponse response = shopService.approveShop(id);
        return ApiResponse.<ShopApplicationResponse>builder()
                .message("Đã duyệt đơn và tạo gian hàng thành công")
                .result(response)
                .build();
    }

    /**
     * POST /shops/applications/{id}/reject
     * Admin từ chối đơn kèm lý do.
     * Quyền: ADMIN
     */
    @PostMapping("/applications/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShopApplicationResponse> rejectShop(
            @PathVariable String id,
            @Valid @RequestBody RejectShopRequest request) {

        ShopApplicationResponse response = shopService.rejectShop(id, request);
        return ApiResponse.<ShopApplicationResponse>builder()
                .message("Đã từ chối đơn đăng ký gian hàng")
                .result(response)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SHOP MANAGEMENT — Quản lý gian hàng
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /shops/me
     * Chủ shop lấy thông tin gian hàng của mình.
     * Quyền: SELLER
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ShopResponse> getMyShop(@AuthenticationPrincipal Jwt jwt) {
        ShopResponse response = shopService.getMyShop(getEmail(jwt));
        return ApiResponse.<ShopResponse>builder()
                .message("Thông tin gian hàng của bạn")
                .result(response)
                .build();
    }

    /**
     * PUT /shops/me
     * Cập nhật tên, mô tả, banner, logo gian hàng.
     * Quyền: SELLER
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ShopResponse> updateShop(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateShopRequest request) {

        ShopResponse response = shopService.updateShop(getEmail(jwt), request);
        return ApiResponse.<ShopResponse>builder()
                .message("Cập nhật gian hàng thành công")
                .result(response)
                .build();
    }

    /**
     * GET /shops/{id}
     * Lấy thông tin public của shop (khách xem) — không cần đăng nhập.
     * Quyền: PUBLIC
     */
    @GetMapping("/{id}")
    public ApiResponse<ShopResponse> getShopById(@PathVariable Long id) {
        ShopResponse response = shopService.getShopById(id);
        return ApiResponse.<ShopResponse>builder()
                .message("Thông tin gian hàng")
                .result(response)
                .build();
    }

    /**
     * GET /shops?status=ACTIVE
     * Admin xem tất cả shops, filter theo status (tuỳ chọn).
     * Quyền: ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ShopResponse>> getAllShops(
            @RequestParam(required = false) ShopStatus status) {

        List<ShopResponse> shops = shopService.getAllShops(status);
        return ApiResponse.<List<ShopResponse>>builder()
                .message("Danh sách tất cả gian hàng")
                .result(shops)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  REVENUE & WALLET — Ví & Doanh thu
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /shops/me/wallet
     * Xem số dư ví, tổng doanh thu, tổng đã rút.
     * Quyền: SELLER
     */
    @GetMapping("/me/wallet")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ShopWalletResponse> getWallet(@AuthenticationPrincipal Jwt jwt) {
        ShopWalletResponse wallet = shopService.getWallet(getEmail(jwt));
        return ApiResponse.<ShopWalletResponse>builder()
                .message("Thông tin ví của gian hàng")
                .result(wallet)
                .build();
    }

    /**
     * GET /shops/me/revenue?period=DAILY|WEEKLY|MONTHLY
     * Thống kê doanh thu theo ngày/tuần/tháng, top sản phẩm bán chạy.
     * Quyền: SELLER
     */
    @GetMapping("/me/revenue")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<RevenueStatsResponse> getRevenueStats(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "DAILY") String period) {

        RevenueStatsResponse stats = shopService.getRevenueStats(getEmail(jwt), period);
        return ApiResponse.<RevenueStatsResponse>builder()
                .message("Thống kê doanh thu gian hàng")
                .result(stats)
                .build();
    }

    /**
     * POST /shops/me/withdrawals
     * Tạo yêu cầu rút tiền, kiểm tra balance đủ không.
     * Quyền: SELLER
     */
    @PostMapping("/me/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<WithdrawalResponse> requestWithdrawal(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WithdrawalRequest request) {

        WithdrawalResponse response = shopService.requestWithdrawal(getEmail(jwt), request);
        return ApiResponse.<WithdrawalResponse>builder()
                .code(201)
                .message("Yêu cầu rút tiền đã được tạo, vui lòng chờ Admin duyệt")
                .result(response)
                .build();
    }

    /**
     * GET /shops/me/withdrawals
     * Lịch sử các lần rút tiền và trạng thái.
     * Quyền: SELLER
     */
    @GetMapping("/me/withdrawals")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<List<WithdrawalResponse>> getWithdrawalHistory(
            @AuthenticationPrincipal Jwt jwt) {

        List<WithdrawalResponse> history = shopService.getWithdrawalHistory(getEmail(jwt));
        return ApiResponse.<List<WithdrawalResponse>>builder()
                .message("Lịch sử rút tiền")
                .result(history)
                .build();
    }

    /**
     * PUT /shops/withdrawals/{id}/process
     * Admin duyệt hoặc từ chối yêu cầu rút tiền.
     * Quyền: ADMIN
     */
    @PutMapping("/withdrawals/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WithdrawalResponse> processWithdrawal(
            @PathVariable String id,
            @Valid @RequestBody ProcessWithdrawalRequest request) {

        WithdrawalResponse response = shopService.processWithdrawal(id, request);
        String msg = request.getStatus().name().equals("APPROVED")
                ? "Đã duyệt yêu cầu rút tiền"
                : "Đã từ chối yêu cầu rút tiền";
        return ApiResponse.<WithdrawalResponse>builder()
                .message(msg)
                .result(response)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private String getEmail(Jwt jwt) {
        return jwt.getSubject(); // JWT subject là email (xem AuthService.generateToken)
    }
}
