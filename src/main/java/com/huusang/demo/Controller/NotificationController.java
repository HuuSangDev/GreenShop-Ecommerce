package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Response.NotificationResponse;
import com.huusang.demo.Service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(JwtAuthenticationToken auth) {
        String email = auth.getToken().getSubject();
        return ApiResponse.<List<NotificationResponse>>builder()
                .result(notificationService.getMyNotifications(email))
                .build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(JwtAuthenticationToken auth) {
        String email = auth.getToken().getSubject();
        return ApiResponse.<Long>builder()
                .result(notificationService.getUnreadCount(email))
                .build();
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id, JwtAuthenticationToken auth) {
        String email = auth.getToken().getSubject();
        notificationService.markAsRead(id, email);
        return ApiResponse.<Void>builder()
                .message("Marked as read")
                .build();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(JwtAuthenticationToken auth) {
        String email = auth.getToken().getSubject();
        notificationService.markAllAsRead(email);
        return ApiResponse.<Void>builder()
                .message("Marked all as read")
                .build();
    }
}
