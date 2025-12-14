package com.example.runmapproapp.data.api;

import com.example.runmapproapp.data.model.Notification;
import com.example.runmapproapp.data.model.UnreadCountResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {
    @GET("api/notifications")
    Call<List<Notification>> getNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/notifications/unread-count")
    Call<UnreadCountResponse> getUnreadCount();

    @PUT("api/notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") String notificationId);

    @PUT("api/notifications/read-all")
    Call<Void> markAllAsRead();
}
