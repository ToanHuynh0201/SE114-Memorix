package com.example.memorix.data.remote.Interceptor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.view.login.LoginActivity;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.Login.LoginResponse;
import com.example.memorix.data.remote.dto.Token.RefreshTokenRequest;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Thêm header Authorization vào request nếu có access token
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);

        Request requestWithToken = originalRequest;
        if (accessToken != null) {
            Log.d("AUTH_INTERCEPTOR", "Access token attached to request");
            requestWithToken = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
        }

        Response response = chain.proceed(requestWithToken);

        if (response.code() == 401) {
            Log.d("AUTH_INTERCEPTOR", "401 Unauthorized detected");
            // Đọc body trả về để check lỗi token expired + action refresh
            String responseBody = response.peekBody(Long.MAX_VALUE).string();
            Log.d("AUTH_INTERCEPTOR", "Response body on 401: " + responseBody);

            if (responseBody.contains("Invalid token")) {
                Log.d("AUTH_INTERCEPTOR", "Token expired. Attempting to refresh...");
                // Gọi refresh token synchronously
                String refreshToken = prefs.getString("refresh_token", null);
                if (refreshToken == null) {
                    Log.e("AUTH_INTERCEPTOR", "Refresh token is null. Logging out.");
                    logoutAndRedirect();
                    return response;
                }

                AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
                Call<LoginResponse> call = authApi.refreshToken(new RefreshTokenRequest(refreshToken));
                try {
                    retrofit2.Response<LoginResponse> refreshResponse = call.execute();
                    if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                        Log.d("AUTH_INTERCEPTOR", "Refresh token success");
                        String newAccessToken = refreshResponse.body().getAccess_token();
                        String newRefreshToken = refreshResponse.body().getRefresh_token();

                        // Lưu token mới
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("access_token", newAccessToken);
                        editor.putString("refresh_token", newRefreshToken);
                        editor.apply();

                        // Thực hiện lại request với token mới
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + newAccessToken)
                                .build();

                        response.close(); // Đóng response cũ
                        Log.d("AUTH_INTERCEPTOR", "Retrying request with new token");
                        return chain.proceed(newRequest);
                    } else if (refreshResponse.code() == 401) {
                        Log.e("AUTH_INTERCEPTOR", "Refresh token failed with code: " + refreshResponse.code());
                        // Refresh token hết hạn, logout user
                        logoutAndRedirect();
                    }
                } catch (Exception e) {
                    Log.e("AUTH_INTERCEPTOR", "Exception during refresh: " + e.getMessage());
                    logoutAndRedirect();
                }
            }
        }

        return response;
    }

    private void logoutAndRedirect() {
        Log.d("AUTH_INTERCEPTOR", "Logging out user and redirecting to LoginActivity");
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("access_token");
        editor.remove("refresh_token");
        editor.apply();

        // Optional: gửi broadcast logout
        Intent broadcastIntent = new Intent("com.example.ACTION_LOGOUT");
        context.sendBroadcast(broadcastIntent);

        // ✅ Đảm bảo gọi LoginActivity trên Main Thread
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
