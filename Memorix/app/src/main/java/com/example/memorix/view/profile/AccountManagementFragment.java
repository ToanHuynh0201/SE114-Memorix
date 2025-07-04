package com.example.memorix.view.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.AuthApi;
import com.example.memorix.data.remote.dto.LogOut.LogoutRequest;
import com.example.memorix.data.remote.dto.Token.TokenManager;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.helper.LogoutHelper;
import com.example.memorix.view.login.ChangePasswordActivity;
import com.example.memorix.view.login.LoginActivity;
import com.example.memorix.viewmodel.UserViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountManagementFragment extends Fragment {

    private UserViewModel userViewModel;
    private TokenManager tokenManager;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView userPhoneTextView;
    private TextView learningMethodTextView; // Thay đổi từ currentLanguageTextView
    private MaterialButton editProfileButton;
    private MaterialButton logoutButton;
    private View changePasswordLayout;
    private View learningMethodLayout; // Thay đổi từ languageLayout

    private ImageView userAvatarImageView;
    private ActivityResultLauncher<Intent> editProfileLauncher;



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

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        userViewModel.fetchUser(); // Gọi lại API sau khi EditProfile xong
                    }
                }
        );

        // Thiết lập Toolbar (nếu cần)
        setupToolbar(view);

        // Khởi tạo các view
        initViews(view);

        // Lấy thông tin người dùng từ SharedPreferences hoặc UserModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        observeUser();
        userViewModel.fetchUser();




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

    private void observeUser(){
        userViewModel.user().observe(getViewLifecycleOwner(),u->{
            if(u!=null){
                userNameTextView .setText(u.getUsername());
                userEmailTextView.setText(u.getEmail());
                userPhoneTextView.setText(u.getPhone() != null ? u.getPhone() : "Chưa có");

                String imageUrl = u.getImage_url();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load("http://192.168.200.9:3000" + imageUrl)
                            .placeholder(R.drawable.ic_memorix_logo)
                            .error(R.drawable.ic_memorix_logo)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(userAvatarImageView);
                }

                // cache xuống SharedPreferences
                SharedPreferences.Editor ed = requireContext()
                        .getSharedPreferences("UserPrefs",Context.MODE_PRIVATE).edit();
                ed.putString("user_name",u.getUsername());
                ed.putString("user_email",u.getEmail());
                ed.putString("user_phone", u.getPhone());
                ed.apply();
            }
        });
    }

    private void initViews(View view) {
        userNameTextView = view.findViewById(R.id.user_name);
        userEmailTextView = view.findViewById(R.id.user_email);
        userPhoneTextView = view.findViewById(R.id.user_phone);
        learningMethodTextView = view.findViewById(R.id.text_current_language); // Sử dụng lại ID cũ
        editProfileButton = view.findViewById(R.id.btn_edit_profile);
        logoutButton = view.findViewById(R.id.btn_logout);
        changePasswordLayout = view.findViewById(R.id.layout_change_password);
        learningMethodLayout = view.findViewById(R.id.layout_language); // Sử dụng lại ID cũ
        userAvatarImageView = view.findViewById(R.id.profile_image);

        // Cập nhật text hiển thị cho phương pháp học tập
        learningMethodTextView.setText("Spaced Repetition");
    }

    private void loadUserData() {
        // Đây là nơi bạn sẽ lấy dữ liệu từ SharedPreferences, Database hoặc API
        // Ví dụ:
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.fetchUser(); // gọi API lấy thông tin user

        userViewModel.user().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userNameTextView.setText(user.getUsername());
                userEmailTextView.setText(user.getEmail());

            }
        });
    }

    private void setupClickListeners() {
        // Xử lý sự kiện khi click vào nút Chỉnh sửa hồ sơ
        editProfileButton.setOnClickListener(v -> {
            // Mở activity chỉnh sửa hồ sơ
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                editProfileLauncher.launch(new Intent(getActivity(), EditProfileActivity.class));
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

        // Xử lý sự kiện khi click vào mục Phương pháp học tập (trước đây là Ngôn ngữ)
        learningMethodLayout.setOnClickListener(v -> openSpacedRepetitionInfo());

        // Xử lý sự kiện khi click vào nút Đăng xuất
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    // Phương thức mới để mở màn hình giới thiệu Spaced Repetition
    private void openSpacedRepetitionInfo() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), SpacedRepetitionInfoActivity.class);
            startActivity(intent);
        }
    }

    // Xóa phương thức showLanguageDialog() cũ vì không còn cần thiết

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

        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);
        String refreshToken = prefs.getString("refresh_token", null);


        if (accessToken == null || refreshToken == null) {
            // Nếu thiếu token thì logout luôn
            clearTokenAndNavigateToLogin();
            return;
        }

        AuthApi apiService = ApiClient.getClient().create(AuthApi.class);
        Call<Void> call = apiService.logout("Bearer " + accessToken ,new LogoutRequest(refreshToken));

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Server xác nhận logout -> xóa token và quay lại login
                if (response.isSuccessful()) {
                    clearTokenAndNavigateToLogin();
                } else {
                    LogoutHelper.safeLogout(requireContext());
                    //Toast.makeText(getContext(), "Logout thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối khi logout", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearTokenAndNavigateToLogin() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("access_token");
        editor.remove("refresh_token");
        editor.remove("isLoggedIn");
        editor.apply();

        // Xóa user info (nếu có)
        SharedPreferences userPrefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        // Kiểm tra nếu trước đó user đã tick "nhớ mật khẩu"
        SharedPreferences loginPrefs = getContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        boolean remember = loginPrefs.getBoolean("remember_password", false);

        if (remember) {
            // Nếu đã tick nhớ → hỏi người dùng
            new AlertDialog.Builder(getContext())
                    .setTitle("Xóa mật khẩu đã lưu?")
                    .setMessage("Bạn đã lưu mật khẩu trước đó. Bạn có muốn xóa mật khẩu này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Xóa toàn bộ loginPrefs
                        loginPrefs.edit().clear().apply();
                        proceedToLogin();
                    })
                    .setNegativeButton("Giữ lại", (dialog, which) -> {
                        // Không xóa loginPrefs
                        proceedToLogin();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            proceedToLogin();
        }
    }
    private void proceedToLogin() {
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
    @Override
    public void onResume(){
        super.onResume();
        userViewModel.fetchUser();
        userViewModel.user().removeObservers(getViewLifecycleOwner());
        userViewModel.user().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                userNameTextView.setText(u.getUsername());
                userEmailTextView.setText(u.getEmail());
                userPhoneTextView.setText(u.getPhone() != null ? u.getPhone() : "Chưa có");

                String imageUrl = u.getImage_url();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load("http://192.168.86.138:3000" + imageUrl)
                            .placeholder(R.drawable.ic_memorix_logo)
                            .error(R.drawable.ic_memorix_logo)
                            .skipMemoryCache(true) // không dùng cache
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .circleCrop()
                            .into(userAvatarImageView);
                }
            }
        });
    }
}