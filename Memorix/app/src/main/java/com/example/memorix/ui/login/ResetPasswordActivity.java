package com.example.memorix.ui.login;

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
import com.example.memorix.data.remote.dto.ResetPassword.ResetPasswordRequest;
import com.example.memorix.data.remote.dto.ResetPassword.ResetPasswordResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ResetPasswordActivity
 * ---------------------------------------------------
 * INPUTS
 *   • 16‑char token (user paste from email)
 *   • New password (min 6 chars)
 * OUTCOME
 *   • Calls POST /api/auth/reset-password
 *   • On success   → Toast + navigate LoginActivity
 *   • On error     → Toast error message
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextToken;
    private EditText editTextNewPassword;
    private MaterialButton buttonReset;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        editTextToken       = findViewById(R.id.edit_text_token);
        editTextNewPassword = findViewById(R.id.edit_text_new_password);
        buttonReset         = findViewById(R.id.button_reset);
        progressBar         = findViewById(R.id.progress_bar);

        // Auto‑trim token length to 16 chars


        buttonReset.setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        String token    = editTextToken.getText().toString().trim();
        String password = editTextNewPassword.getText().toString().trim();

        if (token.isEmpty()) {
            Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonReset.setEnabled(false);

        AuthApi api = ApiClient.getClient().create(AuthApi.class);
        ResetPasswordRequest req = new ResetPasswordRequest(token, password);

        api.resetPassword(req).enqueue(new Callback<ResetPasswordResponse>() {
            @Override public void onResponse(Call<ResetPasswordResponse> call, Response<ResetPasswordResponse> res) {
                progressBar.setVisibility(View.GONE);
                buttonReset.setEnabled(true);
                if(res.isSuccessful() && res.body()!=null){
                    Toast.makeText(ResetPasswordActivity.this, res.body().getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Invalid token or server error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ResetPasswordResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonReset.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
