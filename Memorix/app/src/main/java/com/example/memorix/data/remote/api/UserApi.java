package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.User.UpdateUserRequest;
import com.example.memorix.data.remote.dto.User.UserResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public interface UserApi{

    @GET("/api/users/me")
    Call<UserResponse> getCurrentUser();

    @PUT("/api/users/me")
    Call<UserResponse> updateUser(@Body UpdateUserRequest request);
}
