package com.example.memorix.data.remote.dto.User;
import com.google.gson.annotations.SerializedName;
public class UpdateUserRequest {
    public String username;
    public String email;
    public String phone;
    public Boolean isVerified;
    @SerializedName("image_url")
    public String imageUrl;

    public UpdateUserRequest(String username , String email, String phone , boolean isVerified , String imageUrl){
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.isVerified = isVerified;
        this.imageUrl = imageUrl;
    }


}
