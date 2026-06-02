package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.WithdrawalRequest;
import com.huusang.demo.Dto.Response.*;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Service.AdminService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    AdminService adminService;

    // ═══════════════════════════════════════════════════════════════════════════
    // ANALYTICS & DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/analytics/overview")
    public ApiResponse<Map<String, Object>> getOverview() {
        log.info("Admin: Getting system overview");
        return ApiResponse.<Map<String, Object>>builder()
                .result(adminService.getSystemOverview())
                .message("Lấy tổng quan hệ thống thành công")
                .build();
    }

    @GetMapping("/analytics/revenue-chart")
    public ApiResponse<List<Map<String, Object>>> getRevenueChart(
            @RequestParam(defaultValue = "DAILY") String period) {
        log.info("Admin: Getting revenue chart for period: {}", period);
        return ApiResponse.<List<Map<String, Object>>>builder()
                .result(adminService.getRevenueChart(period))
                .message("Lấy biểu đồ doanh thu thành công")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // USER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @PatchMapping("/users/{id}/ban")
    public ApiResponse<String> banUser(@PathVariable String id) {
        log.info("Admin: Banning user {}", id);
        adminService.banUser(id);
        return ApiResponse.<String>builder()
                .message("Khóa tài khoản thành công")
                .build();
    }

    @PatchMapping("/users/{id}/unban")
    public ApiResponse<String> unbanUser(@PathVariable String id) {
        log.info("Admin: Unbanning user {}", id);
        adminService.unbanUser(id);
        return ApiResponse.<String>builder()
                .message("Mở khóa tài khoản thành công")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRODUCT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @PatchMapping("/products/{id}/hide")
    public ApiResponse<String> hideProduct(@PathVariable Long id) {
        log.info("Admin: Hiding product {}", id);
        adminService.hideProduct(id);
        return ApiResponse.<String>builder()
                .message("Ẩn sản phẩm thành công")
                .build();
    }

    @PatchMapping("/products/{id}/unhide")
    public ApiResponse<String> unhideProduct(@PathVariable Long id) {
        log.info("Admin: Unhiding product {}", id);
        adminService.unhideProduct(id);
        return ApiResponse.<String>builder()
                .message("Hiện sản phẩm thành công")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ORDER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/orders")
    public ApiResponse<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin: Getting orders - status: {}, page: {}, size: {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(adminService.getAllOrders(status, pageable))
                .message("Lấy danh sách đơn hàng thành công")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DISPUTE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/disputes")
    public ApiResponse<Page<Map<String, Object>>> getDisputes(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Admin: Getting disputes - status: {}, page: {}, size: {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<Map<String, Object>>>builder()
                .result(adminService.getDisputes(status, pageable))
                .message("Lấy danh sách khiếu nại thành công")
                .build();
    }

    @GetMapping("/disputes/{id}")
    public ApiResponse<Map<String, Object>> getDisputeDetail(@PathVariable Long id) {
        log.info("Admin: Getting dispute detail {}", id);
        return ApiResponse.<Map<String, Object>>builder()
                .result(adminService.getDisputeDetail(id))
                .message("Lấy chi tiết khiếu nại thành công")
                .build();
    }

    @PostMapping("/disputes/{id}/resolve")
    public ApiResponse<String> resolveDispute(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("Admin: Resolving dispute {} with verdict: {}", id, request.get("verdict"));
        adminService.resolveDispute(id, request.get("verdict"), request.get("adminNote"));
        return ApiResponse.<String>builder()
                .message("Giải quyết khiếu nại thành công")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WITHDRAWALS MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/withdrawals")
    public ApiResponse<Page<Map<String, Object>>> getSellerWithdrawals(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Admin: Getting seller withdrawals - status: {}, page: {}, size: {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        var withdrawals = adminService.getSellerWithdrawals(pageable);
        
        // Convert WithdrawalResponse to Map<String, Object>
        Page<Map<String, Object>> result = withdrawals.map(wr -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", wr.getId());
            map.put("shopId", wr.getShopId());
            map.put("shopName", wr.getShopName());
            map.put("amount", wr.getAmount());
            map.put("status", wr.getStatus());
            map.put("requestedAt", wr.getRequestedAt());
            map.put("resolvedAt", wr.getResolvedAt());
            return map;
        });
        
        return ApiResponse.<Page<Map<String, Object>>>builder()
                .result(result)
                .message("Lấy danh sách rút tiền từ các seller thành công")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WALLET MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/wallet")
    public ApiResponse<AdminWalletResponse> getAdminWallet() {
        log.info("Admin: Getting admin wallet info");
        return ApiResponse.<AdminWalletResponse>builder()
                .result(adminService.getAdminWallet())
                .message("Lấy thông tin ví admin thành công")
                .build();
    }

    @PostMapping("/wallet/withdraw")
    public ApiResponse<AdminWalletResponse> withdrawFromAdminWallet(
            @Valid @RequestBody WithdrawalRequest request) {
        log.info("Admin: Withdrawing {} from admin wallet", request.getAmount());
        AdminWalletResponse response = adminService.withdrawFromAdminWallet(request);
        return ApiResponse.<AdminWalletResponse>builder()
                .result(response)
                .message("Rút tiền thành công")
                .build();
    }

    @GetMapping("/wallets/shops")
    public ApiResponse<List<ShopWalletResponse>> getAllShopWallets() {
        log.info("Admin: Getting all shop wallets");
        return ApiResponse.<List<ShopWalletResponse>>builder()
                .result(adminService.getAllShopWallets())
                .message("Lấy danh sách ví shop thành công")
                .build();
    }
}

