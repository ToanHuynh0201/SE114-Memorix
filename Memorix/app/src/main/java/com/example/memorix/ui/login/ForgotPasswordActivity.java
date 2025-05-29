package com.example.memorix.ui.login;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    //image_view_back, edit_text_email, button_reset_password;
    private ImageView imageViewBack;
    private TextInputEditText editTextEmail;
    private MaterialButton buttonResetPassword;
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

        setupClickListeners();
    }

    private void setupClickListeners() {
        imageViewBack.setOnClickListener(v -> finish());

        buttonResetPassword.setOnClickListener(v -> {

        });
    }

    private void initViews() {
        imageViewBack = findViewById(R.id.image_view_back);
        editTextEmail = findViewById(R.id.edit_text_email);
        buttonResetPassword = findViewById(R.id.button_reset_password);
    }
}