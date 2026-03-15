package com.huusang.demo.Entity;

import com.huusang.demo.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    User buyer;

    @Column(name = "total_amount")
    BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    OrderStatus status = OrderStatus.PENDING;

    String shippingAddress;
    String paymentMethod;
    LocalDateTime createdAt = LocalDateTime.now();



    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<ShopOrder> orders;
}
