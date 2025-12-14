package com.example.runmapproapp.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.model.ChatMessage;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    private List<ChatMessage> messages;
    private String currentUserId;

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutIncoming;
        private LinearLayout layoutOutgoing;
        private ShapeableImageView ivSenderAvatar;
        private TextView tvSenderName;
        private TextView tvIncomingMessage;
        private TextView tvIncomingTime;
        private TextView tvOutgoingMessage;
        private TextView tvOutgoingTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutIncoming = itemView.findViewById(R.id.layoutIncoming);
            layoutOutgoing = itemView.findViewById(R.id.layoutOutgoing);
            ivSenderAvatar = itemView.findViewById(R.id.ivSenderAvatar);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvIncomingMessage = itemView.findViewById(R.id.tvIncomingMessage);
            tvIncomingTime = itemView.findViewById(R.id.tvIncomingTime);
            tvOutgoingMessage = itemView.findViewById(R.id.tvOutgoingMessage);
            tvOutgoingTime = itemView.findViewById(R.id.tvOutgoingTime);
        }

        public void bind(ChatMessage message) {
            if (message.isMine()) {
                // Show outgoing message
                layoutIncoming.setVisibility(View.GONE);
                layoutOutgoing.setVisibility(View.VISIBLE);
                tvOutgoingMessage.setText(message.getText());
                tvOutgoingTime.setText(formatTime(message.getCreatedAt()));
            } else {
                // Show incoming message
                layoutIncoming.setVisibility(View.VISIBLE);
                layoutOutgoing.setVisibility(View.GONE);
                tvIncomingMessage.setText(message.getText());
                tvIncomingTime.setText(formatTime(message.getCreatedAt()));

                // Show sender info for group chats
                if (message.getSenderName() != null) {
                    tvSenderName.setText(message.getSenderName());
                    tvSenderName.setVisibility(View.VISIBLE);
                } else {
                    tvSenderName.setVisibility(View.GONE);
                }

                // Load sender avatar
                if (message.getSenderAvatarUrl() != null && !message.getSenderAvatarUrl().isEmpty()) {
                    String avatarUrl = message.getSenderAvatarUrl();
                    if (!avatarUrl.startsWith("http")) {
                        avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                    }
                    Glide.with(itemView.getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(ivSenderAvatar);
                } else {
                    ivSenderAvatar.setImageResource(R.drawable.ic_person);
                }
            }
        }

        private String formatTime(String isoTime) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                Date date = inputFormat.parse(isoTime);
                if (date == null) return "";

                SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.US);
                return outputFormat.format(date);
            } catch (Exception e) {
                return "";
            }
        }
    }
}
