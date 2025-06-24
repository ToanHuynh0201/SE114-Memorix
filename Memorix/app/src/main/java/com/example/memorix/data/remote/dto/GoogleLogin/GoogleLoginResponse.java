package com.example.memorix.data.remote.dto.GoogleLogin;

public class GoogleLoginResponse {
    private String access_token;
    private User user;

    public String getAccess_token() {
        return access_token;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        private int user_id;
        private String username;
        private String email;
        private String phone;
        private String image_url;
        private boolean is_verified;

        public int getUser_id() {
            return user_id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getImage_url() {
            return image_url;
        }

        public boolean isIs_verified() {
            return is_verified;
        }
    }
}