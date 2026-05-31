package com.huusang.demo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingReviewResponse {
    Long orderItemId;
    Long orderId;
    Long productId;
    String productName;
    String productImage;
    String variantName;
    Long shopId;
    String shopName;
    Integer quantity;
    BigDecimal priceAtBuy;
    LocalDateTime deliveredAt;
}
