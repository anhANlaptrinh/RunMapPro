package com.example.runmapproapp.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.api.MediaApi;
import com.example.runmapproapp.data.api.PostApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a Retrofit instance configured with logging and authentication.
 */
public final class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit;
    private static Context appContext;

    private ApiClient() {
    }

    /**
     * Initialize ApiClient with application context.
     * Must be called before using any API methods.
     */
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        retrofit = null; // Force rebuild on next access
    }

    @NonNull
    public static AuthApi getAuthApi() {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(AuthApi.class);
    }

    @NonNull
    public static UserApi getUserApi() {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(UserApi.class);
    }

    @NonNull
    public static PostApi getPostApi() {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(PostApi.class);
    }

    @NonNull
    public static GroupApi getGroupApi() {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(GroupApi.class);
    }

    @NonNull
    public static MediaApi getMediaApi() {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(MediaApi.class);
    }

    @NonNull
    public static ChatApi getChatApi() {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(ChatApi.class);
    }

    @NonNull
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit;
    }

    private static Retrofit buildRetrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor);

        // Add auth interceptor if context is available
        if (appContext != null) {
            clientBuilder.addInterceptor(new AuthInterceptor(appContext));
        }

        OkHttpClient okHttpClient = clientBuilder.build();

        Gson gson = new GsonBuilder().setLenient().create();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
