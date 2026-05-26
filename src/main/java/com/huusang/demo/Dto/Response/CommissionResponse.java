package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommissionResponse {

    String id;
    Long shopOrderId;
    Long shopId;
    String shopName;
    BigDecimal grossAmount;        // Doanh thu gốc
    BigDecimal commissionRate;     // % hoa hồng (VD: 2.00)
    BigDecimal commissionAmt;      // Tiền sàn thu
    BigDecimal netAmount;          // Tiền shop nhận
}
