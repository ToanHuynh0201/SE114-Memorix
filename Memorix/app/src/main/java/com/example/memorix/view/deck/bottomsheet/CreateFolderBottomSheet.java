package com.example.memorix.view.deck.bottomsheet;
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
public class CreateFolderBottomSheet extends BottomSheetDialogFragment {

    private EditText etFolderName;
    private AppCompatButton btnCreateFolder;
    private ImageView ivClose;
    private CreateFolderListener listener;

    public interface CreateFolderListener {
        void onFolderCreated(String folderName);
    }

    public static CreateFolderBottomSheet newInstance() {
        return new CreateFolderBottomSheet();
    }

    public void setCreateFolderListener(CreateFolderListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_deck_create_folder, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etFolderName = view.findViewById(R.id.et_folder_name);
        btnCreateFolder = view.findViewById(R.id.btn_create_folder);
        ivClose = view.findViewById(R.id.iv_close);
    }

    private void setupClickListeners() {
        ivClose.setOnClickListener(v -> dismiss());

        btnCreateFolder.setOnClickListener(v -> {
            String folderName = etFolderName.getText().toString().trim();
            if (!folderName.isEmpty()) {
                if (listener != null) {
                    listener.onFolderCreated(folderName);
                }
                dismiss();
            } else {
                etFolderName.setError("Vui lòng nhập tên thư mục");
            }
        });
    }
}