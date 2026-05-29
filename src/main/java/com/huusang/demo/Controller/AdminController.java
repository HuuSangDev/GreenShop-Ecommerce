package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.DisputeCreateRequest;
import com.huusang.demo.Dto.Request.DisputeResolveRequest;
import com.huusang.demo.Dto.Response.*;
import com.huusang.demo.Enum.DisputeStatus;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Service.AdminService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminController {

    AdminService adminService;

    // ═══════════════════════════════════════════════════════════════════════════
    // DISPUTE — Khiếu nại
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * POST /admin/disputes
     * BUYER: Tạo khiếu nại cho một ShopOrder.
     * Quyền: isAuthenticated() — bất kỳ user đã đăng nhập
     */
    @PostMapping("/disputes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DisputeResponse> createDispute(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DisputeCreateRequest request) {

        DisputeResponse response = adminService.createDispute(getEmail(jwt), request);
        return ApiResponse.<DisputeResponse>builder()
                .code(201)
                .message("Khiếu nại đã được gửi thành công, Admin sẽ xem xét trong thời gian sớm nhất")
                .result(response)
                .build();
    }

    /**
     * GET /admin/disputes?status=OPEN&page=0&size=20
     * ADMIN: Lấy danh sách tất cả disputes, tuỳ chọn lọc theo trạng thái.
     * status = null → trả về tất cả.
     * Quyền: ADMIN
     */
    @GetMapping("/disputes")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<DisputeResponse>> getDisputes(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<DisputeResponse> disputes = adminService.getDisputes(status, page, size);
        return ApiResponse.<Page<DisputeResponse>>builder()
                .message("Danh sách khiếu nại")
                .result(disputes)
                .build();
    }

    /**
     * POST /admin/disputes/{id}/resolve
     * ADMIN: Đưa ra phán quyết cho một khiếu nại.
     *   - RESOLVED_BUYER_WIN  → Trừ ShopWallet.balance (hoàn tiền ngầm định).
     *   - RESOLVED_SELLER_WIN → Không thay đổi tài chính.
     * Quyền: ADMIN
     */
    @PostMapping("/disputes/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DisputeResponse> resolveDispute(
            @PathVariable String id,
            @Valid @RequestBody DisputeResolveRequest request) {

        DisputeResponse response = adminService.resolveDispute(id, request);
        String msg = switch (request.getVerdict()) {
            case RESOLVED_BUYER_WIN  -> "Đã phán quyết: Người mua thắng — đã trừ số dư ví của shop";
            case RESOLVED_SELLER_WIN -> "Đã phán quyết: Người bán thắng — đơn khiếu nại đã đóng";
            default -> "Đã giải quyết khiếu nại";
        };

        return ApiResponse.<DisputeResponse>builder()
                .message(msg)
                .result(response)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ORDERS — Quản lý đơn hàng toàn hệ thống
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/orders?status=PENDING&keyword=nguyen&page=0&size=20
     * ADMIN: Xem, lọc và tìm kiếm toàn bộ đơn hàng trên hệ thống.
     *   - status: lọc theo trạng thái (null = tất cả)
     *   - keyword: tìm theo email hoặc tên người mua
     * Quyền: ADMIN
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<AdminOrderResponse>> getAdminOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminOrderResponse> orders = adminService.getAdminOrders(status, keyword, page, size);
        return ApiResponse.<Page<AdminOrderResponse>>builder()
                .message("Danh sách đơn hàng hệ thống")
                .result(orders)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANALYTICS — Dashboard & Revenue Chart
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /admin/analytics/overview
     * ADMIN: Các con số tổng kết nhanh cho trang chủ Admin:
     *   - Tổng GMV (Gross Merchandise Value)
     *   - Số đơn hàng mới hôm nay
     *   - Số shop mới đăng ký hôm nay
     *   - Số khiếu nại đang mở
     *   - Số yêu cầu rút tiền chờ duyệt
     * Quyền: ADMIN
     */
    @GetMapping("/analytics/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminOverviewResponse> getOverview() {
        AdminOverviewResponse overview = adminService.getOverview();
        return ApiResponse.<AdminOverviewResponse>builder()
                .message("Tổng quan hệ thống")
                .result(overview)
                .build();
    }

    /**
     * GET /admin/analytics/revenue-chart?period=DAILY
     * ADMIN: Dữ liệu biểu đồ doanh thu để vẽ Line Chart.
     *   - period = DAILY   → 30 ngày gần nhất
     *   - period = MONTHLY → 12 tháng gần nhất
     *   - period = YEARLY  → 5 năm gần nhất
     * Quyền: ADMIN
     */
    @GetMapping("/analytics/revenue-chart")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RevenueChartResponse> getRevenueChart(
            @RequestParam(defaultValue = "DAILY") String period) {

        RevenueChartResponse chart = adminService.getRevenueChart(period);
        return ApiResponse.<RevenueChartResponse>builder()
                .message("Dữ liệu biểu đồ doanh thu — " + period)
                .result(chart)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // USER MANAGEMENT — Ban / Unban
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * PATCH /admin/users/{id}/ban
     * ADMIN: Khóa tài khoản người dùng (active = false).
     * Sau khi ban, SecurityConfig sẽ từ chối JWT của user này ngay lập tức.
     * Quyền: ADMIN
     */
    @PatchMapping("/users/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> banUser(@PathVariable String id) {
        UserResponse response = adminService.banUser(id);
        return ApiResponse.<UserResponse>builder()
                .message("Đã khóa tài khoản người dùng thành công")
                .result(response)
                .build();
    }

    /**
     * PATCH /admin/users/{id}/unban
     * ADMIN: Kích hoạt lại tài khoản (active = true).
     * Quyền: ADMIN
     */
    @PatchMapping("/users/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> unbanUser(@PathVariable String id) {
        UserResponse response = adminService.unbanUser(id);
        return ApiResponse.<UserResponse>builder()
                .message("Đã kích hoạt lại tài khoản người dùng thành công")
                .result(response)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private String getEmail(Jwt jwt) {
        return jwt.getSubject();
    }
}
