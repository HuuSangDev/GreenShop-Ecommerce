package com.huusang.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    Long   id;

    // Product info
    Long   productId;
    String productName;

    // Reviewer info (ẩn email/phone)
    String userId;
    String reviewerName;  // fullName hoặc username

    // Order item info
    Long   orderItemId;
    String variantName;   // Tên variant đã mua

    // Review content
    Integer rating;
    String  comment;
    List<String> imageUrls;

    // Seller reply
    String        sellerReply;
    LocalDateTime sellerRepliedAt;
    String        shopName;

    // Timestamps
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Chỉ hiển thị khi admin xem — ẩn với public
    Boolean isDeleted;
    String  deleteReason;
    LocalDateTime deletedAt;
}
