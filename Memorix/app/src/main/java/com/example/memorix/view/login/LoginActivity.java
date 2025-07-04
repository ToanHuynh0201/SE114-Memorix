package com.example.memorix.view.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.memorix.R;
import com.example.memorix.data.remote.dto.GoogleLogin.GoogleLoginRequest;
import com.example.memorix.helper.HideSoftKeyboard;
import com.example.memorix.view.MainActivity;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.Login.LoginRequest;
import com.example.memorix.data.remote.dto.Login.LoginResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;


import org.json.JSONObject;

import java.util.Objects;

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
    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;


    private boolean biometricPromptShown = false;
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
        HideSoftKeyboard.setupHideKeyboard(this, findViewById(R.id.main));
        initViews();

        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean remember = loginPrefs.getBoolean("remember_password", false);
        checkboxRememberPassword.setChecked(remember);
        editTextAccount.setText("");
        editTextPassword.setText("");
        setupClickListeners();
        String passedEmail = getIntent().getStringExtra("EMAIL");
        if (passedEmail != null) {
            editTextAccount.setText(passedEmail);
        }
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedToken = prefs.getString("access_token", null);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (savedToken != null && isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

//        if (remember) {
//            String savedEmail = prefs.getString("saved_email", "");
//            String savedPassword = prefs.getString("saved_password", "");
//
//            if (!savedEmail.isEmpty()) {
//                editTextAccount.setText(savedEmail);
//                if (!savedPassword.isEmpty()) {
//                    editTextPassword.setText(savedPassword);
//                }
//            }
//        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ client_id.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

// Gắn sự kiện cho nút Google
        MaterialButton googleSignInButton = findViewById(R.id.btn_google_sign_in);
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());


        setupClickListeners();

        TextInputLayout emailInputLayout = findViewById(R.id.text_input_layout_email);

        emailInputLayout.setEndIconOnClickListener(v -> {
            SharedPreferences LoginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String savedEmail =LoginPrefs.getString("saved_email", "");
            String savedPassword = LoginPrefs.getString("saved_password", "");

            String currentEmail = editTextAccount.getText().toString().trim();

            if (currentEmail.equals(savedEmail) && !savedPassword.isEmpty()) {
                showBiometricPrompt(() -> {
                    editTextPassword.setText(savedPassword);
                }); // ✅ Hàm đã có rồi
            } else {
                Toast.makeText(this, "Email không khớp hoặc chưa lưu mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
        emailInputLayout.setEndIconVisible(false);
        // Thêm TextWatcher cho editTextAccount để khi nhập đúng email đã lưu thì load password
        editTextAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                biometricPromptShown = false;
                String inputEmail = s.toString().trim();

                if (Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                    SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    String savedEmail = prefs.getString("saved_email", "");
                    String savedPassword = prefs.getString("saved_password", "");
                    boolean remember = prefs.getBoolean("remember_password", false);

                    if (inputEmail.equals(savedEmail) && remember && !biometricPromptShown) {
                        emailInputLayout.setEndIconVisible(true);
                        biometricPromptShown = true; // tránh gọi lại nhiều lần

                        showBiometricPrompt(() -> {
                            editTextPassword.setText(savedPassword);
                        });
                    } else {
                        editTextPassword.setText("");
                    }
                } else {
                    editTextPassword.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showBiometricPrompt(Runnable onSuccess) {
        androidx.biometric.BiometricPrompt biometricPrompt = new androidx.biometric.BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        runOnUiThread(onSuccess); // callback
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(LoginActivity.this, "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(LoginActivity.this, "Lỗi xác thực: " + errString, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        androidx.biometric.BiometricPrompt.PromptInfo promptInfo =
                new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Xác thực vân tay")
                        .setSubtitle("Xác thực để tự động điền mật khẩu")
                        .setNegativeButtonText("Hủy")
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.d("LOGIN_DEBUG", "Received result from Google Sign-In");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                String email = account.getEmail();
                Log.d("LOGIN_DEBUG", "Google account email: " + email);
                Log.d("LOGIN_DEBUG", "Google ID Token: " + idToken);

                // Gửi idToken lên server backend của bạn
                sendGoogleTokenToServer(idToken);
            } catch (ApiException e) {
                Log.e("LOGIN_DEBUG", "Google Sign-in failed", e);
                e.printStackTrace();
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendGoogleTokenToServer(String idToken) {
        Log.d("LOGIN_DEBUG", "Sending idToken to backend: " + idToken);
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        GoogleLoginRequest request = new GoogleLoginRequest(idToken);

        authApi.loginWithGoogle(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (!loginResponse.getUser().isVerified()) {
                        // Nếu chưa xác thực → mở VerifyEmailActivity
                        Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("USER_ID", loginResponse.getUser().getUserId());
                        intent.putExtra("EMAIL", loginResponse.getUser().getEmail());
                        intent.putExtra("SOURCE", "google");
                        startActivity(intent);
                        finish();
                    } else {
                        // Nếu đã xác thực → vào MainActivity
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();

                        // Lưu access_token nếu muốn
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("access_token", loginResponse.getAccess_token());
                        editor.putString("refresh_token", loginResponse.getRefresh_token());
                        editor.putBoolean("user_verified", loginResponse.getUser().isVerified());
                        editor.putString("user_name", loginResponse.getUser().getUsername());
                        editor.putString("user_email", loginResponse.getUser().getEmail());
                        editor.putString("user_phone", loginResponse.getUser().getPhone());
                        editor.putString("user_image", loginResponse.getUser().getImageUrl());
                        editor.putBoolean("isLoggedIn", true);

                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        JSONObject json = new JSONObject(errorBody);

                        String errorMessage = "";
                        if (json.has("message")) {
                            errorMessage = json.getString("message");
                        } else if (json.has("error")) {
                            errorMessage = json.getString("error");
                        } else if (json.has("detail")) {
                            errorMessage = json.getString("detail");
                        }

                        Log.e("GOOGLE_LOGIN_ERROR", "Lỗi Google Login: " + errorMessage);

                        if (errorMessage.equalsIgnoreCase("Google account has no email")) {
                            Toast.makeText(LoginActivity.this, "Tài khoản Google không có email. Vui lòng dùng tài khoản khác.", Toast.LENGTH_LONG).show();
                        } else if (errorMessage.equalsIgnoreCase("Invalid Google token")) {
                            Toast.makeText(LoginActivity.this, "Token Google không hợp lệ. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                    }

                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String extractEmailFromToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) return null;

            String payloadJson = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
            JSONObject payload = new JSONObject(payloadJson);
            return payload.getString("email");
        } catch (Exception e) {
            Log.e("TokenDecode", "Failed to extract email", e);
            return null;
        }
    }

    private void initViews() {
        textViewRegister = findViewById(R.id.text_view_register);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        editTextAccount = findViewById(R.id.edit_text_account);
        String emailFromRegister = getIntent().getStringExtra("email");
        if (emailFromRegister != null) {
            editTextAccount.setText(emailFromRegister);
        }
        editTextPassword = findViewById(R.id.edit_text_password);
        checkboxRememberPassword = findViewById(R.id.checkbox_remember_password);
        buttonLogin = findViewById(R.id.button_login);
    }
    private void loginUser(String email, String password) {
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<>() {
            @Override

            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.code() == 403) {
                    try {
                        // Ưu tiên đọc từ body nếu có
                        if (response.body() != null) {
                            String msg = "Verification code sent to " + response.body().getEmail();
                            showToast(msg);

                            int userId = response.body().getUser().getUserId(); // đảm bảo là int
                            Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                            intent.putExtra("USER_ID", userId);
                            intent.putExtra("EMAIL", response.body().getEmail());
                            intent.putExtra("SOURCE", "manual");
                            startActivity(intent);

                            Toast.makeText(LoginActivity.this, "Email chưa được xác thực. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Nếu body null → đọc từ errorBody
                        String errorBodyString = response.errorBody() != null ? response.errorBody().string() : "";
                        JSONObject json = new JSONObject(errorBodyString);

                        String email = json.optString("email", editTextAccount.getText().toString());
                        String userIdStr = json.optString("user_id", null); // kiểu chuỗi
                        Log.d("LOGIN_DEBUG", "user_id: " + userIdStr + ", email: " + email);

                        if (userIdStr != null && email != null) {
                            try {
                                int userId = Integer.parseInt(userIdStr); // ép kiểu an toàn
                                Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                                intent.putExtra("USER_ID", userId);
                                intent.putExtra("EMAIL", email);
                                intent.putExtra("SOURCE", "manual");
                                startActivity(intent);
                            } catch (NumberFormatException e) {
                                Log.e("LOGIN_DEBUG", "user_id không hợp lệ: " + userIdStr);
                                Toast.makeText(LoginActivity.this, "Không thể mở xác thực email vì user_id không hợp lệ.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e("LOGIN_DEBUG", "Thiếu user_id hoặc email → không thể mở VerifyEmailActivity");
                            Toast.makeText(LoginActivity.this, "Không thể mở xác thực email vì thiếu thông tin.", Toast.LENGTH_LONG).show();
                        }

                        Toast.makeText(LoginActivity.this, "Email chưa được xác thực. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Lỗi khi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                    }

                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    String accessToken = loginResponse.getAccess_token();
                    String refreshToken = loginResponse.getRefresh_token();

                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor loginEditor = loginPrefs.edit();

                    editor.putString("access_token", accessToken);
                    editor.putString("refresh_token", refreshToken);

                    editor.putBoolean("user_verified", loginResponse.getUser().isVerified());
                    editor.putString("user_name", loginResponse.getUser().getUsername());
                    editor.putString("user_email", loginResponse.getUser().getEmail());
                    editor.putString("user_phone", loginResponse.getUser().getPhone());
                    editor.putString("user_image", loginResponse.getUser().getImageUrl());
                    editor.putBoolean("isLoggedIn", true);



                    if (checkboxRememberPassword.isChecked()) {
                        loginEditor.putString("saved_email", email);
                        loginEditor.putString("saved_password", password);
                        loginEditor.putBoolean("remember_password", true);
                    } else {
                        loginEditor.remove("saved_email");
                        loginEditor.remove("saved_password");
                        loginEditor.putBoolean("remember_password", false);
                    }
                    loginEditor.apply();
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorBodyString = response.errorBody().string();
                        JSONObject json = new JSONObject(errorBodyString);
                        Log.d("LOGIN_DEBUG", "Error body: " + errorBodyString);

                        // Thử lấy message theo nhiều cách (tùy backend)
                        String errorMessage = "";
                        if (json.has("message")) {
                            errorMessage = json.getString("message");
                        } else if (json.has("error")) {
                            errorMessage = json.getString("error");
                        } else if (json.has("detail")) {
                            errorMessage = json.getString("detail");
                        }

                        Log.e("LOGIN_ERROR", "Lỗi từ server: " + errorMessage); // kiểm tra log

                        if (errorMessage.equalsIgnoreCase("Invalid credentials")) {
                            Toast.makeText(LoginActivity.this, "Vui lòng đăng nhập bằng Google.", Toast.LENGTH_SHORT).show();

                        } else if (errorMessage.equalsIgnoreCase("Email not verified")) {
                            Toast.makeText(LoginActivity.this, "Email chưa được xác thực. Vui lòng kiểm tra hộp thư để xác thực.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e("LOGIN_DEBUG", "onFailure: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }
    private void signInWithGoogle() {
        Log.d("LOGIN_DEBUG", "User clicked Google Sign-In button");
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // Đảm bảo đã đăng xuất trước khi gọi sign-in
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
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
            String email = Objects.requireNonNull(editTextAccount.getText()).toString().trim();
            String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

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
    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}