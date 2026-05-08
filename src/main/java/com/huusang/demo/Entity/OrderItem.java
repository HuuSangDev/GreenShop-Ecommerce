package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_order_id", nullable = false)
    ShopOrder shopOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    ProductVariant productVariant;

    Integer quantity;
    @Column(name = "price_at_buy", nullable = false, precision = 15, scale = 2)
    BigDecimal priceAtBuy;          // Giá chốt tại thời điểm mua

    @Column(name = "discount_amount", precision = 15, scale = 2)
    BigDecimal discountAmount = BigDecimal.ZERO; // Phần voucher phân bổ (prorating)
}
