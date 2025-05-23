package com.example.memorix.ui.deck.bottomsheet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.memorix.R;

public class CreateDeckBottomSheet extends BottomSheetDialogFragment {

    private EditText etDeckName;
    private AppCompatButton btnCreateDeck;
    private ImageView ivClose;
    private CreateDeckListener listener;

    public interface CreateDeckListener {
        void onDeckCreated(String deckName);
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
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etDeckName = view.findViewById(R.id.et_deck_name);
        btnCreateDeck = view.findViewById(R.id.btn_create_deck);
        ivClose = view.findViewById(R.id.iv_close);
    }

    private void setupClickListeners() {
        ivClose.setOnClickListener(v -> dismiss());

        btnCreateDeck.setOnClickListener(v -> {
            String deckName = etDeckName.getText().toString().trim();
            if (!deckName.isEmpty()) {
                if (listener != null) {
                    listener.onDeckCreated(deckName);
                }
                dismiss();
            } else {
                etDeckName.setError("Vui lòng nhập tên bộ thẻ");
            }
        });
    }
}