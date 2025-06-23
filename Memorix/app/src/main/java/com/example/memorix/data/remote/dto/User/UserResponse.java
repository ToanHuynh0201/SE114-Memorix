package com.example.memorix.data.remote.dto.User;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("user_id")
    private int userId;

    private String username;
    private String email;

    // Getter
    public int getUserId()       { return userId; }
    public String getUsername()  { return username; }
    public String getEmail()     { return email; }


}
