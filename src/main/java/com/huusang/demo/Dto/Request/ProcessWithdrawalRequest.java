package com.huusang.demo.Dto.Request;

import com.huusang.demo.Enum.WithdrawalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProcessWithdrawalRequest {

    @NotNull(message = "Trạng thái không được để trống")
    WithdrawalStatus status; // APPROVED hoặc REJECTED

    String adminNote;
}
