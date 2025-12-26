package com.example.runmapproapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.AdminApi;
import com.example.runmapproapp.data.response.StatisticsResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView tvTotalUsers;
    private TextView tvActiveUsers;
    private TextView tvBannedUsers;
    private TextView tvTotalPosts;
    private TextView tvTotalGroups;
    private TextView tvTotalRuns;
    private MaterialButton btnRefresh;
    private ProgressBar progressBar;
    private View statsContainer;

    private AdminApi adminApi;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(requireContext());
        adminApi = ApiClient.getClient().create(AdminApi.class);

        bindViews(view);
        setupListeners();
        loadStatistics();
    }

    private void bindViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvActiveUsers = view.findViewById(R.id.tvActiveUsers);
        tvBannedUsers = view.findViewById(R.id.tvBannedUsers);
        tvTotalPosts = view.findViewById(R.id.tvTotalPosts);
        tvTotalGroups = view.findViewById(R.id.tvTotalGroups);
        tvTotalRuns = view.findViewById(R.id.tvTotalRuns);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        statsContainer = view.findViewById(R.id.statsContainer);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadStatistics());
    }

    private void loadStatistics() {
        setLoading(true);

        adminApi.getStatistics().enqueue(new Callback<StatisticsResponse>() {
            @Override
            public void onResponse(Call<StatisticsResponse> call, Response<StatisticsResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayStatistics(response.body());
                } else {
                    showError(getString(R.string.failed_load_statistics));
                }
            }

            @Override
            public void onFailure(Call<StatisticsResponse> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error_message));
            }
        });
    }

    private void displayStatistics(StatisticsResponse stats) {
        tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
        tvActiveUsers.setText(String.valueOf(stats.getActiveUsers()));
        tvBannedUsers.setText(String.valueOf(stats.getBannedUsers()));
        tvTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        tvTotalGroups.setText(String.valueOf(stats.getTotalGroups()));
        tvTotalRuns.setText(String.valueOf(stats.getTotalRuns()));
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        statsContainer.setVisibility(loading ? View.GONE : View.VISIBLE);
        btnRefresh.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
