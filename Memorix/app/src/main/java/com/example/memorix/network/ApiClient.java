package com.example.memorix.network;

import android.content.Context;

import com.example.memorix.data.remote.Interceptor.AuthInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:3000";
    private static Retrofit retrofit = null;

    public static void init(Context context) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init(context) first.");
        }
        return retrofit;
    }
}
