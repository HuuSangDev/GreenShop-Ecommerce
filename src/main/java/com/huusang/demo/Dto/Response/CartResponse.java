package com.huusang.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {
    String cartId;
    List<CartItemResponse> items;
    BigDecimal totalAmount;  // Tổng tiền theo priceSnapshot
    Integer totalItems;      // Tổng số lượng sản phẩm
    Integer totalDistinctItems; // Số loại sản phẩm khác nhau
}
