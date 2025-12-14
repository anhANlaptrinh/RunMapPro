package com.example.runningapp.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager extends TextWebSocketHandler {

    // userId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
        } else {
            log.warn("WebSocket connection without userId, closing. sessionId={}", session.getId());
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket disconnected: userId={}, sessionId={}, status={}", userId, session.getId(), status);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Client sends messages via REST API, not via WebSocket
        log.debug("Received message from client: {}", message.getPayload());
    }

    public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.debug("Sent message to userId={}: {}", userId, message);
            } catch (IOException e) {
                log.error("Failed to send message to userId={}", userId, e);
            }
        } else {
            log.debug("No active session for userId={}", userId);
        }
    }

    private String getUserIdFromSession(WebSocketSession session) {
        // Extract userId from session attributes (set by interceptor)
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }
}
