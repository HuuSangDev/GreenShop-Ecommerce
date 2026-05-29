package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeCreateRequest {

    /** ID của ShopOrder bị khiếu nại */
    @NotNull(message = "shopOrderId không được để trống")
    Long shopOrderId;

    /** Lý do khiếu nại */
    @NotBlank(message = "Lý do khiếu nại không được để trống")
    String reason;
}
