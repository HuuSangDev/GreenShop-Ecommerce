package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopApplicationRequest {

    @NotBlank(message = "Tên gian hàng không được để trống")
    @Size(min = 3, max = 100, message = "Tên gian hàng từ 3 đến 100 ký tự")
    String shopName;

    @Size(max = 1000, message = "Mô tả không quá 1000 ký tự")
    String description;

    @NotBlank(message = "Mã số thuế không được để trống")
    String taxCode;

    String taxAddress;

    String taxFullName;
}
