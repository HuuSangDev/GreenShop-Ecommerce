package com.huusang.demo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private boolean available;
    private LocalDateTime createdAt;
    
    private Long shopId;
    private String shopName;
    
    private Long categoryId;
    private String categoryName;
    
    private List<ProductVariantResponse> variants;

}
