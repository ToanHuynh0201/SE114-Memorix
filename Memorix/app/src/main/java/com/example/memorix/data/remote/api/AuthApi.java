package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.LoginRequest;
import com.example.memorix.data.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}

