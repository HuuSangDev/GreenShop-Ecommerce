package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueStatsResponse {

    // Tổng quan
    BigDecimal totalRevenue;
    BigDecimal totalOrders;

    // Doanh thu theo khoảng thời gian (key: ngày/tuần/tháng)
    Map<String, BigDecimal> revenueByPeriod;

    // Top sản phẩm bán chạy
    List<TopProductResponse> topProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductResponse {
        Long productId;
        String productName;
        Long totalSold;
        BigDecimal totalRevenue;
    }
}
