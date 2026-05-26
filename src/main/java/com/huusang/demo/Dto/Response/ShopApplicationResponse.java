package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.ShopApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopApplicationResponse {

    String id;
    String shopName;
    String description;
    String taxCode;
    String taxAddress;
    String taxFullName;
    ShopApplicationStatus status;
    String rejectReason;
    LocalDateTime submittedAt;
    LocalDateTime resolvedAt;

    // Thông tin người nộp đơn
    String userId;
    String userEmail;
    String userFullName;
}
