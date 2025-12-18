package com.example.runmapproapp.dto;

public class CreateRunRequest {
    private String userId;
    private String startTime;
    private String endTime;
    private double distanceMeters;
    private long durationMs;
    private int steps;
    private double calories;
    private double bestPaceSecPerKm;
    private double avgPaceSecPerKm;
    private GeoJsonLineStringDto path;

    // Constructor
    public CreateRunRequest() {}

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getBestPaceSecPerKm() { return bestPaceSecPerKm; }
    public void setBestPaceSecPerKm(double bestPaceSecPerKm) { this.bestPaceSecPerKm = bestPaceSecPerKm; }

    public double getAvgPaceSecPerKm() { return avgPaceSecPerKm; }
    public void setAvgPaceSecPerKm(double avgPaceSecPerKm) { this.avgPaceSecPerKm = avgPaceSecPerKm; }

    public GeoJsonLineStringDto getPath() { return path; }
    public void setPath(GeoJsonLineStringDto path) { this.path = path; }

    // Builder pattern
    public static class Builder {
        private final CreateRunRequest request = new CreateRunRequest();

        public Builder userId(String userId) {
            request.userId = userId;
            return this;
        }

        public Builder startTime(String startTime) {
            request.startTime = startTime;
            return this;
        }

        public Builder endTime(String endTime) {
            request.endTime = endTime;
            return this;
        }

        public Builder distanceMeters(double distanceMeters) {
            request.distanceMeters = distanceMeters;
            return this;
        }

        public Builder durationMs(long durationMs) {
            request.durationMs = durationMs;
            return this;
        }

        public Builder steps(int steps) {
            request.steps = steps;
            return this;
        }

        public Builder calories(double calories) {
            request.calories = calories;
            return this;
        }

        public Builder bestPaceSecPerKm(double bestPaceSecPerKm) {
            request.bestPaceSecPerKm = bestPaceSecPerKm;
            return this;
        }

        public Builder avgPaceSecPerKm(double avgPaceSecPerKm) {
            request.avgPaceSecPerKm = avgPaceSecPerKm;
            return this;
        }

        public Builder path(GeoJsonLineStringDto path) {
            request.path = path;
            return this;
        }

        public CreateRunRequest build() {
            return request;
        }
    }
}
