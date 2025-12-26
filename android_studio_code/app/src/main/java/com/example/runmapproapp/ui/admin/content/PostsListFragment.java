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
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.data.response.MessageResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private PostsAdminAdapter adapter;
    private AdminApi adminApi;
    private PostApi postApi;
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
        postApi = ApiClient.getClient().create(PostApi.class);

        bindViews(view);
        setupRecyclerView();
        loadPosts();
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvEmptyState.setText(R.string.no_posts_found);
    }

    private void setupRecyclerView() {
        adapter = new PostsAdminAdapter(requireContext(), post -> showDeleteDialog(post));
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadPosts() {
        setLoading(true);

        postApi.getFeed(0, 100).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body();
                    adapter.setPosts(posts);
                    tvEmptyState.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    showError(getString(R.string.failed_load_posts));
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error_message));
            }
        });
    }

    private void showDeleteDialog(Post post) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_post_title)
                .setMessage(R.string.delete_post_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> performDeletePost(post))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performDeletePost(Post post) {
        adminApi.deletePost(post.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), R.string.post_deleted_success, Toast.LENGTH_SHORT).show();
                    loadPosts(); // Reload list
                } else {
                    showError(getString(R.string.failed_delete_post));
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
