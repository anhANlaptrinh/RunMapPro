package com.example.runmapproapp.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.model.Conversation;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation, listener);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView ivAvatar;
        private TextView tvName;
        private TextView tvLastMessage;
        private TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Conversation conversation, OnConversationClickListener listener) {
            tvName.setText(conversation.getDisplayName() != null ? 
                    conversation.getDisplayName() : "Unknown");
            tvLastMessage.setText(conversation.getLastMessageText() != null ? 
                    conversation.getLastMessageText() : "No messages yet");

            // Format time
            if (conversation.getLastMessageAt() != null) {
                try {
                    tvTime.setText(formatTime(conversation.getLastMessageAt()));
                } catch (Exception e) {
                    tvTime.setText("");
                }
            } else {
                tvTime.setText("");
            }

            // Load avatar
            String avatarUrl = conversation.getDisplayAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                if (!avatarUrl.startsWith("http")) {
                    avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                }
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(conversation.isGroupChat() ? 
                        R.drawable.ic_group : R.drawable.ic_person);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }

        private String formatTime(String isoTime) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                Date date = inputFormat.parse(isoTime);
                if (date == null) return "";

                long diff = System.currentTimeMillis() - date.getTime();
                long minutes = diff / (60 * 1000);
                long hours = diff / (60 * 60 * 1000);
                long days = diff / (24 * 60 * 60 * 1000);

                if (minutes < 1) return "Just now";
                if (minutes < 60) return minutes + "m ago";
                if (hours < 24) return hours + "h ago";
                if (days < 7) return days + "d ago";

                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.US);
                return outputFormat.format(date);
            } catch (Exception e) {
                return "";
            }
        }
    }
}
