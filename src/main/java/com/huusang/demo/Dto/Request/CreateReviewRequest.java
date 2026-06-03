package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {
    @NotNull(message = "OrderItem ID không được để trống")
    Long orderItemId;

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải từ 1 đến 5")
    @Max(value = 5, message = "Rating phải từ 1 đến 5")
    Integer rating;

    @Size(max = 1000, message = "Bình luận không được vượt quá 1000 ký tự")
    String comment;

    // Optional: Review images (max 5 images, each <= 5MB)
    List<MultipartFile> images;
}

