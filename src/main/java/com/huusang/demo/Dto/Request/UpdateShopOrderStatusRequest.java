package com.huusang.demo.Dto.Request;

import com.huusang.demo.Enum.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Seller cập nhật trạng thái ShopOrder của mình.
 * Các chuyển trạng thái hợp lệ:
 *   PENDING / PAID     → PREPARING     (xác nhận, bắt đầu chuẩn bị)
 *   PREPARING          → READY_TO_SHIP (chuẩn bị xong)
 *   PREPARING          → CANCELLED     (hủy trước khi giao)
 *   READY_TO_SHIP      → SHIPPED       (đã bàn giao vận chuyển)
 *   SHIPPED            → DELIVERED     (đã giao đến tay khách)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateShopOrderStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    OrderStatus status;

    String note; // Lý do hủy hoặc ghi chú thêm (tùy chọn)
}
