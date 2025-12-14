package com.example.runmapproapp.data;

import com.example.runmapproapp.data.model.ChatMessage;
import com.example.runmapproapp.data.model.Conversation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApi {

    @POST("/api/chat/create-direct")
    Call<Conversation> createDirectChat(@Body CreateDirectChatRequest request);

    @POST("/api/chat/create-group")
    Call<Conversation> createGroupChat(@Body CreateGroupChatRequest request);

    @POST("/api/chat/send")
    Call<ChatMessage> sendMessage(@Body SendMessageRequest request);

    @GET("/api/chat/my-conversations")
    Call<List<Conversation>> getMyConversations();

    @GET("/api/chat/messages/{conversationId}")
    Call<List<ChatMessage>> getMessages(@Path("conversationId") String conversationId);

    // Request DTOs
    class CreateDirectChatRequest {
        private String otherUserId;

        public CreateDirectChatRequest(String otherUserId) {
            this.otherUserId = otherUserId;
        }

        public String getOtherUserId() { return otherUserId; }
        public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }
    }

    class CreateGroupChatRequest {
        private String groupName;
        private String groupAvatarUrl;
        private List<String> memberIds;

        public CreateGroupChatRequest(String groupName, List<String> memberIds) {
            this.groupName = groupName;
            this.memberIds = memberIds;
        }

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }

        public String getGroupAvatarUrl() { return groupAvatarUrl; }
        public void setGroupAvatarUrl(String groupAvatarUrl) { this.groupAvatarUrl = groupAvatarUrl; }

        public List<String> getMemberIds() { return memberIds; }
        public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }
    }

    class SendMessageRequest {
        private String conversationId;
        private String text;
        private List<String> mediaIds;

        public SendMessageRequest(String conversationId, String text) {
            this.conversationId = conversationId;
            this.text = text;
        }

        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public List<String> getMediaIds() { return mediaIds; }
        public void setMediaIds(List<String> mediaIds) { this.mediaIds = mediaIds; }
    }
}
