package com.huusang.demo.Mapper;

import com.huusang.demo.Dto.Response.ProductResponse;
import com.huusang.demo.Dto.Response.ProductVariantResponse;
import com.huusang.demo.Entity.Product;
import com.huusang.demo.Entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .available(product.isAvailable())
                .createdAt(product.getCreatedAt())
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getShopName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .variants(product.getVariants() != null ? 
                         product.getVariants().stream()
                                 .map(this::toVariantResponse)
                                 .collect(Collectors.toList()) : null)
                .build();
    }

    public ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .variantName(variant.getVariantName())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .sku(variant.getSku())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
