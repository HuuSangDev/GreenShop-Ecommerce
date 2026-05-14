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
public class CartValidationResponse {

    Boolean isValid;
    List<CartItemWarning> warnings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CartItemWarning {
        String cartItemId;
        String productName;
        String variantName;

        // PRICE_CHANGED | OUT_OF_STOCK | INSUFFICIENT_STOCK | PRODUCT_UNAVAILABLE
        String warningType;
        String message;

        BigDecimal oldPrice;
        BigDecimal newPrice;

        Integer requestedQty;
        Integer availableQty;
    }
}
