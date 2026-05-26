package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * DTO cập nhật sản phẩm — dùng với @ModelAttribute + multipart/form-data.
 * Tất cả các field đều optional (null = giữ nguyên giá trị cũ).
 *
 * Postman: Body → form-data
 *   productName   (Text, optional) → tên mới
 *   price         (Text, optional) → giá mới
 *   image         (File, optional) → ảnh mới — null = giữ nguyên ảnh cũ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @Size(min = 3, max = 255, message = "Tên sản phẩm phải từ 3-255 ký tự")
    private String productName;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    private Long categoryId;

    // File ảnh mới — null hoặc không gửi = giữ nguyên ảnh cũ
    private MultipartFile image;
}
