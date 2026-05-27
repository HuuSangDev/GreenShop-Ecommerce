package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {

    String id;

    // ─── Thông tin hiển thị ───────────────────────────────────────────────────
    String fullName;
    String phone;
    String street;
    String ward;
    String district;
    String province;

    /** Địa chỉ đầy đủ dạng một dòng — tiện cho frontend hiển thị */
    String fullAddress; // = street + ", " + ward + ", " + district + ", " + province

    // ─── Mã định danh GHN (dùng khi gọi API tính phí ship) ───────────────────
    String wardCode;
    Integer districtId;
    Integer provinceId;

    // ─── Trạng thái ──────────────────────────────────────────────────────────
    Boolean isDefault;
}
