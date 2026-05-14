package com.huusang.demo.DTO.Request;

import com.huusang.demo.Enum.VoucherType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherCreateRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(min = 3, max = 50, message = "Mã voucher phải từ 3-50 ký tự")
    String code;

    @NotNull(message = "Loại voucher không được để trống")
    VoucherType type; // PERCENT | FIXED

    @NotNull(message = "Giá trị voucher không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị voucher phải lớn hơn 0")
    BigDecimal value;

    // Chỉ dùng khi type = PERCENT, giới hạn số tiền giảm tối đa
    BigDecimal maxDiscount;

    @DecimalMin(value = "0.0", message = "Giá trị đơn tối thiểu không được âm")
    BigDecimal minOrderAmt;

    @Min(value = 1, message = "Số lượt dùng tối thiểu là 1")
    Integer maxUsage; // null = unlimited

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    LocalDateTime startsAt;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    LocalDateTime expiresAt;

    // null = voucher toàn sàn (Admin), có giá trị = voucher của shop (Seller)
    Long shopId;
}
