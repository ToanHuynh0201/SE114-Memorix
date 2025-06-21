package com.example.memorix.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.ResetPassword.ForgotPasswordRequest;
import com.example.memorix.data.remote.dto.ResetPassword.ForgotPasswordResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    //image_view_back, edit_text_email, button_reset_password;
    private ImageView imageViewBack;
    private TextInputEditText editTextEmail;
    private MaterialButton buttonResetPassword;
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        authApi = ApiClient.getClient().create(AuthApi.class); // Lấy Retrofit

        setupClickListeners();
    }

    private void setupClickListeners() {
        imageViewBack.setOnClickListener(v -> finish());

        buttonResetPassword.setOnClickListener(v -> {
            String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            ForgotPasswordRequest request = new ForgotPasswordRequest(email);

            authApi.forgotPassword(request).enqueue(new Callback<ForgotPasswordResponse>() {
                @Override
                public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ForgotPasswordActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("EMAIL", email); // truyền email nếu cần dùng lại
                        startActivity(intent);
                        finish(); // Quay lại màn hình trước (tùy chọn)
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e("ForgotPassword", "Server Error: " + errorBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(ForgotPasswordActivity.this, "Không thể gửi email đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    private void initViews() {
        imageViewBack = findViewById(R.id.image_view_back);
        editTextEmail = findViewById(R.id.edit_text_email);
        buttonResetPassword = findViewById(R.id.button_reset_password);
    }
}