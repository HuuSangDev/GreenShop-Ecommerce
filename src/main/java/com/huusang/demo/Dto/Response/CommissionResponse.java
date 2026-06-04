package com.huusang.demo.Dto.Response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommissionResponse {
    String id;
    Long shopOrderId;
    Long shopId;
    String shopName;
    BigDecimal grossAmount;
    BigDecimal commissionRate;
    BigDecimal commissionAmt;
    BigDecimal netAmount;
    LocalDateTime createdAt;
}
