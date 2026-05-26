package com.huusang.demo.Dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tạo sản phẩm — dùng với @ModelAttribute + multipart/form-data.
 * Spring Boot tự map các field text và file vào đây.
 *
 * Postman: Body → form-data
 *   productName   (Text)  → "Tai nghe Sony"
 *   description   (Text)  → "Mô tả sản phẩm"
 *   price         (Text)  → 7990000
 *   stockQuantity (Text)  → 100
 *   categoryId    (Text)  → 1
 *   image         (File)  → [chọn file ảnh, optional]
 */
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

    // File ảnh sản phẩm — Spring tự inject từ form-data khi dùng @ModelAttribute
    // Optional: null = không upload ảnh
    private MultipartFile image;

    @Valid
    private List<ProductVariantRequest> variants;
}
