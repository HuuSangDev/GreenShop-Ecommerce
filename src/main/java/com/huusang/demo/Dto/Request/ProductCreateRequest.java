package com.huusang.demo.Dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 3, max = 255, message = "Tên sản phẩm phải từ 3-255 ký tự")
    private String productName;
    
    @NotBlank(message = "Mô tả không được để trống")
    private String description;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;
    
    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
    
    private String imageUrl;
    
    @Valid
    private List<ProductVariantRequest> variants;


}
