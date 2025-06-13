package com.example.memorix.data.remote.dto.User;

public class UpdateUserRequest {
    public String username;
    public String email;
    public String password;

    public UpdateUserRequest(String username , String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
    }


}
