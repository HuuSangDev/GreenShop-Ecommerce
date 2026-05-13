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
    String street;      // Số nhà / tên đường / thôn xóm

    @Column(nullable = false, length = 100)
    String ward;        // Phường / Xã / Thị trấn

    @Column(nullable = false, length = 100)
    String district;    // Quận / Huyện / Thị xã

    @Column(nullable = false, length = 100)
    String province;    // Tỉnh / Thành phố

    @Column(name = "is_default", nullable = false)
    Boolean isDefault = false;

    @PrePersist
    protected void onCreate() { this.id = UUID.randomUUID().toString(); }
}
