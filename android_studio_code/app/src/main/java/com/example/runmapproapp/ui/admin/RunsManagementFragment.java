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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.AdminApi;
import com.example.runmapproapp.data.response.MessageResponse;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.ui.admin.adapter.RunsAdminAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RunsManagementFragment extends Fragment implements RunsAdminAdapter.OnRunActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private RunsAdminAdapter adapter;
    private AdminApi adminApi;
    private RunApiService runApiService;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(requireContext());
        adminApi = ApiClient.getClient().create(AdminApi.class);
        runApiService = RetrofitClient.getRunApiService();

        bindViews(view);
        setupRecyclerView();
        loadRuns();
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvEmptyState.setText(R.string.no_runs_found);
    }

    private void setupRecyclerView() {
        adapter = new RunsAdminAdapter(requireContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadRuns() {
        setLoading(true);

        // Use admin endpoint to get ALL runs
        adminApi.getAllRuns().enqueue(new Callback<List<RunResponse>>() {
            @Override
            public void onResponse(Call<List<RunResponse>> call, Response<List<RunResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<RunResponse> runs = response.body();
                    adapter.setRuns(runs);
                    tvEmptyState.setVisibility(runs.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    showError(getString(R.string.failed_load_runs));
                }
            }

            @Override
            public void onFailure(Call<List<RunResponse>> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error_message));
            }
        });
    }

    @Override
    public void onDeleteRun(RunResponse run) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_run)
                .setMessage(R.string.delete_run_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> performDeleteRun(run))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performDeleteRun(RunResponse run) {
        adminApi.deleteRun(run.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), R.string.run_deleted_success, Toast.LENGTH_SHORT).show();
                    loadRuns(); // Reload list
                } else {
                    showError(getString(R.string.failed_delete_run));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showError(getString(R.string.network_error_message));
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
