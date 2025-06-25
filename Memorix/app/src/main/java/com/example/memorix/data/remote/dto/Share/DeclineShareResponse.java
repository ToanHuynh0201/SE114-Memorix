package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;

public class DeclineShareResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    // Constructors
    public DeclineShareResponse() {}

    public DeclineShareResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DeclineShareResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
