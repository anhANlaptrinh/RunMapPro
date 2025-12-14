package com.example.runmapproapp.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.runmapproapp.data.model.Conversation;
import com.example.runmapproapp.data.model.User;
import com.example.runmapproapp.ui.profile.UserListAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewChatActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private RecyclerView recyclerViewUsers;
    private UserListAdapter userAdapter;
    private ChatApi chatApi;
    private UserApi userApi;
    private AuthManager authManager;
    private List<User> allUsers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        chatApi = ApiClient.getChatApi();
        userApi = ApiClient.getUserApi();
        authManager = new AuthManager(this);

        setupToolbar();
        setupViews();
        loadUsers();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("New Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        etSearch = findViewById(R.id.etSearch);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);

        userAdapter = new UserListAdapter(new ArrayList<>(), this::createDirectChat);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        userApi.searchUsers("").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();
                    // Remove current user from list
                    String currentUserId = authManager.getUserId();
                    allUsers.removeIf(user -> user.getId().equals(currentUserId));
                    userAdapter.updateUsers(allUsers);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(NewChatActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        if (query.isEmpty()) {
            userAdapter.updateUsers(allUsers);
            return;
        }

        List<User> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (User user : allUsers) {
            if (user.getFullName().toLowerCase().contains(lowerQuery) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) {
                filtered.add(user);
            }
        }
        userAdapter.updateUsers(filtered);
    }

    private void createDirectChat(User user) {
        ChatApi.CreateDirectChatRequest request = new ChatApi.CreateDirectChatRequest(user.getId());

        chatApi.createDirectChat(request).enqueue(new Callback<Conversation>() {
            @Override
            public void onResponse(Call<Conversation> call, Response<Conversation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Conversation conversation = response.body();
                    Intent intent = new Intent(NewChatActivity.this, ChatActivity.class);
                    intent.putExtra("conversationId", conversation.getId());
                    intent.putExtra("conversationName", user.getFullName());
                    intent.putExtra("isGroup", false);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(NewChatActivity.this, "Failed to create chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Conversation> call, Throwable t) {
                Toast.makeText(NewChatActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
