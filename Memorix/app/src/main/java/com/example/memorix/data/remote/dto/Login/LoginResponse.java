package com.example.memorix.data.remote.dto.Login;

import com.example.memorix.model.User;

public class LoginResponse {
    private String access_token;
    private String refresh_token;
    private User user;
    private int user_id;
    private String email;


    // Getters
    public String getAccess_token() { return access_token; }
    public String getRefresh_token() { return refresh_token; }
    public User getUser() { return user; }
    public String getEmail() {
        return email;
    }
    public int getUser_id() {
        return user_id;
    }
}

