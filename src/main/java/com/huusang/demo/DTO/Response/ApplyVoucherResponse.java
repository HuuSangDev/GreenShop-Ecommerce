package com.huusang.demo.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyVoucherResponse {

    String voucherId;
    String voucherCode;
    BigDecimal originalAmount;  // Giá trị đơn hàng gốc
    BigDecimal discountAmount;  // Số tiền được giảm
    BigDecimal finalAmount;     // Số tiền thực trả
}
