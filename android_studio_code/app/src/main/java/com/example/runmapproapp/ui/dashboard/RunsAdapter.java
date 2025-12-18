package com.example.runmapproapp.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class RunsAdapter extends RecyclerView.Adapter<RunsAdapter.RunViewHolder> {

    private List<RunResponse> runs = new ArrayList<>();
    private OnRunClickListener listener;
    private int selectedPosition = -1;

    public interface OnRunClickListener {
        void onRunClick(RunResponse run, int position);
    }

    public RunsAdapter(OnRunClickListener listener) {
        this.listener = listener;
    }

    public void setRuns(List<RunResponse> runs) {
        this.runs = runs != null ? runs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_run, parent, false);
        return new RunViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RunViewHolder holder, int position) {
        RunResponse run = runs.get(position);
        holder.bind(run, position);
    }

    @Override
    public int getItemCount() {
        return runs.size();
    }

    class RunViewHolder extends RecyclerView.ViewHolder {
        TextView tvRunDate;
        TextView tvRunDistance;
        TextView tvRunDuration;
        TextView tvRunAvgPace;
        TextView tvDistanceLabel;
        TextView tvDurationLabel;
        TextView tvPaceLabel;

        RunViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRunDate = itemView.findViewById(R.id.tvRunDate);
            tvRunDistance = itemView.findViewById(R.id.tvRunDistance);
            tvRunDuration = itemView.findViewById(R.id.tvRunDuration);
            tvRunAvgPace = itemView.findViewById(R.id.tvRunAvgPace);
            tvDistanceLabel = itemView.findViewById(R.id.tvDistanceLabel);
            tvDurationLabel = itemView.findViewById(R.id.tvDurationLabel);
            tvPaceLabel = itemView.findViewById(R.id.tvPaceLabel);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRunClick(runs.get(position), position);
                }
            });
        }

        void bind(RunResponse run, int position) {
            // Format date
            tvRunDate.setText(FormatUtils.formatDateTime(run.getStartTime()));

            // Format distance
            tvRunDistance.setText(FormatUtils.formatDistance(run.getDistanceMeters()));

            // Format duration
            tvRunDuration.setText(FormatUtils.formatDuration(run.getDurationMs()));

            // Format pace
            tvRunAvgPace.setText(FormatUtils.formatPace(run.getAvgPaceSecPerKm()));

            // Highlight selected
            boolean isSelected = position == selectedPosition;
            itemView.setBackgroundColor(
                    isSelected
                            ? itemView.getContext().getResources().getColor(R.color.deep_blue_500, null)
                            : itemView.getContext().getResources().getColor(android.R.color.white, null)
            );
            
            // Set text colors based on selection
            int textColor = isSelected 
                    ? itemView.getContext().getResources().getColor(android.R.color.white, null)
                    : itemView.getContext().getResources().getColor(android.R.color.black, null);
            int labelColor = isSelected 
                    ? itemView.getContext().getResources().getColor(android.R.color.white, null)
                    : itemView.getContext().getResources().getColor(R.color.black, null);
            
            tvRunDate.setTextColor(textColor);
            tvRunDistance.setTextColor(textColor);
            tvRunDuration.setTextColor(textColor);
            tvRunAvgPace.setTextColor(textColor);
            tvDistanceLabel.setTextColor(labelColor);
            tvDurationLabel.setTextColor(labelColor);
            tvPaceLabel.setTextColor(labelColor);
        }
    }
}
