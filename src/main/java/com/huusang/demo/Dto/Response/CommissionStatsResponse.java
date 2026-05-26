package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommissionStatsResponse {

    BigDecimal totalCommission;           // Tổng hoa hồng sàn đã thu (toàn bộ)
    BigDecimal totalGrossAmount;          // Tổng doanh thu gốc của tất cả shops
    BigDecimal totalNetAmount;            // Tổng tiền shops đã nhận
    Long totalOrders;                     // Tổng số đơn đã tính hoa hồng
    Map<String, BigDecimal> commissionByPeriod;  // Hoa hồng theo ngày/tháng
}
