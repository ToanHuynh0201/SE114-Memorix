package com.example.memorix.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.Register.RegisterRequest;
import com.example.memorix.data.remote.dto.Register.RegisterResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ImageView imageViewBack;
    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private MaterialButton buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        buttonRegister.setOnClickListener(v -> attemptRegister());
        // Thiết lập click listeners
        setupClickListeners();
    }
    private void initViews() {
        imageViewBack =findViewById(R.id.image_view_back);
        editTextFullName = findViewById(R.id.edit_text_full_name);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        buttonRegister = findViewById(R.id.button_register);
        textViewLogin = findViewById(R.id.text_view_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        imageViewBack.setOnClickListener(v -> finish());
        textViewLogin.setOnClickListener(v -> finish());
    }
    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    private void attemptRegister() {
        String username = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill in all fields");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email format");
            return;
        }
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false);

        RegisterRequest request = new RegisterRequest(username, email, password);
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        authApi.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    String welcomeMsg = "Welcome, " + response.body().getUsername();
                    showToast(welcomeMsg);
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", email); // truyền email
                    startActivity(intent);
                    finish(); // Quay lại màn đăng nhập
                } else if (response.code() == 400 && response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        JSONArray errors = jsonObject.getJSONArray("errors");
                        String msg = errors.getJSONObject(0).getString("msg");
                        showToast(msg);
                    } catch (Exception e) {
                        showToast("Invalid input");
                    }
                } else {
                    showToast("Registration failed");
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                showToast("Error: " + t.getMessage());
            }
        });
    }
}