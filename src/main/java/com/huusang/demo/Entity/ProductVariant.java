package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @Column(name = "variant_name")
    String variantName; // Ví dụ: "Màu Xanh, 128GB" hoặc "Điện thoại A"

    BigDecimal price; // Giá có thể khác nhau giữa các bản

    @Column(name = "stock_quantity")
    Integer stockQuantity; // Kho riêng cho bản này

    String sku; // Mã định danh kho (Stock Keeping Unit)

    /**
     * Khối lượng (gram) — dùng để tính phí ship GHN.
     * Nullable: nếu null → OrderService tự gán DEFAULT_WEIGHT_PER_ITEM_GRAM (200g).
     */
    @Column(name = "weight")
    Integer weight;

    // Optimistic locking — tránh race condition khi nhiều user cùng thêm vào giỏ
    @Version
    Long version;


}
