package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.CheckoutPreviewRequest;
import com.huusang.demo.Dto.Request.CheckoutRequest;
import com.huusang.demo.Dto.Request.SePayWebhookRequest;
import com.huusang.demo.Dto.Response.CheckoutPreviewResponse;
import com.huusang.demo.Dto.Response.OrderResponse;
import com.huusang.demo.Service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderService orderService;

    // ─── CHECKOUT PREVIEW ────────────────────────────────────────────────────────
    /**
     * POST /api/v1/checkouts/preview
     * Tính toán và hiển thị thông tin đơn hàng TRƯỚC KHI user xác nhận.
     * KHÔNG tạo order, KHÔNG trừ stock, KHÔNG tạo payment.
     *
     * Request: { "cartItemIds": ["id1", "id2"] }
     * Response: subtotal, shippingFee, finalAmount, items, paymentMethods
     */
    @PostMapping("/api/v1/checkouts/preview")
    public ApiResponse<CheckoutPreviewResponse> previewCheckout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutPreviewRequest request) {

        CheckoutPreviewResponse preview = orderService.previewCheckout(getUserEmail(jwt), request);

        return ApiResponse.<CheckoutPreviewResponse>builder()
                .code(200)
                .message("Thông tin đơn hàng")
                .result(preview)
                .build();
    }

    // ─── CHECKOUT (COD / SEPAY) ───────────────────────────────────────────────────
    /**
     * POST /api/v1/orders
     * Đặt hàng Multi-Vendor.
     * <p>
     * COD  → tạo order, trừ stock ngay, xóa giỏ → return OrderResponse
     * SEPAY → tạo order PENDING_PAYMENT, generate QR → return OrderResponse với paymentUrl
     *
     * Request: { "cartItemIds": [...], "paymentMethod": "COD" | "SEPAY" }
     */
    @PostMapping("/api/v1/orders")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponse> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request) {

        OrderResponse order = orderService.processMultiVendorCheckout(getUserEmail(jwt), request);

        String message = switch (request.getPaymentMethod()) {
            case COD   -> "Đặt hàng thành công";
            case SEPAY -> "Đơn hàng đã tạo — vui lòng quét QR để thanh toán";
        };

        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message(message)
                .result(order)
                .build();
    }

    // ─── SEPAY WEBHOOK ───────────────────────────────────────────────────────────
    /**
     * POST /api/v1/payments/sepay/webhook
     * Endpoint PUBLIC — SePay server gọi khi giao dịch thành công.
     * KHÔNG cần JWT (SePay không có JWT của user).
     * <p>
     * Flow: verify transactionRef → lock stock → deduct stock → PAID → clear cart
     * <p>
     * SePay expects HTTP 200 OK. Nếu 4xx/5xx, SePay sẽ retry.
     * Ta dùng idempotency để an toàn khi retry.
     */
    @PostMapping("/api/v1/payments/sepay/webhook")
    public ResponseEntity<Void> sePayWebhook(@RequestBody SePayWebhookRequest request) {
        log.info("SePay webhook hit: content='{}', amount={}", request.getContent(), request.getTransferAmount());
        orderService.handleSePayCallback(request);
        return ResponseEntity.ok().build();
    }

    // ─── GET ORDER BY ID ─────────────────────────────────────────────────────────
    /**
     * GET /api/v1/orders/{orderId}
     * Lấy thông tin đơn hàng theo ID — cần JWT.
     * Frontend dùng để polling trạng thái SePay: PENDING_PAYMENT → PAID
     */
    @GetMapping("/api/v1/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {

        OrderResponse order = orderService.getOrderById(getUserEmail(jwt), orderId);

        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Thông tin đơn hàng")
                .result(order)
                .build();
    }

    // ─── GET MY ORDERS ────────────────────────────────────────────────────────────
    /**
     * GET /api/v1/orders?status=PENDING
     * Lấy danh sách đơn hàng của user đang đăng nhập.
     * status là optional — không truyền thì lấy tất cả.
     */
    @GetMapping("/api/v1/orders")
    public ApiResponse<java.util.List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) com.huusang.demo.Enum.OrderStatus status) {

        return ApiResponse.<java.util.List<OrderResponse>>builder()
                .code(200)
                .message("Danh sách đơn hàng")
                .result(orderService.getMyOrders(getUserEmail(jwt), status))
                .build();
    }

    // ─── GET ORDER DETAIL ─────────────────────────────────────────────────────────
    /**
     * GET /api/v1/orders/{orderId}/detail
     * Lấy chi tiết đầy đủ một đơn hàng: items, shop, payment, totals.
     */
    @GetMapping("/api/v1/orders/{orderId}/detail")
    public ApiResponse<OrderResponse> getOrderDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {

        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Chi tiết đơn hàng")
                .result(orderService.getOrderDetail(getUserEmail(jwt), orderId))
                .build();
    }

    // ─── CANCEL ORDER ─────────────────────────────────────────────────────────────
    /**
     * PUT /api/v1/orders/{orderId}/cancel
     * Buyer hủy đơn hàng của mình.
     * Chỉ cho phép hủy khi: PENDING, PAID, PENDING_PAYMENT
     */
    @PutMapping("/api/v1/orders/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {

        OrderResponse order = orderService.cancelOrder(getUserEmail(jwt), orderId);

        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Đơn hàng đã được hủy thành công")
                .result(order)
                .build();
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────────
    // JWT subject là email — giống convention CartController
    private String getUserEmail(Jwt jwt) {
        return jwt.getSubject();
    }
}
