package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response của POST /api/v1/checkouts/preview.
 * Trả về đủ thông tin để frontend hiển thị trang xác nhận đặt hàng:
 * tổng tiền, danh sách sản phẩm, phương thức thanh toán khả dụng.
 * <p>
 * QUAN TRỌNG: API này KHÔNG tạo order, KHÔNG trừ stock.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutPreviewResponse {

    BigDecimal subtotal;        // Tổng tiền hàng (sum của item price * qty)
    BigDecimal shippingFee;     // Phí ship (hiện tại = 0, có thể mở rộng)
    BigDecimal discountAmount;  // Giảm giá (voucher, chưa implement = 0)
    BigDecimal finalAmount;     // = subtotal + shippingFee - discountAmount

    int totalItems;             // Tổng số lượng sản phẩm (sum qty)
    int totalDistinctItems;     // Số loại sản phẩm khác nhau

    List<PreviewItemResponse> items;
    List<PaymentMethod> availablePaymentMethods;  // [COD, SEPAY]

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PreviewItemResponse {
        String cartItemId;
        Long variantId;
        String variantName;
        String productName;
        String imageUrl;
        Integer quantity;
        BigDecimal price;       // Giá hiện tại của variant (backend tính)
        BigDecimal subtotal;    // price * quantity
        Integer availableStock; // Để frontend cảnh báo nếu gần hết
    }
}
