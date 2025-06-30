package com.example.memorix.view.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.UserApi;
import com.example.memorix.data.remote.dto.User.UpdateUserRequest;
import com.example.memorix.data.remote.dto.User.UserResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.viewmodel.UserViewModel;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.InputStream;


public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private Uri selectedImageUri;
    private String currentImageUrl;

    private UserViewModel userViewModel;

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

        // Ánh xạ các view
        de.hdodenhof.circleimageview.CircleImageView profileImage = findViewById(R.id.profile_image);
        EditText editName = findViewById(R.id.name_edit_text);
        EditText editEmail = findViewById(R.id.email_edit_text);
        EditText editPhone = findViewById(R.id.phone_edit_text);
        MaterialButton saveButton = findViewById(R.id.btn_save);

        // Load thông tin user từ SharedPreferences
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.fetchUser();

        userViewModel.user().observe(this, user -> {
            if (user != null) {
                editName.setText(user.getUsername());
                editEmail.setText(user.getEmail());
                editPhone.setText(user.getPhone());
                currentImageUrl = user.getImage_url();

                if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(currentImageUrl)
                            .placeholder(R.drawable.ic_memorix_logo)
                            .error(R.drawable.ic_memorix_logo)
                            .into(profileImage);
                }

                // Cache xuống SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                editor.putString("user_name", user.getUsername());
                editor.putString("user_email", user.getEmail());
                editor.putString("user_phone", user.getPhone());
                editor.putString("user_image", user.getImage_url());
                editor.putBoolean("user_verified", user.isVerified());
                editor.apply();
            }
        });





        // Bắt sự kiện chọn ảnh mới
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });

        // Xử lý khi bấm nút lưu
        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String email = editEmail.getText().toString();
            String phone = editPhone.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.isEmpty() && !phone.matches("^\\+?[0-9]{9,15}$")) {
                Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            UserResponse currentUser = userViewModel.user().getValue();
            if (currentUser == null) {
                Toast.makeText(this, "Không lấy được trạng thái xác thực người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isVerified = currentUser.isVerified(); // ✅ chuẩn xác


            UpdateUserRequest request = new UpdateUserRequest(name, email, phone, isVerified, currentImageUrl);
            Log.d("UPDATE_DEBUG", "Body: " + new Gson().toJson(request));
            userViewModel.updateUser(request);
            Toast.makeText(this, "Đã gửi yêu cầu cập nhật", Toast.LENGTH_SHORT).show();
            finish();
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                de.hdodenhof.circleimageview.CircleImageView profileImage = findViewById(R.id.profile_image);
                profileImage.setImageURI(selectedImageUri);
                String base64 = convertImageToBase64(selectedImageUri);

                if (base64 != null) {
                    currentImageUrl = base64;
                    Toast.makeText(this, "Đã chọn ảnh và chuẩn bị upload", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi chuyển ảnh", Toast.LENGTH_SHORT).show();
                }
                currentImageUrl = convertImageToBase64(selectedImageUri);


            }
        }
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);

            // Định dạng chuẩn base64 với tiền tố Data URI
            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            return "data:image/jpeg;base64," + base64; // hoặc image/png tùy loại ảnh
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
