package com.example.memorix.view;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.NotificationApi;
import com.example.memorix.data.remote.dto.Notification.Device;
import com.example.memorix.data.remote.dto.Notification.DeviceResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class  MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String cachedAuthToken;
    private static final String DEFAULT_DEVICE_NAME = "Android Device";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Lấy FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);
                    sendTokenToServer(token);
                });

        setupNavigation();
        cachedAuthToken = getAuthToken();
    }

    private String getAuthToken() {
        if (cachedAuthToken != null) {
            return cachedAuthToken;
        }
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        cachedAuthToken = prefs.getString("access_token", null);
        return cachedAuthToken;
    }
    private void sendTokenToServer(String token) {
        if (cachedAuthToken == null || cachedAuthToken.isEmpty()) {
            Log.w(TAG, "No access token available, cannot register device");
            return;
        }

        // Sử dụng tên mặc định
        Device device = new Device(token, DEFAULT_DEVICE_NAME);

        NotificationApi apiService = ApiClient.getClient().create(NotificationApi.class);

        Call<DeviceResponse> call = apiService.createDevice(cachedAuthToken, device);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DeviceResponse> call, Response<DeviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Token sent to server successfully");
                    Log.d(TAG, "Device ID: " + response.body().getDevice().getDevice_id());
                    Log.d(TAG, "Device Name: " + DEFAULT_DEVICE_NAME);


                } else {
                    Log.e(TAG, "Failed to send token to server: " + response.code());
                    Log.e(TAG, "Response message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DeviceResponse> call, Throwable t) {
                Log.e(TAG, "Error sending token to server: " + t.getMessage());
            }
        });
    }
    private void setupNavigation() {
        findViewById(R.id.nav_host_fragment).post(() -> {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                NavigationUI.setupWithNavController(bottomNav, navController);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}