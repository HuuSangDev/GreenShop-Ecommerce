package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

    Long id;
    Long orderId;
    String transactionId;
    String transactionRef;
    String method;
    String status;
    BigDecimal amount;
    BigDecimal shopAmount;
    LocalDateTime createdAt;
    LocalDateTime paidAt;
}
