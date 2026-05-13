package com.huusang.demo.Entity;

import com.huusang.demo.Enum.PaymentMethod;
import com.huusang.demo.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    PaymentStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Column(name = "transaction_id", length = 100)
    String transactionId;           // Mã giao dịch từ cổng thanh toán

    @Column(name = "paid_at")
    LocalDateTime paidAt;

}
