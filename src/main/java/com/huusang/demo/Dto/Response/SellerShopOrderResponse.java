package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response cho Seller xem đơn hàng của shop mình.
 * shopTotalAmount = tiền hàng SAU khi đã trừ phần voucher discount phân bổ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerShopOrderResponse {

    // ── ShopOrder info ────────────────────────────────────────────────────────
    Long shopOrderId;
    OrderStatus status;
    BigDecimal shopTotalAmount;   // Tiền hàng net (sau khi trừ discount phân bổ)
    BigDecimal shippingFee;
    List<OrderItemResponse> items;

    // ── Parent Order info ─────────────────────────────────────────────────────
    Long orderId;
    String paymentMethod;         // COD | SEPAY
    LocalDateTime createdAt;
    BigDecimal discountAmount;    // Phần voucher được phân bổ cho shop này

    // ── Buyer info ────────────────────────────────────────────────────────────
    String buyerName;
    String buyerEmail;

    // ── Settle info (chỉ có sau khi DELIVERED) ────────────────────────────────
    BigDecimal commissionAmt;     // 2% sàn thu
    BigDecimal netEarned;         // Số tiền thực tế shop nhận vào ví
}
