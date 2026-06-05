package com.huusang.demo.Entity;

import com.huusang.demo.Enum.TransactionType;
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
    @JoinColumn(name = "shop_id", nullable = true)
    Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    WithdrawalStatus status;

    @Column(columnDefinition = "TEXT")
    String note;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
