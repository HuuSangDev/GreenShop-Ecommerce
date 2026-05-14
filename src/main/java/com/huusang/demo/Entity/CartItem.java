package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cart_variant",
                columnNames = {"cart_id", "variant_id"}
        ),
        indexes = @Index(name = "idx_cartitem_cart_id", columnList = "cart_id")
)
public class CartItem {

    @Id
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    ProductVariant productVariant;

    @Column(nullable = false)
    @Builder.Default
    Integer quantity = 1;

    // Lưu giá tại thời điểm thêm vào giỏ — không bị ảnh hưởng khi seller đổi giá
    @Column(name = "price_snapshot", nullable = false, precision = 15, scale = 2)
    BigDecimal priceSnapshot;

    @Column(name = "added_at", updatable = false)
    LocalDateTime addedAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
        this.addedAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
