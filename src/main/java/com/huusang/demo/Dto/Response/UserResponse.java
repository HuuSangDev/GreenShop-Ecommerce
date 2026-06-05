package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String email;
    String fullName;
    String avatar;
    String phoneNumber;
    boolean active;
    Set<RoleResponse> roles;
    java.time.LocalDateTime createdAt;
}
