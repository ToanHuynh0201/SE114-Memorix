package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.Notification.Device;
import com.example.memorix.data.remote.dto.Notification.DeviceResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface NotificationApi {
    @POST("api/devices")
    Call<DeviceResponse> createDevice(@Header("Authorization") String authorization, @Body Device device);
}
