package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.DisputeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResponse {

    String id;

    // --- ShopOrder info ---
    Long shopOrderId;
    String shopName;
    Long shopId;

    // --- Buyer info ---
    String buyerId;
    String buyerEmail;
    String buyerFullName;

    // --- Dispute details ---
    String reason;
    DisputeStatus status;
    String adminNote;

    // --- Financial impact ---
    /** Số tiền bị ảnh hưởng (shopTotalAmount của ShopOrder) */
    java.math.BigDecimal shopTotalAmount;

    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
}
