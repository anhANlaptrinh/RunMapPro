package com.example.runmapproapp.data.response;

/**
 * Statistics response for admin dashboard
 */
public class StatisticsResponse {
    private long totalUsers;
    private long bannedUsers;
    private long activeUsers;
    private long totalPosts;
    private long totalGroups;
    private long totalRuns;
    
    public StatisticsResponse() {
    }
    
    public StatisticsResponse(long totalUsers, long bannedUsers, long activeUsers, 
                             long totalPosts, long totalGroups, long totalRuns) {
        this.totalUsers = totalUsers;
        this.bannedUsers = bannedUsers;
        this.activeUsers = activeUsers;
        this.totalPosts = totalPosts;
        this.totalGroups = totalGroups;
        this.totalRuns = totalRuns;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getBannedUsers() {
        return bannedUsers;
    }
    
    public void setBannedUsers(long bannedUsers) {
        this.bannedUsers = bannedUsers;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public long getTotalPosts() {
        return totalPosts;
    }
    
    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }
    
    public long getTotalGroups() {
        return totalGroups;
    }
    
    public void setTotalGroups(long totalGroups) {
        this.totalGroups = totalGroups;
    }
    
    public long getTotalRuns() {
        return totalRuns;
    }
    
    public void setTotalRuns(long totalRuns) {
        this.totalRuns = totalRuns;
    }
}
