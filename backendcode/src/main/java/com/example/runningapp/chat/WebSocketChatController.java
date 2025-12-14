package com.example.runningapp.chat;

import java.time.Instant;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.runningapp.chat.dto.SendMessageRequest;
import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.BadRequestException;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.common.exception.UnauthorizedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    public WebSocketChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate,
                                   ConversationRepository conversationRepository) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.conversationRepository = conversationRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                log.warn("Unauthenticated user trying to send message");
                return;
            }

            // Validate conversation access
            Conversation conversation = conversationRepository.findById(request.conversationId())
                    .orElseThrow(() -> new NotFoundException("Conversation not found"));
            
            if (!conversation.getMembers().contains(currentUserId)) {
                log.warn("User {} trying to send message to conversation they're not member of: {}", 
                         currentUserId, request.conversationId());
                return;
            }

            // Send message via service
            Message message = chatService.sendMessage(request);

            // Broadcast to all members of the conversation
            for (String memberId : conversation.getMembers()) {
                messagingTemplate.convertAndSendToUser(
                    memberId,
                    "/queue/messages",
                    message
                );
            }

            log.info("Message sent from {} to conversation {}", currentUserId, conversation.getId());
            
        } catch (Exception e) {
            log.error("Error sending message via WebSocket", e);
        }
    }

    @MessageMapping("/chat.markRead")
    public void markAsRead(@Payload MarkReadRequest request) {
        try {
            String currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return;
            }

            // Mark messages as read
            chatService.markMessagesAsRead(request.conversationId(), currentUserId);

            // Notify other members
            Conversation conversation = conversationRepository.findById(request.conversationId())
                    .orElse(null);
            
            if (conversation != null) {
                for (String memberId : conversation.getMembers()) {
                    if (!memberId.equals(currentUserId)) {
                        messagingTemplate.convertAndSendToUser(
                            memberId,
                            "/queue/read-receipts",
                            new ReadReceiptNotification(request.conversationId(), currentUserId, Instant.now())
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
        }
    }

    // DTOs for WebSocket messages
    record MarkReadRequest(String conversationId) {}
    record ReadReceiptNotification(String conversationId, String userId, Instant readAt) {}
}
