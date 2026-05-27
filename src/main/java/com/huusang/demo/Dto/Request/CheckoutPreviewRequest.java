package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request cho API Preview — tính toán phí ship thật + giảm giá.
 * Không tạo order, không trừ stock.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutPreviewRequest {

    @NotEmpty(message = "Danh sách sản phẩm không được trống")
    List<String> cartItemIds;

    // ─── Địa chỉ giao hàng (để tính phí ship qua GHN) ────────────────────────
    /** ID quận/huyện nhà khách hàng theo mã GHN — ví dụ: 1820 */
    Integer toDistrictId;

    /** Mã phường/xã nhà khách hàng theo mã GHN — ví dụ: "030712" */
    String toWardCode;

    // ─── Voucher (optional) ───────────────────────────────────────────────────
    /** Mã voucher muốn áp dụng — null = không dùng voucher */
    String voucherCode;
}
