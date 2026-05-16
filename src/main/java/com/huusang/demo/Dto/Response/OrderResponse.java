package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Enum.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {

    Long orderId;
    OrderStatus status;
    PaymentMethod paymentMethod;

    BigDecimal totalAmount;      // Tổng tiền chưa giảm
    BigDecimal discountAmount;
    BigDecimal finalAmount;      // Số tiền thực trả

    LocalDateTime createdAt;
    int totalShops;              // Số lượng shop tham gia đơn hàng
    int totalItems;              // Tổng số sản phẩm (qty)

    List<ShopOrderResponse> shopOrders;

    /**
     * Chỉ có giá trị khi paymentMethod == SEPAY.
     * Frontend render: <img src='paymentUrl' /> để hiển thị QR thanh toán.
     * Null với COD.
     */
    String paymentUrl;
}
