package com.example.memorix;

import android.app.Application;
import com.example.memorix.data.remote.network.ApiClient;
public class MemorixApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo ApiClient với context của Application
        ApiClient.init(this);
    }
}
