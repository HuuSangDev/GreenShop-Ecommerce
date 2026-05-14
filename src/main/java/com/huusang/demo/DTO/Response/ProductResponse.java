package com.huusang.demo.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    Long id;
    String productName;
    String description;
    BigDecimal price;
    Integer stockQuantity;
    String imageUrl;
    boolean available;
    LocalDateTime createdAt;

    Long shopId;
    String shopName;

    Long categoryId;
    String categoryName;

    List<ProductVariantResponse> variants;
}
