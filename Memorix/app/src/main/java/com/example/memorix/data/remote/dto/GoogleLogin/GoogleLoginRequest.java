package com.example.memorix.data.remote.dto.GoogleLogin;

public class GoogleLoginRequest {
    private String id_token;

    public GoogleLoginRequest(String id_token) {
        this.id_token = id_token;
    }

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }
}