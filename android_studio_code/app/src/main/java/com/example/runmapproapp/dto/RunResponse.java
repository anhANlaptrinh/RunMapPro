package com.example.runmapproapp.dto;

import com.google.gson.annotations.SerializedName;

public class RunResponse {

    // Backend trả "id"
    @SerializedName("id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("distanceMeters")
    private double distanceMeters;

    @SerializedName("durationMs")
    private long durationMs;

    @SerializedName("steps")
    private int steps;

    @SerializedName("calories")
    private double calories;

    @SerializedName("bestPaceSecPerKm")
    private double bestPaceSecPerKm;

    @SerializedName("avgPaceSecPerKm")
    private double avgPaceSecPerKm;

    // Backend trả "path"
    @SerializedName("path")
    private GeoJsonLineStringDto path;

    // ---------- Getters / Setters ----------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getBestPaceSecPerKm() {
        return bestPaceSecPerKm;
    }

    public void setBestPaceSecPerKm(double bestPaceSecPerKm) {
        this.bestPaceSecPerKm = bestPaceSecPerKm;
    }

    public double getAvgPaceSecPerKm() {
        return avgPaceSecPerKm;
    }

    public void setAvgPaceSecPerKm(double avgPaceSecPerKm) {
        this.avgPaceSecPerKm = avgPaceSecPerKm;
    }

    public GeoJsonLineStringDto getPath() {
        return path;
    }

    public void setPath(GeoJsonLineStringDto path) {
        this.path = path;
    }
}
