package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO dành cho Admin xem danh sách đơn hàng toàn hệ thống.
 * Cung cấp nhiều thông tin hơn OrderResponse thông thường (thêm buyer info, shop info).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminOrderResponse {

    Long orderId;
    OrderStatus status;
    String paymentMethod;

    // --- Financial ---
    BigDecimal totalAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;

    // --- Buyer ---
    String buyerId;
    String buyerEmail;
    String buyerFullName;

    // --- Shops summary ---
    int totalShops;
    int totalItems;
    List<ShopSummary> shops;

    LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ShopSummary {
        Long shopOrderId;
        Long shopId;
        String shopName;
        OrderStatus shopOrderStatus;
        BigDecimal shopTotalAmount;
    }
}
