package com.example.memorix.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memorix.R;
import com.example.memorix.ui.login.ChangePasswordActivity;
import com.example.memorix.ui.login.LoginActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountManagementFragment extends Fragment {
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView userPhoneTextView;
    private TextView currentLanguageTextView;
    private MaterialButton editProfileButton;
    private MaterialButton logoutButton;
    private View changePasswordLayout;
    private View languageLayout;

    public AccountManagementFragment() {
        // Required empty public constructor
    }
    public static AccountManagementFragment newInstance(String param1, String param2) {
        return  new AccountManagementFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập Toolbar (nếu cần)
        setupToolbar(view);

        // Khởi tạo các view
        initViews(view);

        // Lấy thông tin người dùng từ SharedPreferences hoặc UserModel
        loadUserData();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null && getActivity() != null) {
            // Nếu bạn sử dụng AppCompatActivity
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Hoặc có thể thiết lập navigation icon trực tiếp cho toolbar
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void initViews(View view) {
        userNameTextView = view.findViewById(R.id.user_name);
        userEmailTextView = view.findViewById(R.id.user_email);
        userPhoneTextView = view.findViewById(R.id.user_phone);
        currentLanguageTextView = view.findViewById(R.id.text_current_language);
        editProfileButton = view.findViewById(R.id.btn_edit_profile);
        logoutButton = view.findViewById(R.id.btn_logout);
        changePasswordLayout = view.findViewById(R.id.layout_change_password);
        languageLayout = view.findViewById(R.id.layout_language);
    }

    private void loadUserData() {
        // Đây là nơi bạn sẽ lấy dữ liệu từ SharedPreferences, Database hoặc API
        // Ví dụ:
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String userName = prefs.getString("user_name", "Nguyen Van A");
            String userEmail = prefs.getString("user_email", "nguyenvana@example.com");
            String userPhone = prefs.getString("user_phone", "+84 123 456 789");
            String language = prefs.getString("language", getString(R.string.vietnamese));

            // Hiển thị dữ liệu lên UI
            userNameTextView.setText(userName);
            userEmailTextView.setText(userEmail);
            userPhoneTextView.setText(userPhone);
            currentLanguageTextView.setText(language);
        }
    }

    private void setupClickListeners() {
        // Xử lý sự kiện khi click vào nút Chỉnh sửa hồ sơ
        editProfileButton.setOnClickListener(v -> {
            // Mở activity chỉnh sửa hồ sơ
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi click vào mục Đổi mật khẩu
        changePasswordLayout.setOnClickListener(v -> {
            // Mở activity đổi mật khẩu
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi click vào mục Ngôn ngữ
        languageLayout.setOnClickListener(v -> showLanguageDialog());

        // Xử lý sự kiện khi click vào nút Đăng xuất
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLanguageDialog() {
        if (getContext() == null) return;

        final String[] languages = {getString(R.string.vietnamese), getString(R.string.english)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.language))
                .setItems(languages, (dialog, which) -> {
                    // Lưu lựa chọn ngôn ngữ vào SharedPreferences
                    if (getContext() != null) {
                        SharedPreferences.Editor editor = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
                        editor.putString("language", languages[which]);
                        editor.apply();

                        // Cập nhật hiển thị trên UI
                        currentLanguageTextView.setText(languages[which]);

                        // Hiển thị thông báo
                        Toast.makeText(getContext(),
                                "Đã chọn: " + languages[which], Toast.LENGTH_SHORT).show();

                        // TODO: Thực hiện thay đổi ngôn ngữ ứng dụng
                        // Đoạn code thay đổi ngôn ngữ thực tế sẽ phức tạp hơn,
                        // cần cấu hình lại LocaleConfig hoặc restart Activity
                    }
                });
        builder.create().show();
    }

    private void showLogoutConfirmationDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, id) -> logout())
                .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void logout() {
        if (getContext() == null || getActivity() == null) return;

        // Xóa dữ liệu người dùng khỏi SharedPreferences
        SharedPreferences.Editor editor = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Hiển thị thông báo
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập và xóa stack
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}