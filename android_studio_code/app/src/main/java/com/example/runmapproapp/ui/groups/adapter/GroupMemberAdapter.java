package com.example.runmapproapp.ui.groups.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.UserApi;
import com.example.runmapproapp.data.model.GroupMember;
import com.example.runmapproapp.data.model.UserProfileResponse;
import com.example.runmapproapp.ui.profile.UserProfileActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    public interface OnMemberActionListener {
        void onMemberAction(GroupMember member, String memberName);
    }

    private List<GroupMember> members;
    private OnMemberActionListener actionListener;
    private String currentUserRole;

    public GroupMemberAdapter(List<GroupMember> members, OnMemberActionListener actionListener) {
        this.members = members;
        this.actionListener = actionListener;
    }
    
    public void setCurrentUserRole(String role) {
        this.currentUserRole = role;
        notifyDataSetChanged();
    }

    public void updateData(List<GroupMember> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMember member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarView;
        private TextView nameView;
        private TextView roleView;
        private ImageView moreIcon;
        private String userId;
        private String displayName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatarImage);
            nameView = itemView.findViewById(R.id.nameText);
            roleView = itemView.findViewById(R.id.roleText);
            moreIcon = itemView.findViewById(R.id.moreIcon);
        }

        void bind(GroupMember member) {
            this.userId = member.getUserId();

            // Set role badge
            String role = member.getRole();
            if ("owner".equals(role)) {
                roleView.setText("Chủ nhóm");
                roleView.setBackgroundResource(R.drawable.bg_badge);
                roleView.setVisibility(View.VISIBLE);
            } else if ("admin".equals(role)) {
                roleView.setText("Quản trị viên");
                roleView.setBackgroundResource(R.drawable.circle_blue);
                roleView.setVisibility(View.VISIBLE);
            } else {
                roleView.setVisibility(View.GONE);
            }
            
            // Show more icon only for owner on non-owner members
            if ("owner".equals(currentUserRole) && !"owner".equals(role)) {
                moreIcon.setVisibility(View.VISIBLE);
                moreIcon.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onMemberAction(member, displayName);
                    }
                });
            } else {
                moreIcon.setVisibility(View.GONE);
            }

            // Load user info
            UserApi userApi = ApiClient.getUserApi();
            userApi.getUserById(member.getUserId()).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfileResponse userProfile = response.body();

                        // Set name
                        displayName = userProfile.getFullName() != null && !userProfile.getFullName().isEmpty()
                                ? userProfile.getFullName()
                                : userProfile.getUsername();
                        nameView.setText(displayName);

                        // Load avatar
                        String avatarUrl = null;
                        if (userProfile.getAvatarUrl() != null && !userProfile.getAvatarUrl().isEmpty()) {
                            if (userProfile.getAvatarUrl().startsWith("http")) {
                                avatarUrl = userProfile.getAvatarUrl();
                            } else {
                                avatarUrl = "http://10.0.2.2:8080" + userProfile.getAvatarUrl();
                            }
                        }

                        if (avatarUrl != null) {
                            Glide.with(itemView.getContext())
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .circleCrop()
                                    .into(avatarView);
                        } else {
                            Glide.with(itemView.getContext())
                                    .load(R.drawable.ic_person)
                                    .circleCrop()
                                    .into(avatarView);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                    displayName = "Unknown User";
                    nameView.setText(displayName);
                    avatarView.setImageResource(R.drawable.ic_person);
                }
            });

            // Click to view profile
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
