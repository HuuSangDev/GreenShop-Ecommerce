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
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order_id",       columnList = "order_id"),
        @Index(name = "idx_payment_transaction_ref", columnList = "transaction_ref")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    PaymentStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    /**
     * Mã tham chiếu giao dịch do hệ thống tự sinh.
     * Format: ORDER_{orderId}  — dùng làm nội dung chuyển khoản để SePay match webhook.
     * Index riêng để webhook callback tìm kiếm nhanh O(log n).
     */
    @Column(name = "transaction_ref", length = 100, unique = true)
    String transactionRef;

    /**
     * URL QR thanh toán SePay — trả về frontend để render <img src='checkoutUrl' />.
     * Null với COD.
     */
    @Column(name = "checkout_url", length = 1024)
    String checkoutUrl;

    /** Mã giao dịch từ cổng thanh toán trả về (sau khi payment thành công) */
    @Column(name = "transaction_id", length = 100)
    String transactionId;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
