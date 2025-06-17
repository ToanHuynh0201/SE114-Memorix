package com.example.memorix.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.memorix.ui.login.LoginActivity;

public class LogoutHelper {

    // Dùng ở mọi nơi, tự đảm bảo chạy trên UI thread
    public static void safeLogout(Context context) {
        // Luôn chạy trên UI thread để tránh crash khi startActivity
        new Handler(Looper.getMainLooper()).post(() -> {
            // Xoá token
            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("access_token");
            editor.remove("refresh_token");
            editor.apply();

            // Thông báo
            Toast.makeText(context, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();

            // Điều hướng về LoginActivity
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}