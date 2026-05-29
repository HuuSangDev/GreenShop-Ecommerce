package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Response DTO cho Admin Dashboard Overview.
 * Hiển thị các con số tổng kết nhanh ở trang chủ Admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminOverviewResponse {

    /** GMV — Tổng Gross Merchandise Value (tổng finalAmount toàn hệ thống) */
    BigDecimal totalGmv;

    /** Tổng số đơn hàng mới trong ngày hôm nay */
    long totalOrdersToday;

    /** Tổng số đơn hàng trong toàn hệ thống */
    long totalOrdersAllTime;

    /** Số shop mới đăng ký trong ngày hôm nay */
    long newShopsToday;

    /** Tổng số shop trên hệ thống */
    long totalShopsAllTime;

    /** Số khiếu nại (dispute) đang chờ xử lý (status = OPEN) */
    long openDisputes;

    /** Số yêu cầu rút tiền đang chờ duyệt */
    long pendingWithdrawals;

    /** Tổng số người dùng trên hệ thống */
    long totalUsers;
}
