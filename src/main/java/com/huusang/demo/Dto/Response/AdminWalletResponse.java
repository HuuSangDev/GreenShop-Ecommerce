package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminWalletResponse {

    String id;
    BigDecimal balance;
    BigDecimal totalEarned;
    BigDecimal totalWithdrawn;
    
    long totalDepositCount;
    long totalWithdrawCount;
}
