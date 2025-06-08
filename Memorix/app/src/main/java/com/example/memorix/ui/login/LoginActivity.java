package com.example.memorix.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.memorix.R;
import com.example.memorix.ui.MainActivity;
import com.example.memorix.network.ApiClient;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.LoginRequest;
import com.example.memorix.data.remote.dto.LoginResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private TextView textViewRegister;
    private TextView textViewForgotPassword;
    private TextInputEditText editTextAccount;
    private TextInputEditText editTextPassword;
    private MaterialCheckBox checkboxRememberPassword;
    private MaterialButton buttonLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("access_token", null);
        if (savedToken != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        initViews();

        // Thiết lập click listeners
        setupClickListeners();
    }

    private void initViews() {
        textViewRegister = findViewById(R.id.text_view_register);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        editTextAccount = findViewById(R.id.edit_text_account);
        editTextPassword = findViewById(R.id.edit_text_password);
        checkboxRememberPassword = findViewById(R.id.checkbox_remember_password);
        buttonLogin = findViewById(R.id.button_login);
    }
    private void loginUser(String email, String password) {
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    String accessToken = loginResponse.getAccess_token();
                    String refreshToken = loginResponse.getRefresh_token();
                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_token", accessToken);
                    editor.putString("refresh_token", refreshToken); // nếu cần
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        // Click listener cho Register TextView
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Click listener cho Forgot Password TextView
        textViewForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        buttonLogin.setOnClickListener(v -> {
            String email = editTextAccount.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });
    }
}