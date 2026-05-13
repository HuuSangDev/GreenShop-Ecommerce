package com.huusang.demo.Entity;

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

    String description;

    Double rating = 0.0;

    @Column(name = "created_at")
    LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    List<ShopOrder> orders;
}
