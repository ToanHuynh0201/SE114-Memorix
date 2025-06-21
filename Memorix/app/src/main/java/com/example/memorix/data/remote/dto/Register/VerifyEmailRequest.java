package com.example.memorix.data.remote.dto.Register;

public class VerifyEmailRequest {
    private long user_id;
    private String code;
    public VerifyEmailRequest(long userId, String code) {
        this.user_id = userId;
        this.code = code;
    }
}