package com.example.runmapproapp.ui.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.Comment;
import com.example.runmapproapp.data.model.CreateCommentRequest;
import com.example.runmapproapp.ui.social.adapter.CommentAdapter;
import com.example.runmapproapp.ui.profile.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends AppCompatActivity implements CommentAdapter.OnCommentInteractionListener {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private EditText etComment;
    private Button btnSendComment;
    private ProgressBar progressBar;

    private PostApi postApi;
    private String postId;
    private String replyToCommentId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.comments_title);
        }

        postId = getIntent().getStringExtra("POST_ID");
        if (postId == null) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupListeners();

        postApi = ApiClient.getPostApi();
        loadComments();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);
    }

    private void setupListeners() {
        btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);

        postApi.getComments(postId, 0, 50).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> sortedComments = sortCommentsWithReplies(response.body());
                    commentAdapter.setComments(sortedComments);
                } else {
                    Toast.makeText(CommentsActivity.this, R.string.failed_to_load_comments, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CommentsActivity.this, getString(R.string.error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Comment> sortCommentsWithReplies(List<Comment> comments) {
        List<Comment> result = new ArrayList<>();
        
        // First, add all parent comments (no parentCommentId)
        for (Comment comment : comments) {
            if (comment.getParentCommentId() == null) {
                result.add(comment);
                addRepliesRecursively(comments, comment.getId(), result);
            }
        }
        
        return result;
    }
    
    private void addRepliesRecursively(List<Comment> allComments, String parentId, List<Comment> result) {
        // Add all direct replies to this parent
        for (Comment comment : allComments) {
            if (comment.getParentCommentId() != null && 
                comment.getParentCommentId().equals(parentId)) {
                result.add(comment);
                // Recursively add replies to this comment
                addRepliesRecursively(allComments, comment.getId(), result);
            }
        }
    }

    private void sendComment() {
        String commentText = etComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_comment, Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendComment.setEnabled(false);

        CreateCommentRequest request = new CreateCommentRequest(postId, commentText, replyToCommentId);

        postApi.addComment(postId, request).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                btnSendComment.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    etComment.setText("");
                    replyToCommentId = null;
                    loadComments(); // Reload to show new comment
                    Toast.makeText(CommentsActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CommentsActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                btnSendComment.setEnabled(true);
                Toast.makeText(CommentsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReplyClick(Comment comment) {
        replyToCommentId = comment.getId();
        etComment.setHint("Reply to comment...");
        etComment.requestFocus();
    }

    @Override
    public void onAuthorClick(String authorId) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("USER_ID", authorId);
        startActivity(intent);
    }

    @Override
    public void onLikeClick(Comment comment) {
        PostApi postApi = ApiClient.getPostApi();
        Call<Comment> call;
        
        if (comment.isLikedByCurrentUser()) {
            // Unlike
            call = postApi.unlikeComment(comment.getId());
        } else {
            // Like
            call = postApi.likeComment(comment.getId());
        }
        
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Reload comments to update UI
                    loadComments();
                } else {
                    Toast.makeText(CommentsActivity.this, R.string.failed_to_update_like, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                Toast.makeText(CommentsActivity.this, getString(R.string.error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditComment(Comment comment) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_comment, null);
        android.widget.EditText etEditComment = dialogView.findViewById(R.id.etEditComment);
        etEditComment.setText(comment.getContentText());

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.edit_comment)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String newText = etEditComment.getText().toString().trim();
                if (newText.isEmpty()) {
                    Toast.makeText(this, R.string.empty_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updateComment(comment, newText);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void updateComment(Comment comment, String newText) {
        PostApi postApi = ApiClient.getPostApi();
        CreateCommentRequest request = new CreateCommentRequest(
            comment.getPostId(),
            newText,
            comment.getParentCommentId()
        );
        
        postApi.updateComment(comment.getId(), request).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int position = commentAdapter.findCommentPosition(comment.getId());
                    if (position != -1) {
                        commentAdapter.updateComment(position, response.body());
                    }
                    Toast.makeText(CommentsActivity.this, R.string.comment_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CommentsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                Toast.makeText(CommentsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteComment(Comment comment) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Comment")
            .setMessage(R.string.delete_comment_confirm)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                PostApi postApi = ApiClient.getPostApi();
                postApi.deleteComment(comment.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CommentsActivity.this, "Comment deleted", Toast.LENGTH_SHORT).show();
                            loadComments();
                        } else {
                            Toast.makeText(CommentsActivity.this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(CommentsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
