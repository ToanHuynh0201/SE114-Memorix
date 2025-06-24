package com.example.memorix.ui.deck.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.model.Deck;

public class DeckViewHolder extends RecyclerView.ViewHolder{
    private TextView tvDeckName;
    private TextView tvDeckDescription;
    private TextView tvCardCount;
    private TextView tvMasteredCount;
    private ProgressBar progressBar;
    private ImageButton btnOverflowMenu;
    private Context context;
    private int currentPosition;
    private DeckActionListener listener;
    public DeckViewHolder(@NonNull View itemView, DeckActionListener listener) {
        super(itemView);
        context = itemView.getContext();
        this.listener = listener;
        context = itemView.getContext();
        tvDeckName = itemView.findViewById(R.id.tv_deck_name);
        tvDeckDescription = itemView.findViewById(R.id.tv_deck_description);
        tvCardCount = itemView.findViewById(R.id.tv_card_count);
        progressBar = itemView.findViewById(R.id.progress_bar);
        btnOverflowMenu = itemView.findViewById(R.id.btn_overflow_menu);

        // Thiết lập sự kiện bấm vào nút 3 chấm
        btnOverflowMenu.setOnClickListener(v -> {
            showPopupMenu();
        });
    }

    private void showPopupMenu() {
        // Inflate layout cho custom menu
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customMenuView = inflater.inflate(R.layout.menu_deck_options, null);

        // Tạo PopupWindow với shadow và góc bo tròn
        final PopupWindow popupWindow = new PopupWindow(
                customMenuView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Thiết lập animation
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        // Thiết lập background cho shadow effect
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(10f);

        // Tìm các view item trong custom layout
        TextView itemEdit = customMenuView.findViewById(R.id.menu_item_edit);
        TextView itemShare = customMenuView.findViewById(R.id.menu_item_share);
        TextView itemResetProgress = customMenuView.findViewById(R.id.menu_item_reset_progress);
        TextView itemDelete = customMenuView.findViewById(R.id.menu_item_delete);

        // Thiết lập sự kiện click cho từng item
        itemEdit.setOnClickListener(v -> {
            // Xử lý khi chọn Chỉnh sửa
            Toast.makeText(context, "Chỉnh sửa bộ thẻ", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onEditDeck(currentPosition);
            }
            popupWindow.dismiss();
        });

        itemShare.setOnClickListener(v -> {
            // Xử lý khi chọn Chia sẻ
            Toast.makeText(context, "Chia sẻ bộ thẻ", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onShareDeck(currentPosition);
            }
            popupWindow.dismiss();
        });

        itemResetProgress.setOnClickListener(v -> {
            // Xử lý khi chọn Đặt lại tiến độ
            Toast.makeText(context, "Đặt lại tiến độ", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onResetProgress(currentPosition);
            }
            popupWindow.dismiss();
        });

        itemDelete.setOnClickListener(v -> {
            // Xử lý khi chọn Xóa
            Toast.makeText(context, "Xóa bộ thẻ", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onDeleteDeck(currentPosition);
            }
            popupWindow.dismiss();
        });

        // Hiển thị popup menu
        // Để hiển thị menu bên dưới và phải của button
        popupWindow.showAsDropDown(btnOverflowMenu, -150, 0);
    }

    @SuppressLint("SetTextI18n")
    public void bind(Deck deck, int position) {
        this.currentPosition = position;
        tvDeckName.setText(deck.getName());
        tvDeckDescription.setText(deck.getDescription());
        tvCardCount.setText(deck.getCardCount() + " cards");

        // Cập nhật tiến độ
//        int progress = 0;
//        if (deck.getCardCount() > 0) {
//            progress = (int) (((float) deck.getMasteredCount() / deck.getCardCount()) * 100);
//        }
//        progressBar.setProgress(progress);
    }
}
