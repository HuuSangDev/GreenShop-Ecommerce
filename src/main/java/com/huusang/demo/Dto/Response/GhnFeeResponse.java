package com.huusang.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response từ GHN API tính phí vận chuyển.
 * <p>
 * Format thực tế của GHN:
 * {
 *   "code": 200,
 *   "message": "Success",
 *   "data": {
 *     "total": 36300,
 *     "service_fee": 30000,
 *     "insurance_fee": 0,
 *     ...
 *   }
 * }
 * <p>
 * @JsonIgnoreProperties(ignoreUnknown = true) — bỏ qua các field GHN trả về mà ta không cần.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnFeeResponse {

    /** HTTP status code từ GHN (200 = success) */
    @JsonProperty("code")
    private Integer code;

    /** Message từ GHN ("Success" hoặc mô tả lỗi) */
    @JsonProperty("message")
    private String message;

    /** Dữ liệu phí vận chuyển */
    @JsonProperty("data")
    private FeeData data;

    /**
     * Kiểm tra response có thành công không.
     */
    public boolean isSuccess() {
        return code != null && code == 200 && data != null;
    }

    /**
     * Lấy tổng phí vận chuyển (VNĐ).
     * Trả về 0 nếu response lỗi.
     */
    public int getTotalFee() {
        return isSuccess() ? data.getTotal() : 0;
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeeData {

        /** Tổng phí vận chuyển (VNĐ) — bao gồm tất cả các khoản phí */
        @JsonProperty("total")
        private int total;

        /** Phí dịch vụ vận chuyển cơ bản */
        @JsonProperty("service_fee")
        private int serviceFee;

        /** Phí bảo hiểm hàng hóa */
        @JsonProperty("insurance_fee")
        private int insuranceFee;

        /** Phí đóng gói */
        @JsonProperty("pick_station_fee")
        private int pickStationFee;

        /** Phụ phí khác */
        @JsonProperty("coupon_value")
        private int couponValue;
    }
}
