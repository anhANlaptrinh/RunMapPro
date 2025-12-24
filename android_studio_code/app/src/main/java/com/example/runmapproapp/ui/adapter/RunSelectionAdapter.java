package com.example.runmapproapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.dto.RunResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunSelectionAdapter extends RecyclerView.Adapter<RunSelectionAdapter.ViewHolder> {

    private List<RunResponse> runs = new ArrayList<>();
    private List<RunResponse> filteredRuns = new ArrayList<>();
    private OnRunClickListener listener;

    public interface OnRunClickListener {
        void onRunClick(RunResponse run);
    }

    public RunSelectionAdapter(OnRunClickListener listener) {
        this.listener = listener;
    }

    public void setRuns(List<RunResponse> runs) {
        this.runs = runs;
        this.filteredRuns = new ArrayList<>(runs);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredRuns.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredRuns.addAll(runs);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (RunResponse run : runs) {
                // Search by distance or date
                String distance = String.format(Locale.US, "%.2f", run.getDistanceMeters() / 1000.0);
                String date = run.getStartTime();
                
                if (distance.contains(lowerQuery) || date.toLowerCase().contains(lowerQuery)) {
                    filteredRuns.add(run);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_run_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RunResponse run = filteredRuns.get(position);
        holder.bind(run);
    }

    @Override
    public int getItemCount() {
        return filteredRuns.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRunDateTime;
        TextView tvRunDistance;
        TextView tvRunDuration;
        TextView tvRunSteps;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRunDateTime = itemView.findViewById(R.id.tvRunDateTime);
            tvRunDistance = itemView.findViewById(R.id.tvRunDistance);
            tvRunDuration = itemView.findViewById(R.id.tvRunDuration);
            tvRunSteps = itemView.findViewById(R.id.tvRunSteps);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRunClick(filteredRuns.get(position));
                    }
                }
            });
        }

        void bind(RunResponse run) {
            // Format date
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                java.util.Date date = isoFormat.parse(run.getStartTime());
                if (date != null) {
                    tvRunDateTime.setText(displayFormat.format(date));
                } else {
                    tvRunDateTime.setText(run.getStartTime());
                }
            } catch (Exception e) {
                tvRunDateTime.setText(run.getStartTime().substring(0, Math.min(16, run.getStartTime().length())));
            }

            // Distance
            double distanceKm = run.getDistanceMeters() / 1000.0;
            tvRunDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));

            // Duration
            long totalSeconds = run.getDurationMs() / 1000;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            
            if (hours > 0) {
                tvRunDuration.setText(String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds));
            } else {
                tvRunDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            // Steps
            tvRunSteps.setText(String.valueOf(run.getSteps()));
        }
    }
}
