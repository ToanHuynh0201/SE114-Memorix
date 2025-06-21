package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.LogOut.LogoutRequest;
import com.example.memorix.data.remote.dto.Login.LoginRequest;
import com.example.memorix.data.remote.dto.Login.LoginResponse;
import com.example.memorix.data.remote.dto.Register.RegisterRequest;
import com.example.memorix.data.remote.dto.Register.RegisterResponse;
import com.example.memorix.data.remote.dto.Register.VerifyEmailRequest;
import com.example.memorix.data.remote.dto.Register.VerifyEmailResponse;
import com.example.memorix.data.remote.dto.ResetPassword.ForgotPasswordRequest;
import com.example.memorix.data.remote.dto.ResetPassword.ForgotPasswordResponse;
import com.example.memorix.data.remote.dto.ResetPassword.ResetPasswordRequest;
import com.example.memorix.data.remote.dto.ResetPassword.ResetPasswordResponse;
import com.example.memorix.data.remote.dto.Token.RefreshTokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @Headers("Content-Type: application/json")
    @POST("/api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("/api/auth/verify-email")
    Call<VerifyEmailResponse> verifyEmail(@Body VerifyEmailRequest r);

    @POST("/api/auth/refresh-token")
    Call<LoginResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("/api/auth/logout")
    Call<Void> logout(
            @Header("Authorization") String bearerToken,
            @Body LogoutRequest request
    );

    @POST("/api/auth/forgot-password")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("/api/auth/reset-password")
    Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest request);
}

