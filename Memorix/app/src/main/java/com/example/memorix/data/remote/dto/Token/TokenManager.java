package com.example.memorix.data.remote.dto.Token;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TokenManager {

    private static final String PREF_NAME = "MyAppPrefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Save both tokens
    public void saveTokens(@NonNull String accessToken, @NonNull String refreshToken) {
        prefs.edit()
                .putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .apply();
    }

    // Save only access token
    public void saveAccessToken(@NonNull String accessToken) {
        prefs.edit().putString(ACCESS_TOKEN_KEY, accessToken).apply();
    }

    // Save only refresh token
    public void saveRefreshToken(@NonNull String refreshToken) {
        prefs.edit().putString(REFRESH_TOKEN_KEY, refreshToken).apply();
    }

    @Nullable
    public String getAccessToken() {
        return prefs.getString(ACCESS_TOKEN_KEY, null);
    }

    @Nullable
    public String getRefreshToken() {
        return prefs.getString(REFRESH_TOKEN_KEY, null);
    }

    // Xoá cả hai token
    public void clear() {
        prefs.edit()
                .remove(ACCESS_TOKEN_KEY)
                .remove(REFRESH_TOKEN_KEY)
                .apply();
    }
}
