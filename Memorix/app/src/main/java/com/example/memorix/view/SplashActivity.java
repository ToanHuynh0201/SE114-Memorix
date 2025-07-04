
package com.example.memorix.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.Login.LoginResponse;
import com.example.memorix.data.remote.dto.Token.RefreshTokenRequest;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.view.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // Animation duration and delay constants
    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private static final int CARD_ANIMATION_DURATION = 1500;
    private static final int LOGO_ANIMATION_DURATION = 1200;
    private static final int TEXT_ANIMATION_DURATION = 1000;

    // UI Elements
    private ImageView logoImageView;
    private TextView appNameTextView;
    private TextView taglineTextView;
    private final List<CardView> flashcards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoImageView = findViewById(R.id.logo_image);
        appNameTextView = findViewById(R.id.app_name_text);
        taglineTextView = findViewById(R.id.tagline_text);

        // Initialize flashcards
        flashcards.add(findViewById(R.id.flashcard1));
        flashcards.add(findViewById(R.id.flashcard2));
        flashcards.add(findViewById(R.id.flashcard3));
        flashcards.add(findViewById(R.id.flashcard4));
        flashcards.add(findViewById(R.id.flashcard5));


        // Hide elements initially for animation
        logoImageView.setAlpha(0f);
        appNameTextView.setAlpha(0f);
        taglineTextView.setAlpha(0f);
        Log.d("SPLASH_DEBUG", "SplashActivity.onCreate() được gọi");


        for (CardView card : flashcards) {
            card.setAlpha(0f);
            card.setScaleX(0.5f);
            card.setScaleY(0.5f);
        }


        // Start animations after a short delay
        new Handler().postDelayed(this::startAnimations, 100);

        // Navigate to main activity after splash duration
        new Handler().postDelayed(() -> {
            if (isLoggedIn()) {
                checkTokenValidity(); // ← thêm dòng này
            } else {
                goToLogin();
            }
        }, SPLASH_DURATION);
    }
    private void checkTokenValidity() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String refreshToken = prefs.getString("refresh_token", null);
        Log.d("SPLASH_DEBUG", "checkTokenValidity() -> refresh_token: " + refreshToken);
        if (refreshToken == null) {
            Log.d("SPLASH_DEBUG", "No refresh token → redirect to login");
            goToLogin(); // Không có refresh token => chưa đăng nhập => Login
            return;
        }

        AuthApi api = ApiClient.getClient().create(AuthApi.class);
        Call<LoginResponse> call = api.refreshToken(new RefreshTokenRequest(refreshToken));

        call.enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SPLASH_DEBUG", "Token refresh success");
                    // Lưu token mới
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("access_token", response.body().getAccess_token());
                    editor.putString("refresh_token", response.body().getRefresh_token());
                    editor.apply();

                    Log.d("SPLASH_DEBUG", "→ Gọi xác thực vân tay");
                    // ✅ Token còn hạn → tiếp tục gọi xác thực vân tay
                    authenticateAndProceed();
                } else {
                    Log.d("SPLASH_DEBUG", "Token refresh failed: " + response.code());
                    // ❌ Refresh token hết hạn → logout & vào Login luôn
                    logoutAndGoToLogin();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.d("SPLASH_DEBUG", "Token refresh failed (network): " + t.getMessage());
                // ❌ Lỗi mạng → cũng logout an toàn
                logoutAndGoToLogin();
            }
        });
    }
    private void authenticateAndProceed() {
        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {


                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        goToMain(); // Thành công → vào MainActivity
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        logoutAndGoToLogin(); // Hủy hoặc lỗi → logout
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Có thể cho Toast nhẹ nếu muốn
                    }
                }
        );
        Log.d("SPLASH_DEBUG", "Gọi xác thực vân tay");
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực để tiếp tục")
                .setSubtitle("Xác nhận vân tay để vào ứng dụng")
                .setNegativeButtonText("Hủy")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void goToMain() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }

    private void logoutAndGoToLogin() {
        // Xoá toàn bộ token và trạng thái đăng nhập
        getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

        goToLogin();
    }
    private boolean isLoggedIn() {
        boolean result = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getBoolean("isLoggedIn", false);

        Log.d("SPLASH_DEBUG", "isLoggedIn() -> " + result);
        return result;
    }


    private void startAnimations() {
        // Tạo AnimatorSet riêng cho từng loại animation
        AnimatorSet flashcardAnimSet = new AnimatorSet();
        AnimatorSet logoAnimSet = new AnimatorSet();
        AnimatorSet textAnimSet;

        // List animator cho từng nhóm
        List<Animator> flashcardAnimators = new ArrayList<>();
        List<Animator> logoAnimators = new ArrayList<>();

        // Flashcard animations
        for (int i = 0; i < flashcards.size(); i++) {
            CardView card = flashcards.get(i);

            AnimatorSet cardAnimSet = new AnimatorSet();
            List<Animator> cardAnimators = new ArrayList<>();

            // Create card appear animation
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f);
            fadeIn.setDuration(CARD_ANIMATION_DURATION);

            // Scale animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, View.SCALE_X, 0.5f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.5f, 1.0f);
            scaleX.setDuration(CARD_ANIMATION_DURATION);
            scaleY.setDuration(CARD_ANIMATION_DURATION);

            // Rotation animation
            ObjectAnimator rotation = ObjectAnimator.ofFloat(card, View.ROTATION,
                    new Random().nextInt(40) - 20, 0f);
            rotation.setDuration(CARD_ANIMATION_DURATION);
            rotation.setInterpolator(new AnticipateOvershootInterpolator());

            cardAnimators.add(fadeIn);
            cardAnimators.add(scaleX);
            cardAnimators.add(scaleY);
            cardAnimators.add(rotation);

            cardAnimSet.playTogether(cardAnimators);
            cardAnimSet.setStartDelay(i * 100L);

            flashcardAnimators.add(cardAnimSet);
        }

        // Logo animations
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0f, 1f);
        logoFadeIn.setDuration(LOGO_ANIMATION_DURATION);

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 0.5f, 1.0f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 0.5f, 1.0f);
        logoScaleX.setDuration(LOGO_ANIMATION_DURATION);
        logoScaleY.setDuration(LOGO_ANIMATION_DURATION);
        logoScaleX.setInterpolator(new DecelerateInterpolator());
        logoScaleY.setInterpolator(new DecelerateInterpolator());

        logoAnimators.add(logoFadeIn);
        logoAnimators.add(logoScaleX);
        logoAnimators.add(logoScaleY);

        // App name text animation
        ObjectAnimator appNameFadeIn = ObjectAnimator.ofFloat(appNameTextView, View.ALPHA, 0f, 1f);
        appNameFadeIn.setDuration(TEXT_ANIMATION_DURATION);

        ObjectAnimator appNameTranslateY = ObjectAnimator.ofFloat(appNameTextView, View.TRANSLATION_Y, 50f, 0f);
        appNameTranslateY.setDuration(TEXT_ANIMATION_DURATION);
        appNameTranslateY.setInterpolator(new AccelerateDecelerateInterpolator());

        // Tagline text animation - đồng bộ với app name
        ObjectAnimator taglineFadeIn = ObjectAnimator.ofFloat(taglineTextView, View.ALPHA, 0f, 1f);
        taglineFadeIn.setDuration(TEXT_ANIMATION_DURATION);

        ObjectAnimator taglineTranslateY = ObjectAnimator.ofFloat(taglineTextView, View.TRANSLATION_Y, 30f, 0f);
        taglineTranslateY.setDuration(TEXT_ANIMATION_DURATION);
        taglineTranslateY.setInterpolator(new AccelerateDecelerateInterpolator());

        // Tạo AnimatorSet cho phần text (app name và tagline cùng nhau)
        textAnimSet = new AnimatorSet();
        textAnimSet.playTogether(
                appNameFadeIn, appNameTranslateY,
                taglineFadeIn, taglineTranslateY
        );
        textAnimSet.setStartDelay(700); // Một delay duy nhất cho cả app name và tagline

        // Phối hợp các animation
        flashcardAnimSet.playTogether(flashcardAnimators);

        logoAnimSet.playTogether(logoAnimators);
        logoAnimSet.setStartDelay(300);

        // AnimatorSet chính để chạy tất cả
        AnimatorSet mainAnimSet = new AnimatorSet();
        mainAnimSet.playTogether(flashcardAnimSet, logoAnimSet, textAnimSet);
        mainAnimSet.start();
    }
}