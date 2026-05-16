package com.huusang.demo.Enum;

public enum OrderStatus {
    /**
     * COD: Đơn mới tạo, chờ shop xác nhận
     * (trước đây là PENDING — giữ nguyên để backward-compat)
     */
    PENDING,

    /**
     * SEPAY: Đơn đã tạo, đang chờ user thanh toán QR
     * Stock chưa bị trừ — chỉ trừ sau khi SePay callback SUCCESS
     */
    PENDING_PAYMENT,

    /** SePay callback SUCCESS → stock trừ → order chuyển PAID */
    PAID,

    PREPARING,
    READY_TO_SHIP,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED
}
