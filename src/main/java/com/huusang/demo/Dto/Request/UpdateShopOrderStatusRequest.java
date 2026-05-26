package com.huusang.demo.Dto.Request;

import com.huusang.demo.Enum.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateShopOrderStatusRequest {

    @NotNull(message = "Status không được để trống")
    OrderStatus status;
}
