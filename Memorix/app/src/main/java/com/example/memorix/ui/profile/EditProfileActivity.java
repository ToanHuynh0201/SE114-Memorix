package com.example.memorix.ui.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.UserApi;
import com.example.memorix.data.remote.dto.Token.TokenManager;
import com.example.memorix.data.remote.dto.User.UpdateUserRequest;
import com.example.memorix.data.remote.dto.User.UserResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText editName = findViewById(R.id.name_edit_text);
        EditText editEmail = findViewById(R.id.email_edit_text);
        EditText editPhone = findViewById(R.id.phone_edit_text);
        MaterialButton saveButton = findViewById(R.id.btn_save);

        // Load thông tin user từ SharedPreferences (hoặc ViewModel)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        editName.setText(prefs.getString("user_name", ""));
        editEmail.setText(prefs.getString("user_email", ""));
        editPhone.setText(prefs.getString("user_phone", ""));

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String email = editEmail.getText().toString();
            String phone = editPhone.getText().toString();

            updateUser(name, email, phone);
        });
    }

    private void updateUser(String name, String email, String phone) {
        UpdateUserRequest request = new UpdateUserRequest(name, email, phone);
        UserApi userApi = ApiClient.getClient().create(UserApi.class);

        userApi.updateUser(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    UserResponse updatedUser = response.body();
                    // Lưu lại hoặc hiển thị nếu cần
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
