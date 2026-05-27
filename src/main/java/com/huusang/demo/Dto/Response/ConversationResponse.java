package com.huusang.demo.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    Long id;
    String buyerId;
    String buyerName;
    String sellerId;
    String sellerName;
    String shopName;
    String shopLogo;
    String lastMessage;
    LocalDateTime lastMessageAt;
    long unreadCount;
}
