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
import com.example.runmapproapp.data.model.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private final OnCommentInteractionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);

    public interface OnCommentInteractionListener {
        void onReplyClick(Comment comment);
        void onAuthorClick(String authorId);
        void onLikeClick(Comment comment);
        void onEditComment(Comment comment);
        void onDeleteComment(Comment comment);
    }

    public CommentAdapter(List<Comment> comments, OnCommentInteractionListener listener) {
        this.comments = comments;
        this.listener = listener;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public void updateComment(int position, Comment updatedComment) {
        if (position >= 0 && position < comments.size()) {
            comments.set(position, updatedComment);
            notifyItemChanged(position);
        }
    }

    public int findCommentPosition(String commentId) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAuthorAvatar;
        private final TextView tvAuthorName;
        private final TextView tvTimestamp;
        private final TextView tvContent;
        private final ImageButton btnLikeComment;
        private final ImageButton btnMenuComment;
        private final TextView tvReply;
        private final TextView tvLikeCount;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnLikeComment = itemView.findViewById(R.id.btnLikeComment);
            btnMenuComment = itemView.findViewById(R.id.btnMenuComment);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
        }

        public void bind(Comment comment) {
            // Author name
            String authorName = comment.getAuthorName();
            if (authorName == null || authorName.isEmpty()) {
                authorName = "User " + comment.getAuthorId().substring(0, Math.min(8, comment.getAuthorId().length()));
            }
            tvAuthorName.setText(authorName);
            
            // Author avatar
            String avatarUrl = comment.getAuthorAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                // Convert relative path to full URL
                if (avatarUrl.startsWith("/api/")) {
                    avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                }
                Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(ivAuthorAvatar);
            } else {
                ivAuthorAvatar.setImageResource(R.drawable.ic_person);
            }
            
            // Timestamp
            if (comment.getCreatedAt() != null) {
                tvTimestamp.setText(formatDate(comment.getCreatedAt()));
            }
            
            // Content
            tvContent.setText(comment.getContentText());
            
            // Like count
            tvLikeCount.setText(String.valueOf(comment.getLikeCount()) + " likes");
            
            // Set like icon based on liked status
            if (comment.isLikedByCurrentUser()) {
                btnLikeComment.setImageResource(R.drawable.ic_favorite_filled);
                btnLikeComment.setColorFilter(itemView.getContext().getColor(R.color.pink));
            } else {
                btnLikeComment.setImageResource(R.drawable.ic_favorite_border);
                btnLikeComment.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
            }
            
            // Show menu button only if current user is the author
            AuthManager authManager = new AuthManager(itemView.getContext());
            String currentUserId = authManager.getUserId();
            if (currentUserId != null && currentUserId.equals(comment.getAuthorId())) {
                btnMenuComment.setVisibility(View.VISIBLE);
                btnMenuComment.setOnClickListener(v -> showCommentMenu(v, comment));
            } else {
                btnMenuComment.setVisibility(View.GONE);
            }
            
            // Indent if reply
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
            if (comment.getParentCommentId() != null) {
                params.setMarginStart(48); // Indent replies
            } else {
                params.setMarginStart(0);
            }
            
            // Click listeners
            ivAuthorAvatar.setOnClickListener(v -> listener.onAuthorClick(comment.getAuthorId()));
            tvAuthorName.setOnClickListener(v -> listener.onAuthorClick(comment.getAuthorId()));
            btnLikeComment.setOnClickListener(v -> listener.onLikeClick(comment));
            tvReply.setOnClickListener(v -> listener.onReplyClick(comment));
        }
        
        private void showCommentMenu(View view, Comment comment) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.comment_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.action_edit_comment) {
                        listener.onEditComment(comment);
                        return true;
                    } else if (id == R.id.action_delete_comment) {
                        listener.onDeleteComment(comment);
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
