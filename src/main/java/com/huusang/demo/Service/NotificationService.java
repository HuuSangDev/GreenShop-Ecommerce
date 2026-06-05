package com.huusang.demo.Service;

import com.huusang.demo.Dto.Response.NotificationResponse;
import com.huusang.demo.Entity.Notification;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Enum.NotificationType;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Mapper.NotificationMapper;
import com.huusang.demo.Repository.NotificationRepository;
import com.huusang.demo.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;
    NotificationMapper notificationMapper;
    SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendNotification(User user, NotificationType type, String title, String content, String referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .referenceId(referenceId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(notification);

        // Broadcast to user's personal queue
        // By default, Spring STOMP uses /user/{name}/queue/... 
        // We set Principal name as email in WebSocketConfig
        messagingTemplate.convertAndSendToUser(
                user.getEmail(), 
                "/queue/notifications", 
                response
        );
    }

    public List<NotificationResponse> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notificationMapper.toResponseList(notifications);
    }

    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(String notificationId, String email) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to update this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        notificationRepository.markAllAsReadByUser(user);
    }
}
