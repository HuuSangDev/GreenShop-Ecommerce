package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopResponse {

    Long id;
    String shopName;
    String description;
    String bannerUrl;
    String logoUrl;
    Double rating;
    LocalDateTime createdAt;

    // Thông tin chủ shop
    String ownerId;
    String ownerEmail;
    String ownerFullName;
}
