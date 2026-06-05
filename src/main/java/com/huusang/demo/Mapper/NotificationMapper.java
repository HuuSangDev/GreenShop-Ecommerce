package com.huusang.demo.Mapper;

import com.huusang.demo.Dto.Response.NotificationResponse;
import com.huusang.demo.Entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
