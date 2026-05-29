package com.huusang.demo.Enum;

public enum DisputeStatus {
    /** Khiếu nại đang chờ Admin xử lý */
    OPEN,

    /** Admin đã phán quyết: Người mua đúng — hoàn tiền từ ví shop */
    RESOLVED_BUYER_WIN,

    /** Admin đã phán quyết: Người bán đúng — giải ngân vào ShopWallet */
    RESOLVED_SELLER_WIN
}
