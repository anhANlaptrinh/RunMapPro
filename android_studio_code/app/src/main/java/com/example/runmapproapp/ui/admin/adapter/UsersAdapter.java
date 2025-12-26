package com.example.runmapproapp.ui.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
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
import com.example.runmapproapp.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users;
    private List<User> usersFiltered;
    private Context context;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onBanUser(User user);
        void onUnbanUser(User user);
    }

    public UsersAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.users = new ArrayList<>();
        this.usersFiltered = new ArrayList<>();
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        this.usersFiltered = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.trim().isEmpty()) {
            usersFiltered = new ArrayList<>(users);
        } else {
            usersFiltered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (User user : users) {
                if ((user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerQuery)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) ||
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerQuery))) {
                    usersFiltered.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersFiltered.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return usersFiltered.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvUsername;
        private TextView tvEmail;
        private TextView tvRole;
        private TextView tvBannedBadge;
        private ImageButton btnMore;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvBannedBadge = itemView.findViewById(R.id.tvBannedBadge);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        public void bind(User user) {
            tvUsername.setText(user.getUsername() != null ? user.getUsername() : "N/A");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            
            // Set role
            String role = user.getRole() != null ? user.getRole() : "USER";
            tvRole.setText(role);
            
            // Set banned badge visibility
            tvBannedBadge.setVisibility(user.isBanned() ? View.VISIBLE : View.GONE);
            
            // Load avatar - using same logic as PostAdapter
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String avatarUrl = user.getAvatarUrl();
                // Convert relative path to full URL (same as PostAdapter)
                if (avatarUrl.startsWith("/api/")) {
                    avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                }
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_account_circle)
                        .error(R.drawable.ic_account_circle)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_account_circle);
            }
            
            // Setup more button
            btnMore.setOnClickListener(v -> showPopupMenu(v, user));
        }

        private void showPopupMenu(View view, User user) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.user_actions_menu);
            
            // Hide/show ban/unban based on current status
            popup.getMenu().findItem(R.id.action_ban).setVisible(!user.isBanned());
            popup.getMenu().findItem(R.id.action_unban).setVisible(user.isBanned());
            
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_ban) {
                    listener.onBanUser(user);
                    return true;
                } else if (itemId == R.id.action_unban) {
                    listener.onUnbanUser(user);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }
    }
}
