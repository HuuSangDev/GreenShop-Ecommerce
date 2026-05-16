package com.huusang.demo.Dto.Request;

import com.huusang.demo.Enum.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutRequest {

    @NotEmpty(message = "Danh sách sản phẩm không được trống")
    List<String> cartItemIds;

    @NotNull(message = "Phương thức thanh toán không được trống")
    PaymentMethod paymentMethod;
}
