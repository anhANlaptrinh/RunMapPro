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
import com.example.runmapproapp.data.model.GroupJoinRequest;
import com.example.runmapproapp.data.model.User;
import com.example.runmapproapp.data.model.UserProfileResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingMemberAdapter extends RecyclerView.Adapter<PendingMemberAdapter.ViewHolder> {

    private List<GroupJoinRequest> requests;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(GroupJoinRequest request);
    }

    public PendingMemberAdapter(List<GroupJoinRequest> requests, OnMemberClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    public void updateData(List<GroupJoinRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupJoinRequest request = requests.get(position);
        holder.bind(request, listener);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarView;
        private TextView nameView;
        private TextView timeView;
        private String userId; // Store userId for profile view

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatarImage);
            nameView = itemView.findViewById(R.id.nameText);
            timeView = itemView.findViewById(R.id.timeText);
        }

        void bind(GroupJoinRequest request, OnMemberClickListener listener) {
            // Store userId
            this.userId = request.getUserId();
            
            // Load user info
            UserApi userApi = ApiClient.getUserApi();
            userApi.getUserById(request.getUserId()).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfileResponse userProfile = response.body();
                        
                        // Set name
                        String displayName = userProfile.getFullName() != null && !userProfile.getFullName().isEmpty() 
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
                    nameView.setText("Unknown User");
                    avatarView.setImageResource(R.drawable.ic_person);
                }
            });

            // Format time
            if (request.getRequestedAt() != null) {
                timeView.setText(formatTime(request.getRequestedAt()));
            } else {
                timeView.setText("");
            }

            // Click listener for entire item
            itemView.setOnClickListener(v -> listener.onMemberClick(request));
            
            // Click listener for avatar to view profile
            avatarView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), com.example.runmapproapp.ui.profile.UserProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                itemView.getContext().startActivity(intent);
            });
        }

        private String formatTime(String timestamp) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = inputFormat.parse(timestamp);
                
                if (date != null) {
                    long diff = System.currentTimeMillis() - date.getTime();
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
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}
