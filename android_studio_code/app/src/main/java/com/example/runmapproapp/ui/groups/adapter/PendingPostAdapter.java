package com.example.runmapproapp.ui.groups.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.model.GroupPost;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PendingPostAdapter extends RecyclerView.Adapter<PendingPostAdapter.PendingPostViewHolder> {

    private List<GroupPost> posts;
    private final OnPendingPostActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private AuthManager authManager;

    public interface OnPendingPostActionListener {
        void onApprove(GroupPost post, int position);
        void onReject(GroupPost post, int position);
    }

    public PendingPostAdapter(List<GroupPost> posts, OnPendingPostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }
    
    public void setAuthManager(AuthManager authManager) {
        this.authManager = authManager;
    }

    public void setPosts(List<GroupPost> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void removePost(int position) {
        if (position >= 0 && position < posts.size()) {
            posts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, posts.size());
        }
    }

    @NonNull
    @Override
    public PendingPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_post, parent, false);
        return new PendingPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingPostViewHolder holder, int position) {
        GroupPost post = posts.get(position);
        holder.bind(post, position);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PendingPostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAuthorAvatar;
        private final TextView tvAuthorName;
        private final TextView tvTimestamp;
        private final TextView tvContent;
        private final ImageView ivPostImage;
        private final MaterialButton btnApprove;
        private final MaterialButton btnReject;

        public PendingPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(GroupPost post, int position) {
            // Author name
            tvAuthorName.setText(post.getAuthorName() != null ? post.getAuthorName() : "Unknown");

            // Timestamp
            if (post.getCreatedAt() != null && !post.getCreatedAt().isEmpty()) {
                try {
                    // Parse ISO 8601 format
                    Instant instant = Instant.parse(post.getCreatedAt());
                    Date date = Date.from(instant);
                    tvTimestamp.setText(dateFormat.format(date));
                } catch (Exception e) {
                    tvTimestamp.setText(post.getCreatedAt());
                }
            }

            // Content
            if (post.getContent() != null && !post.getContent().isEmpty()) {
                tvContent.setText(post.getContent());
                tvContent.setVisibility(View.VISIBLE);
            } else {
                tvContent.setVisibility(View.GONE);
            }

            // Author avatar
            if (post.getAuthorAvatar() != null && !post.getAuthorAvatar().isEmpty()) {
                String avatarUrl = "http://10.0.2.2:8080/api/media/" + post.getAuthorAvatar();
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivAuthorAvatar);
            } else {
                ivAuthorAvatar.setImageResource(R.drawable.ic_person);
            }

            // Post image or run card
            if (post.getRunId() != null && !post.getRunId().isEmpty()) {
                // For run posts, show run icon instead of trying to load minimap
                // (minimap endpoint may not be available in pending posts context)
                ivPostImage.setVisibility(View.VISIBLE);
                ivPostImage.setImageResource(R.drawable.ic_directions_run);
                android.util.Log.d("PendingPostAdapter", "Run post detected, runId: " + post.getRunId());
            } else if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
                // Show uploaded media
                ivPostImage.setVisibility(View.VISIBLE);
                String mediaUrl = "http://10.0.2.2:8080/api/media/" + post.getMediaUrls().get(0);
                Glide.with(itemView.getContext())
                        .load(mediaUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_person)
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }

            // Action buttons
            btnApprove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(post, position);
                }
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(post, position);
                }
            });
        }
    }
}
