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
@Table(name = "adminWallets")
public class AdminWallet {
    @Id
    String id;

    @Column(precision = 15, scale = 2)
    BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_earned", precision = 15, scale = 2)
    BigDecimal totalEarned = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", precision = 15, scale = 2)
    BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate() { 
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
