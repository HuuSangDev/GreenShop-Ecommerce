package com.huusang.demo.Dto.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateShopRequest {

    String shopName;

    String description;

    String bannerUrl;

    String logoUrl;
}
