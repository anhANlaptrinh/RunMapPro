package com.example.runmapproapp.ui.admin.content;

import android.os.Bundle;
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
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.model.Group;
import com.example.runmapproapp.data.response.MessageResponse;
import com.example.runmapproapp.ui.admin.adapter.GroupsAdminAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsListFragment extends Fragment implements GroupsAdminAdapter.OnGroupActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private GroupsAdminAdapter adapter;
    private AdminApi adminApi;
    private GroupApi groupApi;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(requireContext());
        adminApi = ApiClient.getClient().create(AdminApi.class);
        groupApi = ApiClient.getClient().create(GroupApi.class);

        bindViews(view);
        setupRecyclerView();
        loadGroups();
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvEmptyState.setText(R.string.no_groups_found);
    }

    private void setupRecyclerView() {
        adapter = new GroupsAdminAdapter(requireContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadGroups() {
        setLoading(true);

        // Use admin endpoint to get ALL groups (both public and private)
        adminApi.getAllGroups().enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Group> groups = response.body();
                    adapter.setGroups(groups);
                    tvEmptyState.setVisibility(groups.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    showError(getString(R.string.failed_load_groups));
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error_message));
            }
        });
    }

    @Override
    public void onDeleteGroup(Group group) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_group)
                .setMessage(getString(R.string.delete_group_confirm, group.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> performDeleteGroup(group))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performDeleteGroup(Group group) {
        adminApi.deleteGroup(group.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), R.string.group_deleted_success, Toast.LENGTH_SHORT).show();
                    loadGroups(); // Reload list
                } else {
                    showError(getString(R.string.failed_delete_group));
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
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
