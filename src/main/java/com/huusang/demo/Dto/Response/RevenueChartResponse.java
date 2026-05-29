package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho biểu đồ doanh thu (Revenue Line Chart).
 * Mỗi điểm data là 1 khoảng thời gian (ngày/tháng/năm) và doanh thu tương ứng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueChartResponse {

    /** Khoảng thời gian: "DAILY" | "MONTHLY" | "YEARLY" */
    String period;

    /** Danh sách các điểm dữ liệu để vẽ biểu đồ */
    List<ChartPoint> data;

    /** Tổng doanh thu trong khoảng đang xem */
    BigDecimal totalRevenue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ChartPoint {
        /**
         * Nhãn trục X — định dạng theo period:
         * - DAILY:   "2026-05-29"
         * - MONTHLY: "2026-05"
         * - YEARLY:  "2026"
         */
        String label;

        /** Doanh thu tương ứng với mốc thời gian này */
        BigDecimal revenue;
    }
}
