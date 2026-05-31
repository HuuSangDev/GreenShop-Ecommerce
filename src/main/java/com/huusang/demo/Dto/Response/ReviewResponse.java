package com.huusang.demo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    Long id;
    Long productId;
    String productName;
    String productImage;
    String userId;
    String userName;
    String userAvatar;
    Integer rating;
    String comment;
    boolean verifiedPurchase;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
