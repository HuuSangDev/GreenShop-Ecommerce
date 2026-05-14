package com.huusang.demo.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị voucher phải lớn hơn 0")
    BigDecimal value;

    BigDecimal maxDiscount;

    @DecimalMin(value = "0.0", message = "Giá trị đơn tối thiểu không được âm")
    BigDecimal minOrderAmt;

    @Min(value = 1, message = "Số lượt dùng tối thiểu là 1")
    Integer maxUsage;

    LocalDateTime startsAt;

    LocalDateTime expiresAt;
}
