package com.huusang.demo.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailResponse {
    Long id;
    Long productId;
    String productName;
    String productImage;
    Long shopId;
    String shopName;
    String userId;
    String userName;
    String userAvatar;
    Integer rating;
    String comment;
    List<String> imageUrls;
    boolean verifiedPurchase;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
