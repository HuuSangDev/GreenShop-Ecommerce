package com.huusang.demo.Entity;

import com.huusang.demo.Enum.ShopStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "shops")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    User owner; // Chủ cửa hàng

    @Column(name = "shop_name", nullable = false)
    String shopName;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "banner_url")
    String bannerUrl;

    @Column(name = "logo_url")
    String logoUrl;

    @Builder.Default
    Double rating = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    ShopStatus status = ShopStatus.ACTIVE;

    @Column(name = "created_at")
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    // ─── Thông tin địa chỉ kho hàng & GHN ────────────────────────────────────

    /** ShopId của shop này trên hệ thống GHN — dùng làm Header "ShopId" khi gọi GHN API */
    @Column(name = "ghn_shop_id")
    Integer ghnShopId;

    /** ID quận/huyện kho hàng (theo mã GHN) — ví dụ: 1442 = Quận 1, TP.HCM */
    @Column(name = "district_id")
    Integer districtId;

    /** Mã phường/xã kho hàng (theo mã GHN) — ví dụ: "21211" = P. Bến Nghé */
    @Column(name = "ward_code", length = 20)
    String wardCode;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    List<ShopOrder> orders;
}
