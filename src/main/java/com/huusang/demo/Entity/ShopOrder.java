package com.huusang.demo.Entity;

import com.huusang.demo.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "shop_orders")
public class ShopOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Trỏ về Đơn hàng gốc của Khách
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    // Trỏ về Shop nào phải xử lý gói hàng này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Trạng thái độc lập của gói hàng (Shop A đang giao, Shop B có thể đã hủy)
    OrderStatus status; // PENDING, PREPARING, SHIPPING, DELIVERED, CANCELLED

    // Tiền ship riêng của gói hàng này
    @Column(name = "shipping_fee")
    BigDecimal shippingFee;

    // Tổng tiền hàng của riêng Shop này (Để đối soát trả tiền cho Shop)
    @Column(name = "shop_total_amount")
    BigDecimal shopTotalAmount;

    // 1 Gói hàng sẽ chứa nhiều sản phẩm (OrderItem)
    @OneToMany(mappedBy = "shopOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> orderItems = new ArrayList<>();
}
