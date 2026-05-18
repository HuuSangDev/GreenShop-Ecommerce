package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị voucher phải lớn hơn 0")
    BigDecimal value;

    BigDecimal maxDiscount;

    @DecimalMin(value = "0.0", message = "Giá trị đơn tối thiểu không được âm")
    BigDecimal minOrderAmt;

    @Min(value = 1, message = "Số lượt dùng tối thiểu là 1")
    Integer maxUsage;

    LocalDateTime startsAt;

    LocalDateTime expiresAt;

    // Getters and Setters
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public BigDecimal getMinOrderAmt() {
        return minOrderAmt;
    }

    public void setMinOrderAmt(BigDecimal minOrderAmt) {
        this.minOrderAmt = minOrderAmt;
    }

    public Integer getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(Integer maxUsage) {
        this.maxUsage = maxUsage;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}