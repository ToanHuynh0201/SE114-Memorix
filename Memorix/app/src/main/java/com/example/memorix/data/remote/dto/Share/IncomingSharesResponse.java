package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;
import java.util.List;
public class IncomingSharesResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<IncomingShare> data;

    // Constructors
    public IncomingSharesResponse() {}

    public IncomingSharesResponse(boolean success, List<IncomingShare> data) {
        this.success = success;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<IncomingShare> getData() {
        return data;
    }

    public void setData(List<IncomingShare> data) {
        this.data = data;
    }
}
