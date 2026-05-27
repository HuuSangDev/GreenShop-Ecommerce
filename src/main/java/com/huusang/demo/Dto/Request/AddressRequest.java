package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {

    // ─── Thông tin hiển thị ───────────────────────────────────────────────────

    @NotBlank(message = "Họ tên không được trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    String fullName;

    @NotBlank(message = "Số điện thoại không được trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{8,10}$", message = "Số điện thoại không hợp lệ")
    String phone;

    @NotBlank(message = "Địa chỉ chi tiết không được trống")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    String street;   // Số nhà, tên đường, ngõ hẻm

    @NotBlank(message = "Tên phường/xã không được trống")
    String ward;     // Tên hiển thị: "Phường Vĩnh Phúc"

    @NotBlank(message = "Tên quận/huyện không được trống")
    String district; // Tên hiển thị: "Quận Ba Đình"

    @NotBlank(message = "Tên tỉnh/thành phố không được trống")
    String province; // Tên hiển thị: "Thành phố Hà Nội"

    // ─── Mã định danh GHN (chọn từ Dropdown tích hợp API GHN) ────────────────

    @NotBlank(message = "Mã phường/xã (GHN) không được trống")
    String wardCode;    // VD: "1A0807"

    @NotNull(message = "Mã quận/huyện (GHN) không được trống")
    Integer districtId; // VD: 1482

    @NotNull(message = "Mã tỉnh/thành phố (GHN) không được trống")
    Integer provinceId; // VD: 201

    // ─── Cờ mặc định ─────────────────────────────────────────────────────────

    /**
     * true  = đặt đây làm địa chỉ giao hàng mặc định.
     * false = không set mặc định (trừ khi đây là địa chỉ đầu tiên — ép buộc true).
     * null  = tương đương false.
     */
    Boolean isDefault = false;
}
