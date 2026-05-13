package com.huusang.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {
    String  cartItemId;

    Long    productId;
    String  productName;
    String  productImageUrl;

    Long    variantId;
    String  variantName;
    String  sku;

    Integer quantity;

    BigDecimal priceSnapshot;   // Giá lúc thêm vào giỏ
    BigDecimal currentPrice;    // Giá hiện tại của variant
    Boolean    priceChanged;    // true nếu giá đã thay đổi so với lúc thêm

    Integer availableStock;
    Boolean inStock;

    BigDecimal subtotal;        // priceSnapshot * quantity

    LocalDateTime addedAt;
}
