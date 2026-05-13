package com.huusang.demo.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    
    private Long id;
    private String variantName;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
}
