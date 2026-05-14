package com.huusang.demo.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFilterRequest {

    Long categoryId;
    Long shopId;
    BigDecimal minPrice;
    BigDecimal maxPrice;

    Integer page = 0;
    Integer size = 10;
    String sortBy = "createdAt";
    String sortDirection = "DESC";
}
