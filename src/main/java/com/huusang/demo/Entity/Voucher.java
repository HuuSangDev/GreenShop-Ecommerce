package com.huusang.demo.Entity;

import com.huusang.demo.Enum.VoucherType;
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
@Table(name = "vouchers")
public class Voucher {
    @Id
    private String id;

    // NULL = voucher toàn sàn (Admin tạo)
    // Có giá trị = voucher riêng của shop
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    Shop shop;

    @Column(unique = true, nullable = false, length = 50)
    String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    VoucherType type;   // PERCENT | FIXED

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal value;

    @Column(name = "max_discount", precision = 15, scale = 2)
    BigDecimal maxDiscount;

    @Column(name = "min_order_amt", precision = 15, scale = 2)
    BigDecimal minOrderAmt = BigDecimal.ZERO;

    @Column(name = "max_usage")
    Integer maxUsage;

    @Column(name = "used_count")
    Integer usedCount = 0;

    @Column(name = "starts_at", nullable = false)
    LocalDateTime startsAt;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(nullable = false)
    boolean active = true;

    @PrePersist
    protected void onCreate() { this.id = UUID.randomUUID().toString(); }
}
