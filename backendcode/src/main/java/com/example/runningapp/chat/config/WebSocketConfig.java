package com.example.runningapp.chat.config;

import com.example.runningapp.chat.WebSocketSessionManager;
import com.example.runningapp.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final WebSocketSessionManager sessionManager;
    private final JwtService jwtService;

    public WebSocketConfig(WebSocketSessionManager sessionManager, JwtService jwtService) {
        this.sessionManager = sessionManager;
        this.jwtService = jwtService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker with prefix /topic and /queue
        config.enableSimpleBroker("/topic", "/queue");
        // Messages with prefix /app will be routed to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that clients will connect to
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Raw WebSocket endpoint for Android/iOS clients (no STOMP)
        registry.addHandler(sessionManager, "/ws/chat/websocket")
                .setAllowedOrigins("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        // Extract JWT token from Authorization header
                        String authHeader = request.getHeaders().getFirst("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);
                            try {
                                if (jwtService.isTokenValid(token)) {
                                    String userId = jwtService.extractUserId(token);
                                    attributes.put("userId", userId);
                                    log.info("WebSocket handshake: userId={}", userId);
                                    return true;
                                }
                            } catch (Exception e) {
                                log.error("Invalid JWT token", e);
                            }
                        }
                        log.warn("WebSocket handshake failed: no valid token");
                        return false;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                });
    }
}
