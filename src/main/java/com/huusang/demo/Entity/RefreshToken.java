package com.huusang.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    // Đây là chuỗi UUID ngẫu nhiên trả về cho Frontend
    @Column(nullable = false, unique = true)
    String token;

    // Thời gian hết hạn (Ví dụ: 7 ngày kể từ lúc tạo)
    @Column(nullable = false)
    Instant expiryDate;

    // Quan hệ ManyToOne: Nhiều thẻ RefreshToken có thể thuộc về 1 User
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;
}
