package com.example.memorix.data.remote.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorix.data.remote.api.UserApi;
import com.example.memorix.data.remote.dto.User.UpdateUserRequest;
import com.example.memorix.data.remote.dto.User.UserResponse;
import com.example.memorix.data.remote.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;

    public UserRepository() {
        userApi = ApiClient.getClient().create(UserApi.class);
    }

    public LiveData<UserResponse> getMe() {
        MutableLiveData<UserResponse> userLiveData = new MutableLiveData<>();

        userApi.getCurrentUser().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    userLiveData.postValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                userLiveData.postValue(null);
            }
        });

        return userLiveData;
    }

    public LiveData<UserResponse> updateMe(UpdateUserRequest request) {
        MutableLiveData<UserResponse> result = new MutableLiveData<>();

        userApi.updateUser(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                result.postValue(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                result.postValue(null);
            }
        });

        return result;
    }
}

