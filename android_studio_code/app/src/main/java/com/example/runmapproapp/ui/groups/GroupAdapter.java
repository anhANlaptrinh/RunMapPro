package com.example.runmapproapp.ui.groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.model.Group;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.bind(group, listener);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    public void addGroups(List<Group> newGroups) {
        int startPosition = this.groups.size();
        this.groups.addAll(newGroups);
        notifyItemRangeInserted(startPosition, newGroups.size());
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivGroupCover;
        private TextView tvGroupName;
        private TextView tvGroupDescription;
        private TextView tvGroupStats;
        private TextView tvGroupPrivacy;
        private TextView tvAdminBadge;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroupCover = itemView.findViewById(R.id.ivGroupCover);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);
            tvGroupStats = itemView.findViewById(R.id.tvGroupStats);
            tvGroupPrivacy = itemView.findViewById(R.id.tvGroupPrivacy);
            tvAdminBadge = itemView.findViewById(R.id.tvAdminBadge);
        }

        public void bind(Group group, OnGroupClickListener listener) {
            tvGroupName.setText(group.getName());
            tvGroupDescription.setText(group.getDescription());
            
            String stats = itemView.getContext().getString(R.string.group_stats_format, 
                    group.getMemberCount(), group.getPostCount());
            tvGroupStats.setText(stats);
            
            String privacy = group.getPrivacy() != null && group.getPrivacy().equals("PRIVATE") 
                    ? itemView.getContext().getString(R.string.private_group) 
                    : itemView.getContext().getString(R.string.public_group);
            tvGroupPrivacy.setText(privacy);
            
            // Show admin badge if user is owner or admin
            if (group.getUserRole() != null && 
                    (group.getUserRole().equals("owner") || group.getUserRole().equals("admin"))) {
                tvAdminBadge.setVisibility(View.VISIBLE);
                String badgeText = group.getUserRole().equals("owner") 
                        ? itemView.getContext().getString(R.string.group_owner) 
                        : itemView.getContext().getString(R.string.group_admin);
                tvAdminBadge.setText(badgeText);
            } else {
                tvAdminBadge.setVisibility(View.GONE);
            }

            // Load cover image
            if (group.getCoverImageUrl() != null && !group.getCoverImageUrl().isEmpty()) {
                String coverImageUrl = "http://10.0.2.2:8080/api/media/" + group.getCoverImageUrl();
                Glide.with(itemView.getContext())
                        .load(coverImageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(ivGroupCover);
            } else {
                ivGroupCover.setImageResource(R.drawable.ic_launcher_background);
            }

            itemView.setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}
