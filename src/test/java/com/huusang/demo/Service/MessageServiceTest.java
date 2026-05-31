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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService — Unit Tests")
class MessageServiceTest {

    @Mock ConversationRepository conversationRepository;
    @Mock MessageRepository messageRepository;
    @Mock UserRepository userRepository;
    @Mock ShopRepository shopRepository;

    @InjectMocks
    MessageService messageService;

    private User buyer;
    private User seller;
    private Shop shop;
    private Conversation conversation;
    private Message message;

    private static final String BUYER_EMAIL = "buyer@test.com";
    private static final String SELLER_EMAIL = "seller@test.com";

    @BeforeEach
    void setUp() {
        buyer = User.builder()
                .id("buyer-uuid")
                .email(BUYER_EMAIL)
                .username("buyer")
                .fullName("Nguyễn Văn Mua")
                .build();

        seller = User.builder()
                .id("seller-uuid")
                .email(SELLER_EMAIL)
                .username("seller")
                .fullName("Trần Thị Bán")
                .build();

        shop = Shop.builder()
                .id(1L)
                .shopName("Shop Tech Việt")
                .owner(seller)
                .logoUrl("http://logo.jpg")
                .build();

        conversation = Conversation.builder()
                .id(100L)
                .buyer(buyer)
                .seller(seller)
                .lastMessage("Xin chào")
                .lastMessageAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        message = Message.builder()
                .id(500L)
                .conversation(conversation)
                .sender(buyer)
                .content("Xin chào")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("✅ getOrCreateConversation: Lấy hội thoại cũ thành công")
    void getOrCreateConversation_existing_success() {
        when(userRepository.findByEmail(BUYER_EMAIL)).thenReturn(Optional.of(buyer));
        when(userRepository.findById("seller-uuid")).thenReturn(Optional.of(seller));
        when(conversationRepository.findBetweenUsers(buyer, seller)).thenReturn(Optional.of(conversation));
        when(shopRepository.findByOwner(seller)).thenReturn(Optional.of(shop));
        when(messageRepository.countUnreadMessagesForUserInConversation(conversation, buyer)).thenReturn(2L);

        ConversationResponse response = messageService.getOrCreateConversation(BUYER_EMAIL, "seller-uuid");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getShopName()).isEqualTo("Shop Tech Việt");
        assertThat(response.getUnreadCount()).isEqualTo(2L);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ getOrCreateConversation: Tạo hội thoại mới khi chưa có")
    void getOrCreateConversation_new_success() {
        when(userRepository.findByEmail(BUYER_EMAIL)).thenReturn(Optional.of(buyer));
        when(userRepository.findById("seller-uuid")).thenReturn(Optional.of(seller));
        when(conversationRepository.findBetweenUsers(buyer, seller)).thenReturn(Optional.empty());
        when(shopRepository.existsByOwnerId("seller-uuid")).thenReturn(true);
        when(shopRepository.existsByOwnerId("buyer-uuid")).thenReturn(false);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(shopRepository.findByOwner(seller)).thenReturn(Optional.of(shop));

        ConversationResponse response = messageService.getOrCreateConversation(BUYER_EMAIL, "seller-uuid");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("❌ getOrCreateConversation: Trò chuyện với chính mình -> throw CHAT_PARTICIPANT_INVALID")
    void getOrCreateConversation_selfChat_throwsException() {
        when(userRepository.findByEmail(BUYER_EMAIL)).thenReturn(Optional.of(buyer));
        when(userRepository.findById("buyer-uuid")).thenReturn(Optional.of(buyer));

        assertThatThrownBy(() -> messageService.getOrCreateConversation(BUYER_EMAIL, "buyer-uuid"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException appEx = (AppException) e;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CHAT_PARTICIPANT_INVALID);
                });
    }

    @Test
    @DisplayName("✅ getMyConversations: Trả về danh sách hội thoại")
    void getMyConversations_success() {
        when(userRepository.findByEmail(BUYER_EMAIL)).thenReturn(Optional.of(buyer));
        when(conversationRepository.findAllByUserOrderByLastMessageAtDesc(buyer)).thenReturn(List.of(conversation));
        when(shopRepository.findByOwner(seller)).thenReturn(Optional.of(shop));
        when(messageRepository.countUnreadMessagesForUserInConversation(conversation, buyer)).thenReturn(1L);

        List<ConversationResponse> response = messageService.getMyConversations(BUYER_EMAIL);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(100L);
        assertThat(response.get(0).getBuyerName()).isEqualTo("Nguyễn Văn Mua");
    }

    @Test
    @DisplayName("✅ getMessages: Lấy tin nhắn thành công và đánh dấu đã đọc tin đối phương")
    void getMessages_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messagePage = new PageImpl<>(List.of(message));

        when(userRepository.findByEmail(BUYER_EMAIL)).thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable)).thenReturn(messagePage);

        Page<MessageResponse> response = messageService.getMessages(BUYER_EMAIL, 100L, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getContent()).isEqualTo("Xin chào");
        verify(messageRepository).markAsReadByConversationAndOtherSender(conversation, buyer);
    }

    @Test
    @DisplayName("❌ getMessages: Không phải người tham gia cuộc trò chuyện -> throw CONVERSATION_ACCESS_DENIED")
    void getMessages_accessDenied_throwsException() {
        User outsider = User.builder().id("outsider-uuid").email("outsider@test.com").build();
        when(userRepository.findByEmail("outsider@test.com")).thenReturn(Optional.of(outsider));
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> messageService.getMessages("outsider@test.com", 100L, PageRequest.of(0, 10)))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException appEx = (AppException) e;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CONVERSATION_ACCESS_DENIED);
                });
    }

    @Test
    @DisplayName("✅ sendMessage: Gửi tin nhắn thành công và cập nhật tin nhắn cuối cùng")
    void sendMessage_success() {
        SendMessageRequest request = SendMessageRequest.builder().content("Tin nhắn mới").build();

        when(userRepository.findByEmail(BUYER_EMAIL)).thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = messageService.sendMessage(BUYER_EMAIL, 100L, request);

        assertThat(response).isNotNull();
        verify(conversationRepository).save(conversation);
        verify(messageRepository).markAsReadByConversationAndOtherSender(conversation, buyer);
        assertThat(conversation.getLastMessage()).isEqualTo("Tin nhắn mới");
    }
}
