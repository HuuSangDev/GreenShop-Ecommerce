package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "address")
public class Address {
    @Id
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Column(nullable = false, length = 20)
    String phone;

    @Column(nullable = false, length = 255)
    String street;      // Số nhà / ngõ hẻm chi tiết

    // === LƯU TÊN ĐỂ HIỂN THỊ CHO ĐẸP (Frontend dùng) ===
    @Column(nullable = false, length = 100)
    String ward;        // VD: Phường Vĩnh Phúc

    @Column(nullable = false, length = 100)
    String district;    // VD: Quận Ba Đình

    @Column(nullable = false, length = 100)
    String province;    // VD: Thành phố Hà Nội

    // === LƯU MÃ ĐỊNH DANH ĐỂ GỌI API GHN TÍNH SHIP (Backend dùng) ===
    @Column(name = "ward_code", nullable = false, length = 20)
    String wardCode;    // VD: "1A0807"

    @Column(name = "district_id", nullable = false)
    Integer districtId; // VD: 1482

    @Column(name = "province_id", nullable = false)
    Integer provinceId; // VD: 201

    @Column(name = "is_default", nullable = false)
    Boolean isDefault = false;

    @PrePersist
    protected void onCreate() { this.id = UUID.randomUUID().toString(); }
}
