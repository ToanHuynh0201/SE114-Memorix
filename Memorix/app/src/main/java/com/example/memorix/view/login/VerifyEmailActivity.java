package com.example.memorix.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.Register.VerifyEmailRequest;
import com.example.memorix.data.remote.dto.Register.VerifyEmailResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.view.MainActivity;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyEmailActivity extends AppCompatActivity {

    private EditText[] codeInputs = new EditText[6];
    private MaterialButton buttonVerify;
    private ProgressBar progressBar;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Missing user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        codeInputs[0] = findViewById(R.id.otp_digit_1);
        codeInputs[1] = findViewById(R.id.otp_digit_2);
        codeInputs[2] = findViewById(R.id.otp_digit_3);
        codeInputs[3] = findViewById(R.id.otp_digit_4);
        codeInputs[4] = findViewById(R.id.otp_digit_5);
        codeInputs[5] = findViewById(R.id.otp_digit_6);

        buttonVerify = findViewById(R.id.button_verify);
        progressBar = findViewById(R.id.progress_bar);

        setupOtpInputs();

        buttonVerify.setOnClickListener(v -> verifyCode());
    }

    private void setupOtpInputs() {
        for (int i = 0; i < codeInputs.length; i++) {
            final int index = i;
            codeInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < codeInputs.length - 1) {
                        codeInputs[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void verifyCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (EditText input : codeInputs) {
            String digit = input.getText().toString().trim();
            if (digit.isEmpty()) {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            codeBuilder.append(digit);
        }

        String code = codeBuilder.toString();
        buttonVerify.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        VerifyEmailRequest request = new VerifyEmailRequest(userId, code);
        String email = getIntent().getStringExtra("EMAIL");

        authApi.verifyEmail(request).enqueue(new Callback<VerifyEmailResponse>() {
            @Override
            public void onResponse(Call<VerifyEmailResponse> call, Response<VerifyEmailResponse> response) {
                progressBar.setVisibility(View.GONE);
                buttonVerify.setEnabled(true);
                String source = getIntent().getStringExtra("SOURCE");
                String email = getIntent().getStringExtra("EMAIL");
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(VerifyEmailActivity.this, "Email verified successfully", Toast.LENGTH_SHORT).show();
                    if ("google".equals(source)) {
                        // ✅ Nếu là từ Google Sign-In → vào thẳng MainActivity
                        Intent intent = new Intent(VerifyEmailActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // ✅ Nếu là từ đăng ký thủ công → về lại LoginActivity để đăng nhập
                        Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(VerifyEmailActivity.this, "Invalid or expired code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyEmailResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonVerify.setEnabled(true);
                Toast.makeText(VerifyEmailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
