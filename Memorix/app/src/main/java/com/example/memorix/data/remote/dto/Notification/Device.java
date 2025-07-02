package com.example.memorix.data.remote.dto.Notification;

public class Device {
    private String fcm_token;
    private String device_name;

    // Constructor
    public Device(String fcm_token, String device_name) {
        this.fcm_token = fcm_token;
        this.device_name = device_name;
    }

    // Getters and Setters
    public String getFcm_token() {
        return fcm_token;
    }

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }
}