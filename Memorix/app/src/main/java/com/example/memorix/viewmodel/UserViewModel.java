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

    public UserViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<UserResponse> user() {
        return userLiveData;
    }
    public void fetchUser() {
        userRepository.getMe().observeForever(userLiveData::setValue);
    }

    public void updateUser(UpdateUserRequest request) {
        userRepository.updateMe(request).observeForever(userLiveData::setValue);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}

