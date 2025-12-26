package com.example.runmapproapp.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.AdminApi;
import com.example.runmapproapp.data.model.User;
import com.example.runmapproapp.data.response.MessageResponse;
import com.example.runmapproapp.ui.admin.adapter.UsersAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersManagementFragment extends Fragment implements UsersAdapter.OnUserActionListener {

    private RecyclerView recyclerUsers;
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private UsersAdapter adapter;
    private AdminApi adminApi;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(requireContext());
        adminApi = ApiClient.getClient().create(AdminApi.class);

        bindViews(view);
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }

    private void bindViews(View view) {
        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        adapter = new UsersAdapter(requireContext(), this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        setLoading(true);

        adminApi.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    adapter.setUsers(users);
                    tvEmptyState.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    showError(getString(R.string.failed_load_users));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error_message));
            }
        });
    }

    @Override
    public void onBanUser(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.ban_user)
                .setMessage(getString(R.string.ban_user_confirm, user.getUsername()))
                .setPositiveButton(R.string.ban, (dialog, which) -> performBan(user))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onUnbanUser(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.unban_user)
                .setMessage(getString(R.string.unban_user_confirm, user.getUsername()))
                .setPositiveButton(R.string.unban, (dialog, which) -> performUnban(user))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Removed view-profile action for admin per request

    private void performBan(User user) {
        adminApi.banUser(user.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), R.string.user_banned_success, Toast.LENGTH_SHORT).show();
                    loadUsers(); // Reload list
                } else {
                    showError(getString(R.string.failed_ban_user));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showError(getString(R.string.network_error_message));
            }
        });
    }

    private void performUnban(User user) {
        adminApi.unbanUser(user.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), R.string.user_unbanned_success, Toast.LENGTH_SHORT).show();
                    loadUsers(); // Reload list
                } else {
                    showError(getString(R.string.failed_unban_user));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showError(getString(R.string.network_error_message));
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerUsers.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
