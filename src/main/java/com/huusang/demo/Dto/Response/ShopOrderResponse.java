package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopOrderResponse {

    Long shopOrderId;
    Long shopId;
    String shopName;
    OrderStatus status;
    BigDecimal shopTotalAmount;
    List<OrderItemResponse> items;
}
