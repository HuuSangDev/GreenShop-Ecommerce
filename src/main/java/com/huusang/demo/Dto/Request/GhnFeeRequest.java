package com.huusang.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Request body gửi tới GHN API tính phí vận chuyển.
 * <p>
 * snake_case khớp với API GHN, ánh xạ tự động qua @JsonProperty.
 * Dùng @JsonInclude(NON_NULL) để bỏ qua các field null (ví dụ: items khi là hàng nhẹ).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GhnFeeRequest {

    /**
     * Loại dịch vụ:
     *   2 = Hàng nhẹ  (kích thước/khối lượng lấy từ length/width/height/weight)
     *   5 = Hàng nặng (kích thước/khối lượng lấy từ items[].length/width/height/weight)
     */
    @JsonProperty("service_type_id")
    private Integer serviceTypeId;

    /** ID quận/huyện gửi hàng */
    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    /** Mã phường/xã gửi hàng */
    @JsonProperty("from_ward_code")
    private String fromWardCode;

    /** ID quận/huyện nhận hàng */
    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    /** Mã phường/xã nhận hàng */
    @JsonProperty("to_ward_code")
    private String toWardCode;

    /** Chiều dài gói hàng (cm) — dùng cho service_type_id = 2 */
    @JsonProperty("length")
    private Integer length;

    /** Chiều rộng gói hàng (cm) — dùng cho service_type_id = 2 */
    @JsonProperty("width")
    private Integer width;

    /** Chiều cao gói hàng (cm) — dùng cho service_type_id = 2 */
    @JsonProperty("height")
    private Integer height;

    /** Khối lượng gói hàng (gram) */
    @JsonProperty("weight")
    private Integer weight;

    /** Giá trị bảo hiểm (0 = không mua bảo hiểm) */
    @JsonProperty("insurance_value")
    @Builder.Default
    private Integer insuranceValue = 0;

    /** Mã coupon (null = không dùng) */
    @JsonProperty("coupon")
    private String coupon;

    /**
     * Danh sách sản phẩm trong đơn.
     * Bắt buộc khi service_type_id = 5 (Hàng nặng).
     * null khi service_type_id = 2 (sẽ bị bỏ qua nhờ @JsonInclude NON_NULL).
     */
    @JsonProperty("items")
    private List<Item> items;

    // ─────────────────────────────────────────────────────────────────────────
    //  INNER CLASS — Item trong đơn hàng
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Thông tin từng sản phẩm — dùng để tính cước cho service_type_id = 5 (Hàng nặng).
     * GHN lấy kích thước/khối lượng lớn nhất trong items để tính phí.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {

        /** Tên sản phẩm */
        @JsonProperty("name")
        private String name;

        /** Số lượng */
        @JsonProperty("quantity")
        private Integer quantity;

        /** Chiều dài sản phẩm (cm) */
        @JsonProperty("length")
        private Integer length;

        /** Chiều rộng sản phẩm (cm) */
        @JsonProperty("width")
        private Integer width;

        /** Chiều cao sản phẩm (cm) */
        @JsonProperty("height")
        private Integer height;

        /** Khối lượng sản phẩm (gram) */
        @JsonProperty("weight")
        private Integer weight;
    }
}
