package com.huusang.demo.Dto.Request;

import com.huusang.demo.Enum.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutRequest {

    @NotEmpty(message = "Danh sách sản phẩm không được trống")
    List<String> cartItemIds;

    @NotNull(message = "Phương thức thanh toán không được trống")
    PaymentMethod paymentMethod;

    // ─── Địa chỉ giao hàng (bắt buộc để tính phí ship qua GHN) ─────────────
    /** ID quận/huyện nhà khách hàng theo mã GHN — ví dụ: 1820 */
    @NotNull(message = "Quận/huyện giao hàng không được trống")
    Integer toDistrictId;

    /** Mã phường/xã nhà khách hàng theo mã GHN — ví dụ: "030712" */
    @NotNull(message = "Phường/xã giao hàng không được trống")
    String toWardCode;

    // ─── Voucher (optional) ───────────────────────────────────────────────────
    /** Mã voucher muốn áp dụng — null = không dùng voucher */
    String voucherCode;
}
