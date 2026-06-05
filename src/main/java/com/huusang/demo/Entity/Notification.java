package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import com.huusang.demo.Enum.NotificationType;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    NotificationType type;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String content;

    // Optional reference ID (e.g. order ID, product ID, etc.)
    @Column(name = "reference_id")
    String referenceId;

    @Builder.Default
    @Column(name = "is_read")
    boolean isRead = false;

    @Builder.Default
    @Column(name = "created_at")
    LocalDateTime createdAt = LocalDateTime.now();
}
