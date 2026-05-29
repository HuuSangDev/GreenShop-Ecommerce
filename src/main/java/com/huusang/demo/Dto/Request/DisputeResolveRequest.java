package com.huusang.demo.Dto.Request;

import com.huusang.demo.Enum.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResolveRequest {

    /**
     * Phán quyết của Admin:
     *  - RESOLVED_BUYER_WIN  → hoàn tiền cho người mua (trừ ShopWallet.balance)
     *  - RESOLVED_SELLER_WIN → người bán thắng (không thay đổi tài chính)
     */
    @NotNull(message = "Phán quyết (verdict) không được để trống")
    DisputeStatus verdict;

    /** Ghi chú / lý do phán quyết của Admin (tuỳ chọn) */
    String adminNote;
}
