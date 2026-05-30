package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    Shop shop;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<ProductVariant>variants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @Column(name = "product_name", nullable = false)
    String productName;

    @Column(columnDefinition = "TEXT")
    String description;
    BigDecimal price;

    @Column(name = "stock_quantity")
    Integer stockQuantity;

    String imageUrl;

    boolean available = true;

    @Column(name = "hidden", columnDefinition = "BOOLEAN DEFAULT FALSE")
    boolean hidden = false;

    @Column(name = "hide_reason", columnDefinition = "TEXT")
    String hideReason;

    LocalDateTime createdAt = LocalDateTime.now();
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
