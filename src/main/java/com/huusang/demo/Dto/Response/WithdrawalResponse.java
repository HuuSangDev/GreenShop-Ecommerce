package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.WithdrawalStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WithdrawalResponse {

    String id;
    BigDecimal amount;
    WithdrawalStatus status;
    LocalDateTime requestedAt;
    LocalDateTime resolvedAt;
    Long shopId;
    String shopName;
}
