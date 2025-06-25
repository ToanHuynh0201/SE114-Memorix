package com.example.memorix.model;

public class User {
    public int user_id;
    public String username;
    public String email;
    private boolean is_verified;

    public User(int user_id, String username, String email) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        is_verified = false;
    }

    public boolean isVerified() { return is_verified; }
    public int getUserId() { return user_id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }

}
