package com.example.memorix.data.remote.dto.Notification;

public class DeviceResponse {
    private DeviceData device;

    public DeviceData getDevice() {
        return device;
    }

    public static class DeviceData {
        private int device_id;
        private int user_id;
        private String fcm_token;
        private String device_name;
        private String last_used_at;

        // Getters
        public int getDevice_id() { return device_id; }
        public int getUser_id() { return user_id; }
        public String getFcm_token() { return fcm_token; }
        public String getDevice_name() { return device_name; }
        public String getLast_used_at() { return last_used_at; }
    }
}