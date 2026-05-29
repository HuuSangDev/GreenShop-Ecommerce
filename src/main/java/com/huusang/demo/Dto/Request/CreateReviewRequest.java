package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotNull(message = "Order item ID is required")
    Long orderItemId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    String comment;

    // Danh sách URL ảnh đã upload (upload trước, truyền URL vào đây)
    @Size(max = 5, message = "Maximum 5 images per review")
    @Builder.Default
    List<String> imageUrls = new ArrayList<>();
}
