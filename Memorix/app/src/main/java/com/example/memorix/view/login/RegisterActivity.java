package com.example.memorix.view.login;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.memorix.data.remote.dto.GoogleLogin.GoogleLoginRequest;
import com.example.memorix.data.remote.dto.Login.LoginResponse;
import com.example.memorix.data.remote.dto.Register.RegisterRequest;
import com.example.memorix.data.remote.dto.Register.RegisterResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.ui.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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

    private static final int RC_SIGN_IN = 1002; // ID riêng cho RegisterActivity
    private GoogleSignInClient mGoogleSignInClient;

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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ file google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

// Gán click cho nút đăng ký Google
        MaterialButton googleSignInButton = findViewById(R.id.btn_google_sign_in);
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
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
    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                sendGoogleTokenToServer(idToken);
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                Log.e("GOOGLE_REGISTER", "Google SignIn failed", e);
            }
        }
    }
    private void sendGoogleTokenToServer(String idToken) {
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        GoogleLoginRequest request = new GoogleLoginRequest(idToken);

        authApi.loginWithGoogle(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (!loginResponse.getUser().isVerified()) {
                        Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("USER_ID", loginResponse.getUser().getUserId());
                        intent.putExtra("EMAIL", loginResponse.getUser().getEmail());
                        intent.putExtra("SOURCE", "google");
                        startActivity(intent);
                        finish();
                    } else {
                        // Lưu access_token và vào MainActivity
                        Toast.makeText(RegisterActivity.this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();

                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("access_token", loginResponse.getAccess_token());
                        editor.putString("refresh_token", loginResponse.getRefresh_token());
                        editor.apply();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng kí bằng Google thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
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
                    String msg = "Verification code sent to " + response.body().getEmail();
                    showToast(msg);

                    Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                    intent.putExtra("USER_ID", response.body().getUser_id());
                    intent.putExtra("EMAIL", response.body().getEmail());
                    intent.putExtra("SOURCE", "manual");
// 👈 truyền user_id
                    startActivity(intent);
                } else if (response.code() == 400 && response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        JSONArray errors = jsonObject.getJSONArray("errors");
                        String msg = errors.getJSONObject(0).getString("msg");

                        if (msg.equalsIgnoreCase("Email already exists")) {
                            showToast("Email đã được đăng ký. Nếu dùng Google, hãy đăng nhập bằng Google.");
                        } else {
                            showToast(msg);
                        }

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