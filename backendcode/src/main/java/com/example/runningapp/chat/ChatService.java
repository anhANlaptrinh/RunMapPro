package com.example.runningapp.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.runningapp.chat.dto.CreateDirectChatRequest;
import com.example.runningapp.chat.dto.CreateGroupChatRequest;
import com.example.runningapp.chat.dto.SendMessageRequest;
import com.example.runningapp.chat.dto.WebSocketMessageEvent;
import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.BadRequestException;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.common.exception.UnauthorizedException;
import com.example.runningapp.user.User;
import com.example.runningapp.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final WebSocketSessionManager webSocketSessionManager;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ChatService(ConversationRepository conversationRepository, 
                      MessageRepository messageRepository,
                      WebSocketSessionManager webSocketSessionManager,
                      UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.webSocketSessionManager = webSocketSessionManager;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
    }

    public Conversation createDirectChat(CreateDirectChatRequest request) {
        String currentUserId = requireCurrentUser();
        if (currentUserId.equals(request.otherUserId())) {
            throw new BadRequestException("Cannot create a direct chat with yourself");
        }
        List<String> members = List.of(currentUserId, request.otherUserId());
        return conversationRepository.findByTypeAndMembers("direct", members)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .type("direct")
                        .members(members)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()));
    }

    public Conversation createGroupChat(CreateGroupChatRequest request) {
        String currentUserId = requireCurrentUser();
        Set<String> members = new HashSet<>(request.memberIds() == null ? List.of() : request.memberIds());
        members.add(currentUserId);
        if (members.size() < 2) {
            throw new BadRequestException("A group needs at least two members");
        }
        return conversationRepository.save(Conversation.builder()
                .type("group")
                .members(new ArrayList<>(members))
                .groupName(request.groupName())
                .groupAvatarUrl(request.groupAvatarUrl())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    public Message sendMessage(SendMessageRequest request) {
        String currentUserId = requireCurrentUser();
        Conversation conversation = conversationRepository.findById(request.conversationId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        if (!conversation.getMembers().contains(currentUserId)) {
            throw new BadRequestException("You are not a member of this conversation");
        }
        Message message = Message.builder()
                .conversationId(conversation.getId())
                .senderId(currentUserId)
                .text(request.text())
            .mediaIds(request.mediaIds() == null ? List.of() : request.mediaIds())
                .readBy(new ArrayList<>(List.of(currentUserId)))
                .createdAt(Instant.now())
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessageAt(message.getCreatedAt());
        conversation.setUpdatedAt(message.getCreatedAt());
        conversation.setLastMessageText(buildPreview(message));
        conversationRepository.save(conversation);

        // Broadcast to all members via WebSocket
        try {
            // Get sender info
            User sender = userRepository.findById(currentUserId).orElse(null);
            String senderName = sender != null ? sender.getFullName() : "Unknown";
            String senderAvatarUrl = sender != null && sender.getAvatarMediaId() != null 
                ? "/api/media/" + sender.getAvatarMediaId() 
                : null;
            
            WebSocketMessageEvent event = WebSocketMessageEvent.fromMessage(message, senderName, senderAvatarUrl);
            String jsonMessage = objectMapper.writeValueAsString(event);
            
            for (String memberId : conversation.getMembers()) {
                webSocketSessionManager.sendMessageToUser(memberId, jsonMessage);
            }
            log.info("Message broadcast to {} members of conversation {}", 
                    conversation.getMembers().size(), conversation.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast message via WebSocket", e);
        }

        return message;
    }

    public List<Conversation> myConversations() {
        String currentUserId = requireCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "lastMessageAt", "updatedAt");
        return conversationRepository.findByMembersContaining(currentUserId, sort);
    }

    public List<Message> getMessages(String conversationId) {
        String currentUserId = requireCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        if (!conversation.getMembers().contains(currentUserId)) {
            throw new BadRequestException("Access denied to conversation");
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public void markMessagesAsRead(String conversationId, String userId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        for (Message message : messages) {
            if (!message.getReadBy().contains(userId)) {
                message.getReadBy().add(userId);
            }
        }
        messageRepository.saveAll(messages);
    }

    private String buildPreview(Message message) {
        if (CollectionUtils.isEmpty(message.getMediaIds())) {
            return message.getText();
        }
        return message.getText() + " (" + message.getMediaIds().size() + " attachments)";
    }

    private String requireCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }
}
