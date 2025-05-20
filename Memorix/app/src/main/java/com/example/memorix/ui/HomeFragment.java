package com.example.memorix.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.memorix.R;
public class HomeFragment extends Fragment {

    private RecyclerView recyclerFlashcardSets;
    private LinearLayout layoutEmptyState;
    private Button btnReviewToday;
    private Button btnCreateFirstSet;
    private TextView txtViewAll;
    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerFlashcardSets = view.findViewById(R.id.recycler_flashcard_sets);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnReviewToday = view.findViewById(R.id.btn_review_today);
        btnCreateFirstSet = view.findViewById(R.id.btn_create_first_set);
        txtViewAll = view.findViewById(R.id.txt_view_all);

        // Khởi tạo RecyclerView, adapter và các thành phần khác
        setupRecyclerView();
        checkEmptyState();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    private void setupRecyclerView() {
        // Thiết lập RecyclerView để hiển thị danh sách bộ thẻ
        // Thiết lập LayoutManager, Adapter, etc.
    }

    private void checkEmptyState() {
        // Kiểm tra và hiển thị trạng thái trống nếu không có bộ thẻ nào
        // Ví dụ:
        boolean hasData = false; // Thay bằng logic kiểm tra dữ liệu thực tế

        if (hasData) {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerFlashcardSets.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerFlashcardSets.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnReviewToday.setOnClickListener(v -> {
            // Xử lý khi nhấn nút "Ôn tập ngay"
        });

        txtViewAll.setOnClickListener(v -> {
            // Xử lý khi nhấn "Xem tất cả"
        });

        btnCreateFirstSet.setOnClickListener(v -> {
            // Xử lý khi nhấn nút tạo bộ thẻ đầu tiên
        });
    }
}