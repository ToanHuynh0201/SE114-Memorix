package com.example.memorix.data.remote.dto.User;
import com.google.gson.annotations.SerializedName;
public class UpdateUserRequest {
    public String username;
    public String email;
    public String phone;
    public Boolean isVerified;
    private String image_base64;

    public UpdateUserRequest(String username , String email, String phone , boolean isVerified , String image_base64){
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.isVerified = isVerified;
        this.image_base64 = image_base64;
    }


}
