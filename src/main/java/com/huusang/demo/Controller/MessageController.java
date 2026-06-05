package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.SendMessageRequest;
import com.huusang.demo.Dto.Response.ConversationResponse;
import com.huusang.demo.Dto.Response.MessageResponse;
import com.huusang.demo.Service.MessageService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageController {

    MessageService messageService;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. GET OR CREATE CONVERSATION
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/conversations")
    public ApiResponse<ConversationResponse> getOrCreateConversation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String otherUserId) {
        return ApiResponse.<ConversationResponse>builder()
                .message("Lấy hoặc tạo hội thoại thành công")
                .result(messageService.getOrCreateConversation(getEmail(jwt), otherUserId))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. GET MY CONVERSATIONS
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/conversations")
    public ApiResponse<List<ConversationResponse>> getMyConversations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String role) {
        return ApiResponse.<List<ConversationResponse>>builder()
                .message("Danh sách hội thoại của tôi")
                .result(messageService.getMyConversations(getEmail(jwt), role))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. GET MESSAGES (PAGINATED)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<Page<MessageResponse>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<MessageResponse>>builder()
                .message("Lịch sử tin nhắn")
                .result(messageService.getMessages(getEmail(jwt), conversationId, pageable))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. SEND MESSAGE
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/conversations/{conversationId}/messages")
    public ApiResponse<MessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.<MessageResponse>builder()
                .message("Gửi tin nhắn thành công")
                .result(messageService.sendMessage(getEmail(jwt), conversationId, request))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. MARK AS READ
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/conversations/{conversationId}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long conversationId) {
        messageService.markAsRead(getEmail(jwt), conversationId);
        return ApiResponse.<Void>builder()
                .message("Đã đánh dấu đã đọc")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. GET UNREAD COUNT (GLOBAL BADGE)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<Long>builder()
                .message("Số lượng tin nhắn chưa đọc")
                .result(messageService.getUnreadCount(getEmail(jwt)))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private String getEmail(Jwt jwt) {
        return jwt.getSubject(); // JWT subject là email (xem AuthService.generateToken)
    }
}
