package com.example.runmapproapp.ui.notifications;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.model.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void addNotifications(List<Notification> newNotifications) {
        int startPosition = this.notifications.size();
        this.notifications.addAll(newNotifications);
        notifyItemRangeInserted(startPosition, newNotifications.size());
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvNotificationText;
        private TextView tvTimestamp;
        private View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvNotificationText = itemView.findViewById(R.id.tvNotificationText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(Notification notification, OnNotificationClickListener listener) {
            // Set avatar
            if (notification.getSenderAvatar() != null && !notification.getSenderAvatar().isEmpty()) {
                String avatarUrl = "http://10.0.2.2:8080/api/media/" + notification.getSenderAvatar();
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person);
            }

            // Set notification text
            tvNotificationText.setText(notification.getNotificationText(itemView.getContext()));

            // Set timestamp
            tvTimestamp.setText(getTimeAgo(notification.getCreatedAt()));

            // Set unread indicator
            unreadIndicator.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

            // Bold text for unread notifications
            if (!notification.isRead()) {
                tvNotificationText.setTypeface(null, Typeface.BOLD);
            } else {
                tvNotificationText.setTypeface(null, Typeface.NORMAL);
            }

            // Click listener
            itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
        }

        private String getTimeAgo(String createdAt) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(createdAt);
                if (date == null) return "";

                long timeMillis = date.getTime();
                long now = System.currentTimeMillis();
                long diff = now - timeMillis;

                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (days > 0) {
                    return days + " ngày trước";
                } else if (hours > 0) {
                    return hours + " giờ trước";
                } else if (minutes > 0) {
                    return minutes + " phút trước";
                } else {
                    return "Vừa xong";
                }
            } catch (Exception e) {
                return "";
            }
        }
    }
}
