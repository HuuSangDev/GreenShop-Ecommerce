package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "commissions")
public class Commission {

    @Id
    String id;

    @PrePersist
    protected void onCreate() { this.id = UUID.randomUUID().toString(); }

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

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ShopOrder getShopOrder() {
        return shopOrder;
    }

    public void setShopOrder(ShopOrder shopOrder) {
        this.shopOrder = shopOrder;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getCommissionAmt() {
        return commissionAmt;
    }

    public void setCommissionAmt(BigDecimal commissionAmt) {
        this.commissionAmt = commissionAmt;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
}
