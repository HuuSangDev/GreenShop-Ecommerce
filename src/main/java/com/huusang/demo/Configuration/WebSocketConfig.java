package com.huusang.demo.Configuration;

import com.huusang.demo.Repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.crypto.spec.SecretKeySpec;

/**
 * WebSocket + STOMP config.
 *
 * Client kết nối:     ws://localhost:8080/ecommerce/ws
 * Subscribe:          /user/queue/messages   (tin nhắn của mình)
 * Subscribe:          /topic/conversation.{id}  (broadcast trong phòng)
 * Publish (send):     /app/chat.send
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${jwt.signerKey}")
    private String signerKey;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho message broker (subscribe)
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix cho message gửi từ client → server
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix cho personal queue
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Intercept CONNECT frame để validate JWT.
     * Client phải gửi JWT trong header: Authorization: Bearer <token>
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) return message;

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
                            var decoder = NimbusJwtDecoder
                                    .withSecretKey(secretKeySpec)
                                    .macAlgorithm(MacAlgorithm.HS512)
                                    .build();

                            var jwt = decoder.decode(token);

                            // Kiểm tra token bị blacklist (logout)
                            if (invalidatedTokenRepository.existsById(jwt.getId())) {
                                throw new BadJwtException("Token đã bị vô hiệu hóa");
                            }

                            // Set principal để Spring biết user này là ai (/user/queue/...)
                            accessor.setUser(() -> jwt.getSubject());
                            log.info("[WS] User connected: {}", jwt.getSubject());

                        } catch (Exception e) {
                            log.warn("[WS] Invalid JWT on CONNECT: {}", e.getMessage());
                            throw new IllegalArgumentException("Invalid JWT: " + e.getMessage());
                        }
                    } else {
                        log.warn("[WS] No JWT on CONNECT — connection rejected");
                        throw new IllegalArgumentException("Authorization header missing");
                    }
                }
                return message;
            }
        });
    }
}
