package com.example.runmapproapp.ui.admin.adapter;

import android.content.Context;
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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class GroupsAdminAdapter extends RecyclerView.Adapter<GroupsAdminAdapter.GroupViewHolder> {

    private List<Group> groups;
    private Context context;
    private OnGroupActionListener listener;

    public interface OnGroupActionListener {
        void onDeleteGroup(Group group);
    }

    public GroupsAdminAdapter(Context context, OnGroupActionListener listener) {
        this.context = context;
        this.groups = new ArrayList<>();
        this.listener = listener;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_admin, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivGroupCover;
        private TextView tvGroupName;
        private TextView tvGroupDescription;
        private TextView tvGroupStats;
        private TextView tvGroupPrivacy;
        private MaterialButton btnDelete;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroupCover = itemView.findViewById(R.id.ivGroupCover);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);
            tvGroupStats = itemView.findViewById(R.id.tvGroupStats);
            tvGroupPrivacy = itemView.findViewById(R.id.tvGroupPrivacy);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Group group) {
            tvGroupName.setText(group.getName());
            tvGroupDescription.setText(group.getDescription());
            
            // Stats (same logic as GroupAdapter)
            String stats = context.getString(R.string.group_stats_format, 
                    group.getMemberCount(), group.getPostCount());
            tvGroupStats.setText(stats);
            
            // Privacy (handle both uppercase and lowercase from backend)
            String privacy = group.getPrivacy() != null && 
                    (group.getPrivacy().equalsIgnoreCase("PRIVATE") || group.getPrivacy().equalsIgnoreCase("private"))
                    ? context.getString(R.string.private_group) 
                    : context.getString(R.string.public_group);
            tvGroupPrivacy.setText(privacy);
            
            // Load cover image (same logic as GroupAdapter)
            if (group.getCoverImageUrl() != null && !group.getCoverImageUrl().isEmpty()) {
                String coverImageUrl = "http://10.0.2.2:8080/api/media/" + group.getCoverImageUrl();
                Glide.with(context)
                        .load(coverImageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(ivGroupCover);
            } else {
                ivGroupCover.setImageResource(R.drawable.ic_launcher_background);
            }
            
            // Delete button
            btnDelete.setOnClickListener(v -> listener.onDeleteGroup(group));
        }
    }
}
