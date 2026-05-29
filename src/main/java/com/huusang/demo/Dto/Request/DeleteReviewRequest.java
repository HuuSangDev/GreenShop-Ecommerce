package com.huusang.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteReviewRequest {

    @NotBlank(message = "Delete reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    String reason;
}
