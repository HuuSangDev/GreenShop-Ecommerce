package com.huusang.demo.Entity;

import com.huusang.demo.Enum.WithdrawalStatus;
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
@Table(name = "withdrawals")
public class Withdrawal {
    @Id
    String id;

    @PrePersist
    protected void onCreate() { this.id = UUID.randomUUID().toString(); }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    WithdrawalStatus status;

    @Column(name = "requested_at")
    LocalDateTime requestedAt;

    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;


}
