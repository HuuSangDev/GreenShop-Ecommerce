package com.huusang.demo.Dto.Request;

import com.huusang.demo.Enum.VoucherType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherCreateRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(min = 3, max = 50, message = "Mã voucher phải từ 3-50 ký tự")
    String code;

    @NotNull(message = "Loại voucher không được để trống")
    VoucherType type; // PERCENT | FIXED

    @NotNull(message = "Giá trị voucher không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị voucher phải lớn hơn 0")
    BigDecimal value;

    // Chỉ dùng khi type = PERCENT, giới hạn số tiền giảm tối đa
    BigDecimal maxDiscount;

    @DecimalMin(value = "0.0", message = "Giá trị đơn tối thiểu không được âm")
    BigDecimal minOrderAmt;

    @Min(value = 1, message = "Số lượt dùng tối thiểu là 1")
    Integer maxUsage; // null = unlimited

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    LocalDateTime startsAt;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    LocalDateTime expiresAt;

    // null = voucher toàn sàn (Admin), có giá trị = voucher của shop (Seller)
    Long shopId;

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public VoucherType getType() {
        return type;
    }

    public void setType(VoucherType type) {
        this.type = type;
    }

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

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
}