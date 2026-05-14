package com.huusang.demo.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyVoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    String code;

    @NotNull(message = "Giá trị đơn hàng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị đơn hàng phải lớn hơn 0")
    BigDecimal orderAmount;

    // shopId để kiểm tra voucher có áp dụng cho shop này không
    Long shopId;
}
