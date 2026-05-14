package com.huusang.demo.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {

    @Size(min = 3, max = 255, message = "Tên sản phẩm phải từ 3-255 ký tự")
    String productName;

    String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    Integer stockQuantity;

    Long categoryId;

    String imageUrl;
}
