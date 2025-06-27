package com.example.memorix.data.remote.dto.Share;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class FromUser {
    @SerializedName("user_id")
    private long userId;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    // Constructors
    public FromUser() {}

    // Getters and Setters
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @NonNull
    @Override
    public String toString() {
        return "FromUser{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
