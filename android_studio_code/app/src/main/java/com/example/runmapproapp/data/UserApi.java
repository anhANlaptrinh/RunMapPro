package com.example.runmapproapp.data;

import com.example.runmapproapp.data.model.ChangePasswordRequest;
import com.example.runmapproapp.data.model.UpdateProfileRequest;
import com.example.runmapproapp.data.model.User;
import com.example.runmapproapp.data.model.UserProfileResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {

    @GET("/api/users/me")
    Call<UserProfileResponse> getProfile(@Header("Authorization") String token);

    @GET("/api/users/{userId}")
    Call<UserProfileResponse> getUserById(@Path("userId") String userId);

    @PUT("/api/users/me")
    Call<UserProfileResponse> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest body);

    @Multipart
    @POST("/api/users/me/avatar")
    Call<UserProfileResponse> uploadAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part filePart);

    @POST("/api/users/me/change-password")
    Call<Void> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest body);

    @GET("/api/users/search")
    Call<List<User>> searchUsers(@Query("query") String query);
}
