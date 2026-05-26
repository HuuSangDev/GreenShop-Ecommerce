package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WithdrawalRequest {

    @NotNull(message = "Số tiền rút không được để trống")
    @DecimalMin(value = "10000", message = "Số tiền rút tối thiểu là 10,000 VND")
    BigDecimal amount;

    String bankAccountNumber;

    String bankName;

    String accountHolderName;
}
