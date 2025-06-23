package com.example.memorix.model;

public class User {
    public int user_id;
    public String username;
    public String email;
    private boolean is_verified;

    public boolean isVerified() { return is_verified; }
    public int getUserId() { return user_id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }

}
