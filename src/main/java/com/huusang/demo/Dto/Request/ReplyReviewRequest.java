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
public class ReplyReviewRequest {

    @NotBlank(message = "Reply content is required")
    @Size(min = 1, max = 500, message = "Reply must not exceed 500 characters")
    String reply;
}
