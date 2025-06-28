package com.example.memorix.view.deck.bottomsheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.example.memorix.R;

public class CreateDeckBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etDeckName;
    private TextInputEditText etDeckDescription;
    private AppCompatButton btnCreateDeck;
    private ImageView ivClose;
    private TextView tvSelectedColor;
    private TextView tvSelectedCategory;
    private CreateDeckListener listener;

    // Color selection views
    private View colorOption1, colorOption2, colorOption3, colorOption4, colorOption5, colorOption6;
    private View[] colorOptions;
    private int selectedColorId = 1; // Default color ID

    // Category selection views
    private LinearLayout categoryOption1, categoryOption2, categoryOption3, categoryOption4, categoryOption5, categoryOption6;
    private LinearLayout[] categoryOptions;
    private int selectedCategoryId = 1; // Default category ID

    // Color names for display
    private final String[] colorNames = {
            "Hồng - Xanh lá", "Xanh lá nhạt", "Vàng - Cam",
            "Tím", "Hồng - Vàng", "Xanh lá đậm"
    };

    // Category names for display
    private final String[] categoryNames = {
            "Ngôn ngữ", "Khoa học", "Lịch sử",
            "Toán học", "Nghệ thuật", "Khác"
    };

    public interface CreateDeckListener {
        void onDeckCreated(String deckName, String deckDescription, int colorId, int categoryId);
    }

    public static CreateDeckBottomSheet newInstance() {
        return new CreateDeckBottomSheet();
    }

    public void setCreateDeckListener(CreateDeckListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_deck_create_deck, container, false);

        initViews(view);
        setupCategorySelection();
        setupColorSelection();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etDeckName = view.findViewById(R.id.et_deck_name);
        etDeckDescription = view.findViewById(R.id.et_deck_description);
        btnCreateDeck = view.findViewById(R.id.btn_create_deck);
        ivClose = view.findViewById(R.id.iv_close);
        tvSelectedColor = view.findViewById(R.id.tv_selected_color);
        tvSelectedCategory = view.findViewById(R.id.tv_selected_category);

        // Initialize color option views
        colorOption1 = view.findViewById(R.id.color_option_1);
        colorOption2 = view.findViewById(R.id.color_option_2);
        colorOption3 = view.findViewById(R.id.color_option_3);
        colorOption4 = view.findViewById(R.id.color_option_4);
        colorOption5 = view.findViewById(R.id.color_option_5);
        colorOption6 = view.findViewById(R.id.color_option_6);

        colorOptions = new View[]{colorOption1, colorOption2, colorOption3, colorOption4, colorOption5, colorOption6};

        // Initialize category option views
        categoryOption1 = view.findViewById(R.id.category_option_1);
        categoryOption2 = view.findViewById(R.id.category_option_2);
        categoryOption3 = view.findViewById(R.id.category_option_3);
        categoryOption4 = view.findViewById(R.id.category_option_4);
        categoryOption5 = view.findViewById(R.id.category_option_5);
        categoryOption6 = view.findViewById(R.id.category_option_6);

        categoryOptions = new LinearLayout[]{categoryOption1, categoryOption2, categoryOption3, categoryOption4, categoryOption5, categoryOption6};
    }

    private void setupCategorySelection() {
        // Set initial selection
        updateSelectedCategory(1);

        // Set click listeners for category options
        for (int i = 0; i < categoryOptions.length; i++) {
            final int categoryId = i + 1; // Category IDs are 1-6
            categoryOptions[i].setOnClickListener(v -> updateSelectedCategory(categoryId));
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedCategory(int categoryId) {
        selectedCategoryId = categoryId;

        // Reset all category options
        for (LinearLayout categoryOption : categoryOptions) {
            categoryOption.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_option));
            categoryOption.setScaleX(1.0f);
            categoryOption.setScaleY(1.0f);
        }

        // Highlight selected category
        LinearLayout selectedView = categoryOptions[categoryId - 1];
        selectedView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_option_selected));
        selectedView.setScaleX(1.05f);
        selectedView.setScaleY(1.05f);

        // Update text
        tvSelectedCategory.setText("Đã chọn: " + categoryNames[categoryId - 1]);
    }

    private void setupColorSelection() {
        // Set initial selection
        updateSelectedColor(1);

        // Set click listeners for color options
        for (int i = 0; i < colorOptions.length; i++) {
            final int colorId = i + 1; // Color IDs are 1-6
            colorOptions[i].setOnClickListener(v -> updateSelectedColor(colorId));
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedColor(int colorId) {
        selectedColorId = colorId;

        // Reset all color options
        for (View colorOption : colorOptions) {
            colorOption.setScaleX(1.0f);
            colorOption.setScaleY(1.0f);
            colorOption.setElevation(0f);
        }

        // Highlight selected color
        View selectedView = colorOptions[colorId - 1];
        selectedView.setScaleX(1.2f);
        selectedView.setScaleY(1.2f);
        selectedView.setElevation(8f);

        // Update text
        tvSelectedColor.setText("Đã chọn: " + colorNames[colorId - 1]);
    }

    private void setupClickListeners() {
        ivClose.setOnClickListener(v -> dismiss());

        btnCreateDeck.setOnClickListener(v -> {
            String deckName = etDeckName.getText().toString().trim();
            String deckDescription = etDeckDescription.getText().toString().trim();

            if (!deckName.isEmpty()) {
                if (listener != null) {
                    listener.onDeckCreated(deckName, deckDescription, selectedColorId, selectedCategoryId);
                }
                dismiss();
            } else {
                etDeckName.setError("Vui lòng nhập tên bộ thẻ");
            }
        });
    }
}