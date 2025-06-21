package com.example.memorix.data.remote.Interceptor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.memorix.data.remote.dto.Token.RefreshTokenRequest;
import com.example.memorix.helper.LogoutHelper;
import com.example.memorix.ui.login.LoginActivity;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.Login.LoginResponse;

import com.example.memorix.data.remote.dto.Token.TokenManager;
import com.example.memorix.data.remote.network.ApiClient;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class AuthInterceptor implements Interceptor {

    private final Context context;
    private final TokenManager tokenManager;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = new TokenManager(context);
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Lấy access token từ TokenManager
        String accessToken = tokenManager.getAccessToken();

        Request requestWithToken = originalRequest;
        if (accessToken != null) {
            requestWithToken = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
        }

        Response response = chain.proceed(requestWithToken);

        // Kiểm tra nếu access token đã hết hạn
        if (response.code() == 401) {
            String responseBody = response.peekBody(Long.MAX_VALUE).string();

            if (responseBody.contains("\"msg\":\"Access token expired\"") && responseBody.contains("\"action\":\"refresh\"")) {
                String refreshToken = tokenManager.getRefreshToken();
                if (refreshToken == null) {
                    logoutAndRedirect();
                    return response;
                }

                // Gọi API refresh token
                AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
                Call<LoginResponse> call = authApi.refreshToken(new RefreshTokenRequest(refreshToken));

                try {
                    retrofit2.Response<LoginResponse> refreshResponse = call.execute();

                    if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                        String newAccessToken = refreshResponse.body().getAccess_token();
                        String newRefreshToken = refreshResponse.body().getRefresh_token();

                        tokenManager.saveTokens(newAccessToken, newRefreshToken); // Lưu lại token mới

                        // Tạo lại request với token mới
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + newAccessToken)
                                .build();

                        response.close();
                        return chain.proceed(newRequest);
                    } else {
                        logoutAndRedirect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logoutAndRedirect();
                }
            }
        }

        return response;
    }

    private void logoutAndRedirect() {
        tokenManager.clear();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
