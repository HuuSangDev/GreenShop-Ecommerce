package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request cho API Preview — chỉ cần cartItemIds để tính toán,
 * không cần paymentMethod (chưa quyết định lúc preview).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutPreviewRequest {

    @NotEmpty(message = "Danh sách sản phẩm không được trống")
    List<String> cartItemIds;
}
