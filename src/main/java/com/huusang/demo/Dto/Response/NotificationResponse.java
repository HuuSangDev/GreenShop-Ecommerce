package com.huusang.demo.Dto.Response;

import com.huusang.demo.Enum.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String id;
    NotificationType type;
    String title;
    String content;
    String referenceId;
    boolean isRead;
    LocalDateTime createdAt;
}
