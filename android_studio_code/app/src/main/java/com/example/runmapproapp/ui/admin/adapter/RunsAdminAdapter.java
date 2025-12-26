package com.example.runmapproapp.ui.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.dto.RunResponse;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RunsAdminAdapter extends RecyclerView.Adapter<RunsAdminAdapter.RunViewHolder> {

    private List<RunResponse> runs;
    private Context context;
    private OnRunActionListener listener;

    public interface OnRunActionListener {
        void onDeleteRun(RunResponse run);
    }

    public RunsAdminAdapter(Context context, OnRunActionListener listener) {
        this.context = context;
        this.runs = new ArrayList<>();
        this.listener = listener;
    }

    public void setRuns(List<RunResponse> runs) {
        this.runs = runs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_run_admin, parent, false);
        return new RunViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RunViewHolder holder, int position) {
        RunResponse run = runs.get(position);
        holder.bind(run);
    }

    @Override
    public int getItemCount() {
        return runs.size();
    }

    class RunViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvDate;
        private TextView tvDistance;
        private TextView tvDuration;
        private TextView tvPace;
        private MaterialButton btnDelete;

        public RunViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPace = itemView.findViewById(R.id.tvPace);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(RunResponse run) {
            // User name
            tvUserName.setText(run.getUserId() != null ? run.getUserId() : "Unknown User");
            
            // Date
            if (run.getStartTime() != null) {
                tvDate.setText(run.getStartTime());
            } else {
                tvDate.setText("N/A");
            }
            
            // Distance
            if (run.getDistanceMeters() > 0) {
                double distanceKm = run.getDistanceMeters() / 1000.0;
                tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));
            } else {
                tvDistance.setText("0 km");
            }
            
            // Duration
            if (run.getDurationMs() > 0) {
                tvDuration.setText(formatDuration((int)(run.getDurationMs() / 1000)));
            } else {
                tvDuration.setText("--:--");
            }
            
            // Pace
            if (run.getAvgPaceSecPerKm() > 0) {
                int minutes = (int)(run.getAvgPaceSecPerKm() / 60);
                int seconds = (int)(run.getAvgPaceSecPerKm() % 60);
                tvPace.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            } else {
                tvPace.setText("--:--");
            }
            
            // Delete button
            btnDelete.setOnClickListener(v -> listener.onDeleteRun(run));
        }

        private String formatDuration(Integer durationSeconds) {
            int hours = durationSeconds / 3600;
            int minutes = (durationSeconds % 3600) / 60;
            int seconds = durationSeconds % 60;
            
            if (hours > 0) {
                return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
            }
        }
    }
}
