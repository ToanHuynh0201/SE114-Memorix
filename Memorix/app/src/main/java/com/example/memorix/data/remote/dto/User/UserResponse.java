package com.example.memorix.data.remote.dto.User;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("user_id")
    private int userId;

    private String username;
    private String email;

    private String phone;
    private boolean isVerified;

    @SerializedName("image_url")
    private String imageUrl;

    // Getter
    public int getUserId()       { return userId; }
    public String getUsername()  { return username; }
    public String getEmail()     { return email; }

    public String getPhone() {return phone;}
    public boolean isVerified() {return isVerified;}

    public String getImage_url() {
        return imageUrl;
    }

}
