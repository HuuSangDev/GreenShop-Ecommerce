package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "shopWallets")
public class ShopWallet {
    @Id
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    Shop shop;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_earned", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalEarned = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate() { this.id = UUID.randomUUID().toString(); }
}
