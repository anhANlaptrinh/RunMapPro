package com.example.runmapproapp.data.api;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface MediaApi {
    @Multipart
    @POST("/api/media")
    Call<MediaUploadResponse> uploadMedia(@Part MultipartBody.Part file);

    @GET("/api/media/{mediaId}")
    Call<ResponseBody> downloadMedia(@Path("mediaId") String mediaId);
}
