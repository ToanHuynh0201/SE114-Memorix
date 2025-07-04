package com.example.memorix.view.deck.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.model.Deck;

public class DeckViewHolder extends RecyclerView.ViewHolder{
    private static final String TAG = "DeckViewHolder";
    private Deck currentDeck;
    private final TextView tvDeckName;
    private final TextView tvDeckDescription;
    private final TextView tvDeckCategory;
    private final TextView tvTotalCards;
    private final TextView tvLearnedCards;
    private final TextView tvDueCards;
    private final TextView tvUnlearnedCards;
    private final ProgressBar progressBar;
    private final ImageButton btnOverflowMenu;
    private final ImageView ivDueIndicator;
    private final Context context;
    private int currentPosition;
    private final DeckActionListener listener;

    // 6 bộ màu gradient có sẵn (ID từ 1-6)
    private static final int[][] GRADIENT_COLORS = {
            {Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4")}, // ID 1: Hồng -> Xanh lá
            {Color.parseColor("#A8E6CF"), Color.parseColor("#88D8C0")}, // ID 2: Xanh lá nhạt -> Xanh đậm
            {Color.parseColor("#FFD93D"), Color.parseColor("#FF8C42")}, // ID 3: Vàng -> Cam
            {Color.parseColor("#6C5CE7"), Color.parseColor("#A29BFE")}, // ID 4: Tím đậm -> Tím nhạt
            {Color.parseColor("#FD79A8"), Color.parseColor("#FDCB6E")}, // ID 5: Hồng -> Vàng
            {Color.parseColor("#00B894"), Color.parseColor("#55EFC4")}  // ID 6: Xanh lá đậm -> Xanh lá nhạt
    };

    public DeckViewHolder(@NonNull View itemView, DeckActionListener listener) {
        super(itemView);
        context = itemView.getContext();
        this.listener = listener;

        // Initialize views
        tvDeckName = itemView.findViewById(R.id.tv_deck_name);
        tvDeckDescription = itemView.findViewById(R.id.tv_deck_description);
        tvDeckCategory = itemView.findViewById(R.id.tv_deck_category);
        tvTotalCards = itemView.findViewById(R.id.tv_total_cards);
        tvLearnedCards = itemView.findViewById(R.id.tv_learned_cards);
        tvDueCards = itemView.findViewById(R.id.tv_due_cards);
        tvUnlearnedCards = itemView.findViewById(R.id.tv_unlearned_cards);
        progressBar = itemView.findViewById(R.id.progress_bar);
        btnOverflowMenu = itemView.findViewById(R.id.btn_overflow_menu);
        ivDueIndicator = itemView.findViewById(R.id.iv_due_indicator);

        btnOverflowMenu.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customMenuView = inflater.inflate(R.layout.menu_deck_options, null);

        final PopupWindow popupWindow = new PopupWindow(
                customMenuView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(10f);

        TextView itemEdit = customMenuView.findViewById(R.id.menu_item_edit);
        TextView itemShare = customMenuView.findViewById(R.id.menu_item_share);
//        TextView itemResetProgress = customMenuView.findViewById(R.id.menu_item_reset_progress);
        TextView itemDelete = customMenuView.findViewById(R.id.menu_item_delete);

        itemEdit.setOnClickListener(v -> {
            if (listener != null && currentDeck != null) {
                Log.d(TAG, "Edit clicked for deck ID: " + currentDeck.getId());
                listener.onEditDeck(currentDeck.getId());
            }
            popupWindow.dismiss();
        });

        itemShare.setOnClickListener(v -> {
            if (listener != null && currentDeck != null) {
                Log.d(TAG, "Share clicked for deck ID: " + currentDeck.getId());
                listener.onShareDeck(currentDeck.getId());
            }
            popupWindow.dismiss();
        });

//        itemResetProgress.setOnClickListener(v -> {
//            if (listener != null && currentDeck != null) {
//                Log.d(TAG, "Reset progress clicked for deck ID: " + currentDeck.getId());
//                listener.onResetProgress(currentDeck.getId());
//            }
//            popupWindow.dismiss();
//        });

        itemDelete.setOnClickListener(v -> {
            if (listener != null && currentDeck != null) {
                Log.d(TAG, "Delete clicked for deck ID: " + currentDeck.getId());
                listener.onDeleteDeck(currentDeck.getId());
            }
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(btnOverflowMenu, -150, 0);
    }

    private int extractColorIdFromImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.d(TAG, "ImageUrl is null/empty, using default color 1");
            return 1;
        }

        // Check if it's a direct color ID (1-6)
        try {
            int id = Integer.parseInt(imageUrl.trim());
            if (id >= 1 && id <= 6) {
                Log.d(TAG, "Found direct color ID: " + id);
                return id;
            }
        } catch (NumberFormatException e) {
            // It's a URL, try to extract color ID from it
            Log.d(TAG, "Not a direct ID, trying to extract from URL");
        }

        // Extract color ID from placeholder URL patterns
        String url = imageUrl.toLowerCase();
        if (url.contains("color1") || url.contains("ff6b6b")) {
            Log.d(TAG, "Detected color 1 from URL");
            return 1;
        }
        if (url.contains("color2") || url.contains("a8e6cf")) {
            Log.d(TAG, "Detected color 2 from URL");
            return 2;
        }
        if (url.contains("color3") || url.contains("ffd93d")) {
            Log.d(TAG, "Detected color 3 from URL");
            return 3;
        }
        if (url.contains("color4") || url.contains("6c5ce7")) {
            Log.d(TAG, "Detected color 4 from URL");
            return 4;
        }
        if (url.contains("color5") || url.contains("fd79a8")) {
            Log.d(TAG, "Detected color 5 from URL");
            return 5;
        }
        if (url.contains("color6") || url.contains("00b894")) {
            Log.d(TAG, "Detected color 6 from URL");
            return 6;
        }

        Log.d(TAG, "Could not extract color from URL, using fallback based on deck ID");
        // Use deck ID for consistent colors instead of position
        return (int) ((currentDeck.getId() % 6) + 1);
    }

    private void applyDeckColors(Deck deck) {
        // Store current position for fallback use
        this.currentPosition = getAdapterPosition();

        int colorId = extractColorIdFromImageUrl(deck.getImageUrl());


        // Ensure colorId is within valid range (1-6)
        if (colorId < 1 || colorId > 6) {
            // Use deck ID for consistent fallback instead of position
            colorId = (int) ((deck.getId() % 6) + 1);
            Log.d(TAG, "Color ID out of range, using deck ID-based fallback: " + colorId);
        }

        int colorIndex = colorId - 1; // Convert to 0-5 index
        int[] colors = GRADIENT_COLORS[colorIndex];

        Log.d(TAG, "Using color array index: " + colorIndex +
                ", colors: [" + String.format("#%06X", colors[0] & 0xFFFFFF) +
                ", " + String.format("#%06X", colors[1] & 0xFFFFFF) + "]");

        // Tạo gradient border với LayerDrawable
        createGradientBorder(colors);

        // Cập nhật progress bar với màu tương ứng
        updateProgressBarColor(colors);
    }

    private void createGradientBorder(int[] colors) {
        try {
            int borderWidth = dpToPx(3);
            int cornerRadius = dpToPx(12);

            // 1. Tạo gradient drawable cho border (lớp ngoài)
            GradientDrawable borderGradient = new GradientDrawable();
            borderGradient.setShape(GradientDrawable.RECTANGLE);
            borderGradient.setColors(colors); // Gradient colors
            borderGradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            borderGradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            borderGradient.setCornerRadius(cornerRadius);

            // 2. Tạo background trắng (lớp trong)
            GradientDrawable whiteBackground = new GradientDrawable();
            whiteBackground.setShape(GradientDrawable.RECTANGLE);
            whiteBackground.setColor(Color.WHITE);
            whiteBackground.setCornerRadius(cornerRadius);

            // 3. Tạo LayerDrawable để combine gradient border + white background
            LayerDrawable layerDrawable = new LayerDrawable(new android.graphics.drawable.Drawable[]{
                    borderGradient,  // Lớp gradient bên ngoài
                    whiteBackground  // Lớp trắng bên trong
            });

            // 4. Set inset cho lớp trong để tạo border effect
            layerDrawable.setLayerInset(1, borderWidth, borderWidth, borderWidth, borderWidth);

            // 5. Áp dụng lên itemView
            itemView.setBackground(layerDrawable);


        } catch (Exception e) {
            // Fallback to simple border if gradient fails
            GradientDrawable fallbackDrawable = new GradientDrawable();
            fallbackDrawable.setShape(GradientDrawable.RECTANGLE);
            fallbackDrawable.setColor(Color.WHITE);
            fallbackDrawable.setStroke(dpToPx(3), colors[0]);
            fallbackDrawable.setCornerRadius(dpToPx(12));
            itemView.setBackground(fallbackDrawable);
        }
    }

    private void updateProgressBarColor(int[] colors) {
        try {
            // Tạo gradient cho progress bar
            GradientDrawable progressGradient = new GradientDrawable();
            progressGradient.setColors(colors);
            progressGradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            progressGradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            progressGradient.setCornerRadius(dpToPx(8));

            // Tạo background cho progress bar
            GradientDrawable progressBackground = new GradientDrawable();
            progressBackground.setColor(Color.parseColor("#E8E8E8"));
            progressBackground.setCornerRadius(dpToPx(8));

            // Tạo layer list cho progress bar
            LayerDrawable progressLayerDrawable = new LayerDrawable(new android.graphics.drawable.Drawable[]{
                    progressBackground, progressGradient
            });

            progressLayerDrawable.setId(0, android.R.id.background);
            progressLayerDrawable.setId(1, android.R.id.progress);

            progressBar.setProgressDrawable(progressLayerDrawable);

            Log.d(TAG, "Progress bar color updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating progress bar color", e);
        }
    }
    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @SuppressLint("SetTextI18n")
    public void bind(Deck deck, int position) {
        this.currentDeck = deck; // Lưu deck reference
        this.currentPosition = position; // Lưu position for fallback

        // Set basic info
        tvDeckName.setText(deck.getName());
        tvDeckDescription.setText(deck.getDescription());

        // Set category
        String category = deck.getCategory();
        if (category != null && !category.isEmpty()) {
            tvDeckCategory.setText(category);
            tvDeckCategory.setVisibility(View.VISIBLE);
        } else {
            tvDeckCategory.setVisibility(View.GONE);
        }

        // Set card counts
        tvTotalCards.setText(deck.getTotalCards() + " thẻ");
        tvLearnedCards.setText(deck.getLearnedCards() + " đã học");
        tvDueCards.setText(deck.getDueCards() + " cần ôn");
        tvUnlearnedCards.setText(deck.getUnlearnedCards() + " chưa học");

        // Show due indicator if there are due cards
        if (deck.hasDueCards()) {
            ivDueIndicator.setVisibility(View.VISIBLE);
            ivDueIndicator.setColorFilter(ContextCompat.getColor(context, R.color.warning_color));
        } else {
            ivDueIndicator.setVisibility(View.GONE);
        }
        itemView.setOnClickListener(v -> {
            if (listener != null && currentDeck != null) {
                Log.d(TAG, "✅ ItemView clicked for deck: " + currentDeck.getName());
                listener.onDeckClick(currentDeck, getAdapterPosition());
            } else {
                Log.w(TAG, "⚠️ Click ignored: listener or currentDeck is null");
            }
        });

        // Apply colors (progress bar will keep gradient from applyDeckColors)
        applyDeckColors(deck);
    }
}