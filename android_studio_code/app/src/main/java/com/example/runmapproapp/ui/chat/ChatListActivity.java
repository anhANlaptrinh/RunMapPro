package com.example.runmapproapp.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.ChatApi;
import com.example.runmapproapp.data.UserApi;
import com.example.runmapproapp.data.model.Conversation;
import com.example.runmapproapp.data.model.User;
import com.example.runmapproapp.data.model.UserProfileResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.runmapproapp.utils.BottomNavigationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private SwipeRefreshLayout swipeRefresh;
    private ConversationAdapter conversationAdapter;
    private ChatApi chatApi;
    private UserApi userApi;
    private AuthManager authManager;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatApi = ApiClient.getChatApi();
        userApi = ApiClient.getUserApi();
        authManager = new AuthManager(this);
        currentUserId = authManager.getUserId();

        setupToolbar();
        setupViews();
        
        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_chat);
        
        loadConversations();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupViews() {
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);

        conversationAdapter = new ConversationAdapter(new ArrayList<>(), conversation -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("conversationId", conversation.getId());
            intent.putExtra("conversationName", conversation.getDisplayName());
            intent.putExtra("isGroup", conversation.isGroupChat());
            
            // Get other user ID for direct chat
            if (!conversation.isGroupChat() && conversation.getMembers() != null) {
                for (String memberId : conversation.getMembers()) {
                    if (!memberId.equals(currentUserId)) {
                        intent.putExtra("otherUserId", memberId);
                        break;
                    }
                }
            }
            
            startActivity(intent);
        });

        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChats.setAdapter(conversationAdapter);

        swipeRefresh.setOnRefreshListener(this::loadConversations);

        fabNewChat.setOnClickListener(v -> {
            // Open user selection dialog or new chat screen
            Intent intent = new Intent(ChatListActivity.this, NewChatActivity.class);
            startActivity(intent);
        });
    }

    private void loadConversations() {
        swipeRefresh.setRefreshing(true);

        chatApi.getMyConversations().enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Conversation> conversations = response.body();
                    enrichConversationsWithUserInfo(conversations);
                } else {
                    Toast.makeText(ChatListActivity.this, "Failed to load conversations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ChatListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrichConversationsWithUserInfo(List<Conversation> conversations) {
        if (conversations.isEmpty()) {
            conversationAdapter.updateConversations(conversations);
            return;
        }

        // Collect all user IDs to fetch
        Map<String, Conversation> directChatsMap = new HashMap<>();
        for (Conversation conversation : conversations) {
            if ("direct".equals(conversation.getType())) {
                for (String memberId : conversation.getMembers()) {
                    if (!memberId.equals(currentUserId)) {
                        directChatsMap.put(memberId, conversation);
                        break;
                    }
                }
            }
        }

        // Fetch user info for direct chats
        final int[] pendingRequests = {directChatsMap.size()};
        if (pendingRequests[0] == 0) {
            conversationAdapter.updateConversations(conversations);
            return;
        }

        for (Map.Entry<String, Conversation> entry : directChatsMap.entrySet()) {
            String userId = entry.getKey();
            Conversation conversation = entry.getValue();

            userApi.getUserById(userId).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfileResponse user = response.body();
                        conversation.setOtherUserName(user.getFullName());
                        conversation.setOtherUserAvatarUrl(user.getAvatarUrl());
                    }

                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        conversationAdapter.updateConversations(conversations);
                    }
                }

                @Override
                public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        conversationAdapter.updateConversations(conversations);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_chat);
    }
}
