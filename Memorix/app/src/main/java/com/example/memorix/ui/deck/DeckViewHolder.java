package com.example.memorix.ui.deck;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.data.Deck;

public class DeckViewHolder extends RecyclerView.ViewHolder{
    private TextView tvDeckName;
    private TextView tvDeckDescription;
    private TextView tvCardCount;
    private TextView tvMasteredCount;
    private ProgressBar progressBar;
    private ImageButton btnOverflowMenu;
    private Context context;
    public DeckViewHolder(@NonNull View itemView, DeckActionListener listener) {
        super(itemView);
        context = itemView.getContext();
        tvDeckName = itemView.findViewById(R.id.tv_deck_name);
        tvDeckDescription = itemView.findViewById(R.id.tv_deck_description);
        tvCardCount = itemView.findViewById(R.id.tv_card_count);
        tvMasteredCount = itemView.findViewById(R.id.tv_mastered_count);
        progressBar = itemView.findViewById(R.id.progress_bar);
        btnOverflowMenu = itemView.findViewById(R.id.btn_overflow_menu);

        // Thiết lập sự kiện bấm vào nút 3 chấm
        btnOverflowMenu.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(context, btnOverflowMenu);
        popupMenu.inflate(R.menu.menu_deck_options);

        // Thiết lập sự kiện khi chọn một mục trong menu
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                // Xử lý khi chọn Chỉnh sửa
                Toast.makeText(context, "Chỉnh sửa bộ thẻ", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_share) {
                // Xử lý khi chọn Chia sẻ
                Toast.makeText(context, "Chia sẻ bộ thẻ", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_reset_progress) {
                // Xử lý khi chọn Đặt lại tiến độ
                Toast.makeText(context, "Đặt lại tiến độ", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_delete) {
                // Xử lý khi chọn Xóa
                Toast.makeText(context, "Xóa bộ thẻ", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });

        // Hiển thị menu
        popupMenu.show();
    }

    public void bind(Deck deck, int position) {
        tvDeckName.setText(deck.getName());
        tvDeckDescription.setText(deck.getDescription());
        tvCardCount.setText(deck.getCardCount() + " cards");
        tvMasteredCount.setText(deck.getMasteredCount() + " mastered");

        // Cập nhật tiến độ
        int progress = 0;
        if (deck.getCardCount() > 0) {
            progress = (int) (((float) deck.getMasteredCount() / deck.getCardCount()) * 100);
        }
        progressBar.setProgress(progress);
    }
}
