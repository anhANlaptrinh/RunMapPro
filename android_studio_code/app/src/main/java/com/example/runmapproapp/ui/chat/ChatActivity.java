package com.example.runmapproapp.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.ChatApi;
import com.example.runmapproapp.data.UserApi;
import com.example.runmapproapp.data.model.ChatMessage;
import com.example.runmapproapp.data.model.User;
import com.example.runmapproapp.data.model.UserProfileResponse;
import com.example.runmapproapp.data.websocket.WebSocketEvent;
import com.example.runmapproapp.data.websocket.WebSocketManager;
import com.example.runmapproapp.data.websocket.WebSocketService;
import com.example.runmapproapp.ui.chat.ChatMessageAdapter;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements WebSocketService.WebSocketEventListener {

    private static final String TAG = "ChatActivity";
    private RecyclerView recyclerViewMessages;
    private TextInputEditText etMessage;
    private MaterialButton btnSend;
    private ShapeableImageView ivChatAvatar;
    private TextView tvChatName;
    private TextView tvChatStatus;
    private ChatMessageAdapter messageAdapter;
    private ChatApi chatApi;
    private UserApi userApi;
    private AuthManager authManager;
    private WebSocketManager webSocketManager;
    private String conversationId;
    private String conversationName;
    private boolean isGroup;
    private String currentUserId;
    private String otherUserId;
    private Map<String, User> userCache = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatApi = ApiClient.getChatApi();
        userApi = ApiClient.getUserApi();
        authManager = new AuthManager(this);
        currentUserId = authManager.getUserId();
        
        // Initialize WebSocket
        webSocketManager = WebSocketManager.getInstance(this);
        webSocketManager.addListener(this);
        webSocketManager.connect();

        conversationId = getIntent().getStringExtra("conversationId");
        conversationName = getIntent().getStringExtra("conversationName");
        isGroup = getIntent().getBooleanExtra("isGroup", false);
        otherUserId = getIntent().getStringExtra("otherUserId");

        setupToolbar();
        setupViews();
        loadChatInfo();
        loadMessages();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        ivChatAvatar = findViewById(R.id.ivChatAvatar);
        tvChatName = findViewById(R.id.tvChatName);
        tvChatStatus = findViewById(R.id.tvChatStatus);
    }
    
    private void loadChatInfo() {
        if (isGroup) {
            tvChatName.setText(conversationName != null ? conversationName : "Group Chat");
            ivChatAvatar.setImageResource(R.drawable.ic_group);
        } else if (otherUserId != null) {
            userApi.getUserById(otherUserId).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfileResponse user = response.body();
                        tvChatName.setText(user.getFullName());
                        
                        String avatarUrl = user.getAvatarUrl();
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            if (!avatarUrl.startsWith("http")) {
                                avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                            }
                            Glide.with(ChatActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(ivChatAvatar);
                        } else {
                            ivChatAvatar.setImageResource(R.drawable.ic_person);
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                    tvChatName.setText(conversationName != null ? conversationName : "Chat");
                    ivChatAvatar.setImageResource(R.drawable.ic_person);
                }
            });
        } else {
            tvChatName.setText(conversationName != null ? conversationName : "Chat");
            ivChatAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    private void setupViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageAdapter = new ChatMessageAdapter(new ArrayList<>(), currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        chatApi.getMessages(conversationId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessage> messages = response.body();
                    enrichMessagesWithUserInfo(messages);
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrichMessagesWithUserInfo(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            updateMessages(messages);
            return;
        }

        // Mark messages as mine
        for (ChatMessage message : messages) {
            message.setMine(message.getSenderId().equals(currentUserId));
        }

        // If not a group chat, no need to fetch sender names
        if (!isGroup) {
            updateMessages(messages);
            return;
        }

        // Collect unique sender IDs
        Map<String, Boolean> senderIds = new HashMap<>();
        for (ChatMessage message : messages) {
            if (!message.isMine()) {
                senderIds.put(message.getSenderId(), true);
            }
        }

        if (senderIds.isEmpty()) {
            updateMessages(messages);
            return;
        }

        // Fetch user info for senders
        final int[] pendingRequests = {senderIds.size()};
        for (String senderId : senderIds.keySet()) {
            if (userCache.containsKey(senderId)) {
                User cachedUser = userCache.get(senderId);
                for (ChatMessage message : messages) {
                    if (message.getSenderId().equals(senderId)) {
                        message.setSenderName(cachedUser.getFullName());
                        message.setSenderAvatarUrl(cachedUser.getAvatarUrl());
                    }
                }
                pendingRequests[0]--;
                if (pendingRequests[0] == 0) {
                    updateMessages(messages);
                }
                continue;
            }

            userApi.getUserById(senderId).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfileResponse user = response.body();
                        User cachedUser = new User();
                        cachedUser.setId(user.getId());
                        cachedUser.setFullName(user.getFullName());
                        cachedUser.setAvatarUrl(user.getAvatarUrl());
                        userCache.put(senderId, cachedUser);
                        for (ChatMessage message : messages) {
                            if (message.getSenderId().equals(senderId)) {
                                message.setSenderName(cachedUser.getFullName());
                                message.setSenderAvatarUrl(cachedUser.getAvatarUrl());
                            }
                        }
                    }

                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        updateMessages(messages);
                    }
                }

                @Override
                public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        updateMessages(messages);
                    }
                }
            });
        }
    }

    private void updateMessages(List<ChatMessage> messages) {
        messageAdapter.updateMessages(messages);
        if (!messages.isEmpty()) {
            recyclerViewMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void sendMessage() {
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) {
            return;
        }

        ChatApi.SendMessageRequest request = new ChatApi.SendMessageRequest(conversationId, text);
        btnSend.setEnabled(false);

        chatApi.sendMessage(request).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                btnSend.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    etMessage.setText("");
                    ChatMessage newMessage = response.body();
                    newMessage.setMine(true);
                    messageAdapter.addMessage(newMessage);
                    recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                btnSend.setEnabled(true);
                Toast.makeText(ChatActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketManager.removeListener(this);
        webSocketManager.disconnect();
    }
    
    // WebSocket event handlers
    @Override
    public void onConnected() {
        Log.d(TAG, "WebSocket connected for conversation: " + conversationId);
        runOnUiThread(() -> {
            tvChatStatus.setText("Online");
        });
    }
    
    @Override
    public void onDisconnected() {
        Log.d(TAG, "WebSocket disconnected");
        runOnUiThread(() -> {
            tvChatStatus.setText("Offline");
        });
    }
    
    @Override
    public void onEvent(WebSocketEvent event) {
        Log.d(TAG, "onEvent: received WebSocket event, type=" + (event != null ? event.getType() : "null"));
        handleWebSocketEvent(event);
    }
    
    @Override
    public void onError(Throwable error) {
        Log.e(TAG, "WebSocket error", error);
    }
    
    private void handleWebSocketEvent(WebSocketEvent event) {
        if (event == null || event.getType() == null) {
            Log.w(TAG, "handleWebSocketEvent: event or type is null");
            return;
        }
        
        Log.d(TAG, "handleWebSocketEvent: type=" + event.getType() + 
              ", conversationId=" + event.getConversationId() + 
              ", currentConversationId=" + conversationId);
        
        // Only process events for this conversation
        if (!conversationId.equals(event.getConversationId())) {
            Log.d(TAG, "handleWebSocketEvent: ignoring event for different conversation");
            return;
        }
        
        switch (event.getType()) {
            case "message":
                Log.d(TAG, "handleWebSocketEvent: processing message event");
                handleNewMessage(event);
                break;
            case "typing":
                handleTypingIndicator(event);
                break;
            case "read":
                handleReadReceipt(event);
                break;
            default:
                Log.w(TAG, "handleWebSocketEvent: unknown event type: " + event.getType());
        }
    }
    
    private void handleNewMessage(WebSocketEvent event) {
        WebSocketEvent.MessageData messageData = event.getMessage();
        if (messageData == null) {
            Log.w(TAG, "handleNewMessage: messageData is null");
            return;
        }
        
        Log.d(TAG, "handleNewMessage: received message from " + messageData.getSenderId() + 
              ", conversationId=" + messageData.getConversationId() + 
              ", text=" + messageData.getText());
        
        // Don't add our own messages (already added when sent)
        if (messageData.getSenderId().equals(currentUserId)) {
            Log.d(TAG, "handleNewMessage: ignoring own message");
            return;
        }
        
        runOnUiThread(() -> {
            ChatMessage message = new ChatMessage();
            message.setId(messageData.getId());
            message.setConversationId(messageData.getConversationId());
            message.setSenderId(messageData.getSenderId());
            message.setText(messageData.getText());
            message.setCreatedAt(messageData.getCreatedAt());
            message.setSenderName(messageData.getSenderName());
            message.setSenderAvatarUrl(messageData.getSenderAvatarUrl());
            message.setMine(false);
            
            messageAdapter.addMessage(message);
            recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            
            Log.d(TAG, "Received realtime message: " + message.getText() + ", total messages: " + messageAdapter.getItemCount());
        });
    }
    
    private void handleTypingIndicator(WebSocketEvent event) {
        // TODO: Show/hide typing indicator
        Log.d(TAG, "User " + event.getUserId() + " is typing: " + event.getIsTyping());
    }
    
    private void handleReadReceipt(WebSocketEvent event) {
        // TODO: Update message read status
        Log.d(TAG, "Messages read by user: " + event.getUserId());
    }
}
