package com.huusang.demo.DTO.Response;

import com.huusang.demo.Enum.VoucherType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherResponse {

    String id;
    String code;
    VoucherType type;
    BigDecimal value;
    BigDecimal maxDiscount;
    BigDecimal minOrderAmt;
    Integer maxUsage;
    Integer usedCount;
    Integer remainingUsage; // null nếu unlimited
    LocalDateTime startsAt;
    LocalDateTime expiresAt;
    boolean active;

    // null nếu là voucher toàn sàn
    Long shopId;
    String shopName;
}
