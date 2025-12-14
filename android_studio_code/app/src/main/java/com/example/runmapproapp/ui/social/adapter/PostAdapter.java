package com.example.runmapproapp.ui.social.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.model.Post;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private final OnPostInteractionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);

    public interface OnPostInteractionListener {
        void onPostClick(Post post);
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post);
        void onShareClick(Post post);
        void onAuthorClick(String authorId);
        void onEditPost(Post post, int position);
        void onDeletePost(Post post, int position);
    }

    public PostAdapter(List<Post> posts, OnPostInteractionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void addPosts(List<Post> newPosts) {
        int startPosition = posts.size();
        posts.addAll(newPosts);
        notifyItemRangeInserted(startPosition, newPosts.size());
    }

    public void updatePost(int position, Post updatedPost) {
        if (position >= 0 && position < posts.size()) {
            posts.set(position, updatedPost);
            notifyItemChanged(position);
        }
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
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, position);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAuthorAvatar;
        private final TextView tvAuthorName;
        private final TextView tvTimestamp;
        private final TextView tvContent;
        private final MaterialCardView cardOriginalPost;
        private final TextView tvOriginalAuthor;
        private final TextView tvOriginalContent;
        private final ImageView ivOriginalPostImage;
        private final ImageView ivPostImage;
        private final ImageButton btnLike;
        private final ImageButton btnComment;
        private final ImageButton btnShare;
        private final ImageButton btnMenu;
        private final TextView tvLikeCount;
        private final TextView tvCommentCount;
        private final TextView tvShareCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            cardOriginalPost = itemView.findViewById(R.id.cardOriginalPost);
            tvOriginalAuthor = itemView.findViewById(R.id.tvOriginalAuthor);
            tvOriginalContent = itemView.findViewById(R.id.tvOriginalContent);
            ivOriginalPostImage = itemView.findViewById(R.id.ivOriginalPostImage);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvShareCount = itemView.findViewById(R.id.tvShareCount);
        }

        public void bind(Post post, int position) {
            // Author name - use authorName if available, fallback to userId
            String authorDisplayName = post.getAuthorName();
            if (authorDisplayName == null || authorDisplayName.isEmpty()) {
                if (post.getAuthorId() != null && post.getAuthorId().length() >= 8) {
                    authorDisplayName = "User " + post.getAuthorId().substring(0, 8);
                } else {
                    authorDisplayName = "User";
                }
            }
            tvAuthorName.setText(authorDisplayName);
            
            // Load author avatar if available
            if (post.getAuthorAvatar() != null && !post.getAuthorAvatar().isEmpty()) {
                String avatarUrl = post.getAuthorAvatar();
                // Convert relative path to full URL
                if (avatarUrl.startsWith("/api/")) {
                    avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                }
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivAuthorAvatar);
            } else {
                ivAuthorAvatar.setImageResource(R.drawable.ic_person);
            }
            
            // Timestamp
            if (post.getCreatedAt() != null) {
                tvTimestamp.setText(formatDate(post.getCreatedAt()));
            }
            
            // Content
            tvContent.setText(post.getContentText() != null ? post.getContentText() : "");
            tvContent.setVisibility(post.getContentText() != null && !post.getContentText().isEmpty() 
                ? View.VISIBLE : View.GONE);
            
            // Original post preview (for shared posts)
            if (post.getOriginalPost() != null) {
                cardOriginalPost.setVisibility(View.VISIBLE);
                Post originalPost = post.getOriginalPost();
                
                // Original author name
                String originalAuthorName = originalPost.getAuthorName();
                if (originalAuthorName == null || originalAuthorName.isEmpty()) {
                    if (originalPost.getAuthorId() != null && originalPost.getAuthorId().length() >= 8) {
                        originalAuthorName = "User " + originalPost.getAuthorId().substring(0, 8);
                    } else {
                        originalAuthorName = "User";
                    }
                }
                tvOriginalAuthor.setText(originalAuthorName);
                
                // Original content
                String originalContent = originalPost.getContentText();
                if (originalContent != null && !originalContent.isEmpty()) {
                    tvOriginalContent.setText(originalContent);
                    tvOriginalContent.setVisibility(View.VISIBLE);
                } else {
                    tvOriginalContent.setText("[No text content]");
                    tvOriginalContent.setVisibility(View.VISIBLE);
                }
                
                // Original post image
                if (originalPost.getMediaIds() != null && !originalPost.getMediaIds().isEmpty()) {
                    ivOriginalPostImage.setVisibility(View.VISIBLE);
                    String originalMediaUrl = "http://10.0.2.2:8080/api/media/" + originalPost.getMediaIds().get(0);
                    Glide.with(itemView.getContext())
                            .load(originalMediaUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_person)
                            .into(ivOriginalPostImage);
                } else {
                    ivOriginalPostImage.setVisibility(View.GONE);
                }
                
                // Click listener for original post - open original post detail
                cardOriginalPost.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPostClick(originalPost);
                    }
                });
            } else {
                cardOriginalPost.setVisibility(View.GONE);
            }
            
            // Post image (show first media if available)
            if (post.getMediaIds() != null && !post.getMediaIds().isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                String mediaUrl = "http://10.0.2.2:8080/api/media/" + post.getMediaIds().get(0);
                android.util.Log.d("PostAdapter", "Loading post image: " + mediaUrl);
                Glide.with(itemView.getContext())
                        .load(mediaUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_person)
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                android.util.Log.e("PostAdapter", "Failed to load post image: " + model, e);
                                if (e != null) {
                                    e.logRootCauses("PostAdapter");
                                }
                                return false;
                            }
                            
                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                android.util.Log.d("PostAdapter", "Successfully loaded post image: " + model);
                                return false;
                            }
                        })
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }
            
            // Counters
            tvLikeCount.setText(String.valueOf(post.getLikeCount()));
            tvCommentCount.setText(String.valueOf(post.getCommentCount()));
            tvShareCount.setText(String.valueOf(post.getShareCount()));
            
            // Like button state with color
            if (post.isLikedByCurrentUser()) {
                btnLike.setImageResource(R.drawable.ic_favorite_filled);
                btnLike.setColorFilter(itemView.getContext().getColor(R.color.pink));
            } else {
                btnLike.setImageResource(R.drawable.ic_favorite_border);
                btnLike.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
            }
            
            // Show menu button only if current user is the author
            AuthManager authManager = new AuthManager(itemView.getContext());
            String currentUserId = authManager.getUserId();
            if (currentUserId != null && currentUserId.equals(post.getAuthorId())) {
                btnMenu.setVisibility(View.VISIBLE);
                btnMenu.setOnClickListener(v -> showPostMenu(v, post, position));
            } else {
                btnMenu.setVisibility(View.GONE);
            }
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
            
            ivAuthorAvatar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAuthorClick(post.getAuthorId());
                }
            });
            tvAuthorName.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAuthorClick(post.getAuthorId());
                }
            });
            
            btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(post, position);
                }
            });
            btnComment.setOnClickListener(v -> listener.onCommentClick(post));
            btnShare.setOnClickListener(v -> listener.onShareClick(post));
        }
        
        private void showPostMenu(View view, Post post, int position) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.action_edit_post) {
                        listener.onEditPost(post, position);
                        return true;
                    } else if (id == R.id.action_delete_post) {
                        listener.onDeletePost(post, position);
                        return true;
                    }
                    return false;
                }
            });
            popup.show();
        }

        private String formatDate(Date date) {
            return dateFormat.format(date);
        }
    }
}
