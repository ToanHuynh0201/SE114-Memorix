package com.example.memorix.view.deck.bottomsheet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.example.memorix.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddOptionsBottomSheet extends BottomSheetDialogFragment {
    public interface OptionClickListener {
        void onCreateDeckClicked();
        void onGoToLibrary();
    }

    private OptionClickListener listener;

    public static AddOptionsBottomSheet newInstance() {
        return new AddOptionsBottomSheet();
    }

    public void setOptionClickListener(OptionClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_deck_add_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout createDeckLayout = view.findViewById(R.id.layout_create_deck);
        LinearLayout goToLibrary = view.findViewById(R.id.layout_go_to_library);

        createDeckLayout.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCreateDeckClicked();
            }
            dismiss();
        });

        goToLibrary.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoToLibrary();
            }
            dismiss();
        });
    }
}
