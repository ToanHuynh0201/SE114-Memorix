package com.example.memorix.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.memorix.data.remote.api.NotificationApi;
import com.example.memorix.data.remote.dto.Notification.Device;
import com.example.memorix.data.remote.dto.Notification.DeviceResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.view.MainActivity;
import com.example.memorix.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "memorix_notifications";
    private static final String DEFAULT_DEVICE_NAME = "Android Device";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "=== NOTIFICATION RECEIVED ===");
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "Thread: " + Thread.currentThread().getName());
        Log.d(TAG, "Time: " + System.currentTimeMillis());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            sendNotification(title, body);
        }

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Handle data payload if needed
            handleDataPayload(remoteMessage.getData());
        }

        Log.d(TAG, "=== END NOTIFICATION ===");
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "=== NEW FCM TOKEN RECEIVED ===");
        Log.d(TAG, "New token: " + token);

        // Send the new token to your server
        sendRegistrationToServer(token);
    }

    private void handleDataPayload(java.util.Map<String, String> data) {
        // Handle custom data payload here
        // For example, you might want to navigate to a specific screen
        // or update local data based on the payload

        Log.d(TAG, "Handling data payload: " + data.toString());

        // Example: Check if there's a specific action to perform
        String action = data.get("action");
        if (action != null) {
            switch (action) {
                case "open_flashcards":
                    // Handle opening flashcards
                    break;
                case "update_progress":
                    // Handle progress update
                    break;
                default:
                    Log.d(TAG, "Unknown action: " + action);
                    break;
            }
        }
    }

    private String getAuthToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(ACCESS_TOKEN_KEY, null);
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "Attempting to send token to server...");

        String authToken = getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            Log.w(TAG, "No auth token found, saving token for later registration");
            saveTokenForLater(token);
            return;
        }

        // Create device object với đúng field names
        Device device = new Device(token, DEFAULT_DEVICE_NAME);

        // Create API service
        NotificationApi api = ApiClient.getClient().create(NotificationApi.class);
        Call<DeviceResponse> call = api.registerDevice("Bearer " + authToken, device);

        call.enqueue(new Callback<DeviceResponse>() {
            @Override
            public void onResponse(Call<DeviceResponse> call, Response<DeviceResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Device registered successfully from service");
                    DeviceResponse deviceResponse = response.body();
                    if (deviceResponse != null && deviceResponse.getDevice() != null) {
                        Log.d(TAG, "Device ID: " + deviceResponse.getDevice().getDevice_id());
                        Log.d(TAG, "Device Name: " + deviceResponse.getDevice().getDevice_name());
                    }
                } else {
                    Log.e(TAG, "❌ Failed to register device: " + response.code() + " - " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error while registering device: " + t.getMessage(), t);
            }
        });
    }

    private void saveTokenForLater(String token) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString("pending_fcm_token", token).apply();
        Log.d(TAG, "FCM token saved for later registration");
    }

    // Method để test notification local (call từ MainActivity để test)
    public static void testLocalNotification(Context context) {
        Log.d("NotificationTest", "Testing local notification...");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "memorix_notifications",
                    "Memorix Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for flashcard reviews");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, "memorix_notifications")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("🧪 Test Local Notification")
                        .setContentText("Nếu bạn thấy notification này, hệ thống đang hoạt động!")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(999, notificationBuilder.build());
        Log.d("NotificationTest", "✅ Local test notification sent");
    }

    private void sendNotification(String title, String messageBody) {
        Log.d(TAG, "Creating notification: " + title + " - " + messageBody);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Memorix Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for flashcard reviews");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);

            // Check if channel is disabled
            if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                Log.w(TAG, "⚠️ Notification channel is disabled!");
            }
        }

        // Sử dụng default Android icon nếu custom icon không có
        int iconResource;
        try {
            iconResource = R.drawable.ic_notification;
        } catch (Exception e) {
            // Fallback to default Android notification icon
            iconResource = android.R.drawable.ic_dialog_info;
            Log.w(TAG, "Using default notification icon because ic_notification not found");
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(iconResource)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody)); // Hiển thị full text

        notificationManager.notify(0, notificationBuilder.build());
        Log.d(TAG, "✅ Notification sent to system");
        Log.d(TAG, "Notification Channel: " + CHANNEL_ID);
        Log.d(TAG, "Notification ID: 0");

        // Check if notifications are enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            boolean areNotificationsEnabled = notificationManager.areNotificationsEnabled();
            Log.d(TAG, "Notifications enabled: " + areNotificationsEnabled);
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG, "FCM messages were deleted on server");
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
        Log.d(TAG, "FCM message sent: " + msgId);
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        super.onSendError(msgId, exception);
        Log.e(TAG, "FCM send error for message " + msgId, exception);
    }
}
