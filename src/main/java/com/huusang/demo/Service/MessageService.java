package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.SendMessageRequest;
import com.huusang.demo.Dto.Response.ConversationResponse;
import com.huusang.demo.Dto.Response.MessageResponse;
import com.huusang.demo.Entity.Conversation;
import com.huusang.demo.Entity.Message;
import com.huusang.demo.Entity.Shop;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.ConversationRepository;
import com.huusang.demo.Repository.MessageRepository;
import com.huusang.demo.Repository.ShopRepository;
import com.huusang.demo.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MessageService {

    ConversationRepository conversationRepository;
    MessageRepository messageRepository;
    UserRepository userRepository;
    ShopRepository shopRepository;
    SimpMessagingTemplate messagingTemplate; // WebSocket push

    // ─────────────────────────────────────────────────────────────────────────
    // 1. GET OR CREATE CONVERSATION
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public ConversationResponse getOrCreateConversation(String currentUserEmail, String otherUserId) {
        User currentUser = resolveUser(currentUserEmail);
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getId().equals(otherUser.getId())) {
            throw new AppException(ErrorCode.CHAT_PARTICIPANT_INVALID);
        }

        // Kiểm tra xem đã có cuộc hội thoại nào giữa 2 người chưa
        Optional<Conversation> existing = conversationRepository.findBetweenUsers(currentUser.getId(), otherUser.getId());
        Conversation conversation;

        if (existing.isPresent()) {
            conversation = existing.get();
            log.info("Found existing conversation: id={}", conversation.getId());
        } else {
            // Xác định ai là buyer, ai là seller
            User buyer;
            User seller;

            // Nếu user kia có cửa hàng -> user kia là seller, mình là buyer
            boolean otherHasShop = shopRepository.existsByOwnerId(otherUser.getId());
            // Nếu mình có cửa hàng -> mình là seller, user kia là buyer
            boolean currentHasShop = shopRepository.existsByOwnerId(currentUser.getId());

            if (otherHasShop && !currentHasShop) {
                buyer = currentUser;
                seller = otherUser;
            } else if (currentHasShop && !otherHasShop) {
                buyer = otherUser;
                seller = currentUser;
            } else {
                // Mặc định: mình là buyer, đối tác là seller
                buyer = currentUser;
                seller = otherUser;
            }

            conversation = Conversation.builder()
                    .buyer(buyer)
                    .seller(seller)
                    .lastMessageAt(LocalDateTime.now())
                    .build();

            conversation = conversationRepository.save(conversation);
            log.info("Created new conversation: id={}, buyer={}, seller={}",
                    conversation.getId(), buyer.getEmail(), seller.getEmail());
        }

        return mapToConversationResponse(conversation, currentUser);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. GET MY CONVERSATIONS
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(String currentUserEmail) {
        User currentUser = resolveUser(currentUserEmail);
        List<Conversation> conversations = conversationRepository.findAllByUserOrderByLastMessageAtDesc(currentUser);

        return conversations.stream()
                .map(conv -> mapToConversationResponse(conv, currentUser))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. GET MESSAGES (PAGINATED)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public Page<MessageResponse> getMessages(String currentUserEmail, Long conversationId, Pageable pageable) {
        User currentUser = resolveUser(currentUserEmail);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        validateAccess(conversation, currentUser);

        // Đọc lịch sử chat -> Tự động đánh dấu tin nhắn đối phương gửi là đã đọc
        messageRepository.markAsReadByConversationAndOtherSender(conversation, currentUser.getId());

        Page<Message> messages = messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable);
        return messages.map(this::mapToMessageResponse);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. SEND MESSAGE
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public MessageResponse sendMessage(String currentUserEmail, Long conversationId, SendMessageRequest request) {
        User currentUser = resolveUser(currentUserEmail);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        validateAccess(conversation, currentUser);

        // Tạo tin nhắn mới
        Message message = Message.builder()
                .conversation(conversation)
                .sender(currentUser)
                .content(request.getContent())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        // Cập nhật thông tin tin nhắn cuối cùng trên Conversation
        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Đánh dấu các tin nhắn trước đó của đối phương gửi là đã đọc (isRead = true)
        messageRepository.markAsReadByConversationAndOtherSender(conversation, currentUser.getId());

        MessageResponse response = mapToMessageResponse(message);

        // ── Push realtime qua WebSocket ────────────────────────────────────────
        // Gửi cho receiver (người còn lại trong conversation)
        User receiver = conversation.getBuyer().getId().equals(currentUser.getId())
                ? conversation.getSeller()
                : conversation.getBuyer();

        // Push đến personal queue của receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                response
        );

        // Push broadcast vào topic của conversation (cả 2 tab đều nhận)
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversation.getId(),
                response
        );

        log.info("Message sent: id={}, convId={}, sender={}", message.getId(), conversation.getId(), currentUser.getEmail());
        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. MARK AS READ
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public void markAsRead(String currentUserEmail, Long conversationId) {
        User currentUser = resolveUser(currentUserEmail);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        validateAccess(conversation, currentUser);

        messageRepository.markAsReadByConversationAndOtherSender(conversation, currentUser.getId());
        log.info("Marked conversation id={} as read for user {}", conversation.getId(), currentUser.getEmail());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. GET UNREAD COUNT (GLOBAL BADGE)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public long getUnreadCount(String currentUserEmail) {
        User currentUser = resolveUser(currentUserEmail);
        return messageRepository.countUnreadMessagesForUser(currentUser.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateAccess(Conversation conversation, User user) {
        boolean isBuyer = conversation.getBuyer().getId().equals(user.getId());
        boolean isSeller = conversation.getSeller().getId().equals(user.getId());

        if (!isBuyer && !isSeller) {
            log.warn("Access denied to conversation id={} for user {}", conversation.getId(), user.getEmail());
            throw new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED);
        }
    }

    private ConversationResponse mapToConversationResponse(Conversation conversation, User currentUser) {
        // Tìm thông tin Shop của Seller nếu có
        Optional<Shop> shopOpt = shopRepository.findByOwner(conversation.getSeller());

        String shopName = shopOpt.map(Shop::getShopName).orElse(null);
        String shopLogo = shopOpt.map(Shop::getLogoUrl).orElse(null);

        String buyerName = conversation.getBuyer().getFullName() != null && !conversation.getBuyer().getFullName().isBlank()
                ? conversation.getBuyer().getFullName()
                : conversation.getBuyer().getUsername();

        String sellerName = conversation.getSeller().getFullName() != null && !conversation.getSeller().getFullName().isBlank()
                ? conversation.getSeller().getFullName()
                : conversation.getSeller().getUsername();

        long unreadCount = messageRepository.countUnreadMessagesForUserInConversation(conversation, currentUser.getId());

        return ConversationResponse.builder()
                .id(conversation.getId())
                .buyerId(conversation.getBuyer().getId())
                .buyerName(buyerName)
                .sellerId(conversation.getSeller().getId())
                .sellerName(sellerName)
                .shopName(shopName)
                .shopLogo(shopLogo)
                .lastMessage(conversation.getLastMessage())
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();
    }

    private MessageResponse mapToMessageResponse(Message message) {
        String senderName = message.getSender().getFullName() != null && !message.getSender().getFullName().isBlank()
                ? message.getSender().getFullName()
                : message.getSender().getUsername();

        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(senderName)
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isRead(message.isRead())
                .build();
    }
}
