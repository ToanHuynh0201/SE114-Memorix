package com.example.memorix.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.example.memorix.helper.MyFirebaseMessagingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String cachedAuthToken;
    private static final String DEFAULT_DEVICE_NAME = "Android Device";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String PENDING_FCM_TOKEN_KEY = "pending_fcm_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        //        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
////        prefs.edit().putString("access_token", "fake_or_expired_token").apply();
//        prefs.edit()
//                .putString("access_token", "fake_token")
//                .putString("refresh_token", "invalid_refresh_token")
//                .apply();


        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Get cached auth token
        cachedAuthToken = getAuthToken();
        checkAndRequestNotificationPermission();
        // Get FCM token
        initializeFCM();
        //MyFirebaseMessagingService.testLocalNotification(this);
        checkNotificationPermission();
        setupDebugNotification();
        // Setup navigation
        setupNavigation();
    }
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.w(TAG, "⚠️ Requesting notification permission...");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            } else {
                Log.d(TAG, "✅ Notification permission already granted");
            }
        } else {
            Log.d(TAG, "✅ Android < 13, notification permission not required");
        }
    }
    private void initializeFCM() {

        // Lấy FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);

                    // Try to send token to server immediately
                    sendTokenToServer(token);
                });
    }
    // Thêm trong onCreate() method:
    public void setupDebugNotification() {
        // Tạo button test notification (có thể add vào layout hoặc test bằng cách khác)
        // testLocalNotification();

        // Log FCM token để copy
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "=== FCM TOKEN FOR TESTING ===");
                    Log.d(TAG, token);
                    Log.d(TAG, "=== COPY THIS TOKEN ===");
                });
    }

    // Method để test local notification
    private void testLocalNotification() {
        MyFirebaseMessagingService.testLocalNotification(this);
        Toast.makeText(this, "Test notification sent - check notification panel", Toast.LENGTH_LONG).show();
    }

    // Method để check notification permission (Android 13+)
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.w(TAG, "⚠️ Notification permission not granted!");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            } else {
                Log.d(TAG, "✅ Notification permission granted");
            }
        }
    }
    private String getAuthToken() {
        if (cachedAuthToken != null && !cachedAuthToken.isEmpty()) {
            return cachedAuthToken;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cachedAuthToken = prefs.getString(ACCESS_TOKEN_KEY, null);
        return cachedAuthToken;
    }

    private void sendTokenToServer(String token) {
        String authToken = getAuthToken();

        if (authToken == null || authToken.isEmpty()) {
            Log.w(TAG, "No access token available, saving token for later registration");
            saveTokenForLater(token);
            return;
        }

        Log.d(TAG, "Sending FCM token to server...");

        // Create device object
        Device device = new Device(token, DEFAULT_DEVICE_NAME);

        // Create API service
        NotificationApi apiService = ApiClient.getClient().create(NotificationApi.class);

        // Make API call with Bearer prefix
        Call<DeviceResponse> call = apiService.registerDevice("Bearer " + authToken, device);

        call.enqueue(new Callback<DeviceResponse>() {
            @Override
            public void onResponse(Call<DeviceResponse> call, Response<DeviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ FCM token registered successfully");
                    DeviceResponse deviceResponse = response.body();
                    if (deviceResponse.getDevice() != null) {
                        Log.d(TAG, "Device ID: " + deviceResponse.getDevice().getDevice_id());
                        Log.d(TAG, "Device Name: " + deviceResponse.getDevice().getDevice_name());
                    }

                    // Clear any pending token since registration was successful
                    clearPendingToken();

                } else {
                    Log.e(TAG, "❌ Failed to register FCM token: " + response.code());
                    Log.e(TAG, "Response message: " + response.message());

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error while registering FCM token: " + t.getMessage(), t);
            }
        });
    }

    private void saveTokenForLater(String token) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(PENDING_FCM_TOKEN_KEY, token).apply();
        Log.d(TAG, "FCM token saved for later registration");
    }

    private void clearPendingToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(PENDING_FCM_TOKEN_KEY).apply();
    }

    // Call this method after successful login
    public void onLoginSuccess(String accessToken) {
        Log.d(TAG, "Login successful, updating auth token");

        // Save new access token
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(ACCESS_TOKEN_KEY, accessToken).apply();

        // Update cached token
        cachedAuthToken = accessToken;

        // Check if there's a pending FCM token to register
        String pendingToken = prefs.getString(PENDING_FCM_TOKEN_KEY, null);
        if (pendingToken != null) {
            Log.d(TAG, "Found pending FCM token, registering now...");
            sendTokenToServer(pendingToken);
        } else {
            // If no pending token, get current FCM token and register
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String currentToken = task.getResult();
                            if (currentToken != null) {
                                sendTokenToServer(currentToken);
                            }
                        }
                    });
        }
    }

    // Call this method when user logs out
    public void onLogout() {
        Log.d(TAG, "User logged out, clearing tokens");

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .remove(ACCESS_TOKEN_KEY)
                .remove(PENDING_FCM_TOKEN_KEY)
                .apply();

        cachedAuthToken = null;
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh auth token in case it was updated
        cachedAuthToken = getAuthToken();
    }
}