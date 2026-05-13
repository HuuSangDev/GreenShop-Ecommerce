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
@Table(name = "commissions")
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_order_id", nullable = false)
    ShopOrder shopOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    BigDecimal grossAmount;         // Doanh thu gốc của shop

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    BigDecimal commissionRate;      // % sàn thu, VD: 2.00

    @Column(name = "commission_amt", nullable = false, precision = 15, scale = 2)
    BigDecimal commissionAmt;       // Tiền sàn thu = gross * rate / 100

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    BigDecimal netAmount;           // Shop nhận = gross - commissionAmt


}
