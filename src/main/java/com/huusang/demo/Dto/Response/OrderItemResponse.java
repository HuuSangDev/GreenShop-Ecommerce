package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {

    Long orderItemId;
    Long variantId;
    String variantName;
    String sku;
    String productName;
    String productImageUrl;
    Integer quantity;
    BigDecimal priceAtBuy;   // Giá chốt tại thời điểm mua
    BigDecimal subtotal;     // priceAtBuy * quantity
    boolean reviewed;        // true nếu user đã đánh giá item này
    Long productId;          // Để navigate đến trang sản phẩm
}
