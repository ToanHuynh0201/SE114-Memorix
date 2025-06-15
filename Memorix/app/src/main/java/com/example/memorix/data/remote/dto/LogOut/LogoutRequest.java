package com.example.memorix.data.remote.dto.LogOut;

public class LogoutRequest {
    private String refresh_token;

    public LogoutRequest(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    // getter & setter
}
