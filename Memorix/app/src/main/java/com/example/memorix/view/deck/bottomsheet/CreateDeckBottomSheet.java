package com.example.memorix.view.deck.bottomsheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.memorix.R;

public class CreateDeckBottomSheet extends BottomSheetDialogFragment {

    private EditText etDeckName;
    private AppCompatButton btnCreateDeck;
    private ImageView ivClose;
    private TextView tvSelectedColor;
    private CreateDeckListener listener;

    // Color selection views
    private View colorOption1, colorOption2, colorOption3, colorOption4, colorOption5, colorOption6;
    private View[] colorOptions;
    private int selectedColorId = 1; // Default color ID

    // Color names for display
    private final String[] colorNames = {
            "Hồng - Xanh lá", "Xanh lá nhạt", "Vàng - Cam",
            "Tím", "Hồng - Vàng", "Xanh lá đậm"
    };

    public interface CreateDeckListener {
        void onDeckCreated(String deckName, int colorId);
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
        setupColorSelection();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etDeckName = view.findViewById(R.id.et_deck_name);
        btnCreateDeck = view.findViewById(R.id.btn_create_deck);
        ivClose = view.findViewById(R.id.iv_close);
        tvSelectedColor = view.findViewById(R.id.tv_selected_color);

        // Initialize color option views
        colorOption1 = view.findViewById(R.id.color_option_1);
        colorOption2 = view.findViewById(R.id.color_option_2);
        colorOption3 = view.findViewById(R.id.color_option_3);
        colorOption4 = view.findViewById(R.id.color_option_4);
        colorOption5 = view.findViewById(R.id.color_option_5);
        colorOption6 = view.findViewById(R.id.color_option_6);

        colorOptions = new View[]{colorOption1, colorOption2, colorOption3, colorOption4, colorOption5, colorOption6};
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
            if (!deckName.isEmpty()) {
                if (listener != null) {
                    listener.onDeckCreated(deckName, selectedColorId);
                }
                dismiss();
            } else {
                etDeckName.setError("Vui lòng nhập tên bộ thẻ");
            }
        });
    }
}