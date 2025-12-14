package com.example.runningapp.chat.dto;

import com.example.runningapp.chat.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageEvent {
    private String type = "message";
    private String conversationId;
    private MessageData message;
    
    public static WebSocketMessageEvent fromMessage(Message message, String senderName, String senderAvatarUrl) {
        MessageData messageData = new MessageData(
            message.getId(),
            message.getConversationId(),
            message.getSenderId(),
            message.getText(),
            message.getCreatedAt().toString(),
            senderName,
            senderAvatarUrl
        );
        return new WebSocketMessageEvent("message", message.getConversationId(), messageData);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageData {
        private String id;
        private String conversationId;
        private String senderId;
        private String text;
        private String createdAt;
        private String senderName;
        private String senderAvatarUrl;
    }
}
