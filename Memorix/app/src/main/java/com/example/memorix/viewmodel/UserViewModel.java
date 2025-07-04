package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.memorix.data.remote.Repository.UserRepository;
import com.example.memorix.data.remote.dto.User.UpdateUserRequest;
import com.example.memorix.data.remote.dto.User.UserResponse;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<UserResponse> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public UserViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<UserResponse> user() {
        return userLiveData;
    }
    public LiveData<Boolean> updateSuccess() {
        return updateSuccess;
    }
    public void fetchUser() {
        userRepository.getMe().observeForever(userLiveData::setValue);
    }

    public void updateUser(UpdateUserRequest request) {
        userRepository.updateMe(request).observeForever(response -> {
            if (response != null) {
                userLiveData.setValue(response);
                updateSuccess.setValue(true);
            } else {
                updateSuccess.setValue(false);
            }
        });
    }
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}

