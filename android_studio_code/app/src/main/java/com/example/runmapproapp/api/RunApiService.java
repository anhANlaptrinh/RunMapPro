package com.example.runmapproapp.api;

import com.example.runmapproapp.dto.CreateRunRequest;
import com.example.runmapproapp.dto.RunResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RunApiService {
    @POST("runs")
    Call<RunResponse> createRun(@Body CreateRunRequest request);

    @GET("runs")
    Call<List<RunResponse>> getRuns();

    @GET("runs/{id}")
    Call<RunResponse> getRun(@Path("id") String runId);
}
