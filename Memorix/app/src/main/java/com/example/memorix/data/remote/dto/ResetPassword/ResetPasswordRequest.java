package com.example.memorix.data.remote.dto.ResetPassword;

public class ResetPasswordRequest{
    String token;String password;

    public ResetPasswordRequest(String t,String p)
    {
        token=t;
        password=p;
    }
}