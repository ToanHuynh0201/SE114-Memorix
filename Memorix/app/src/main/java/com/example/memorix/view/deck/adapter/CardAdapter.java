package com.example.memorix.view.deck.adapter;

import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.model.Card;
import com.example.memorix.R;
import com.google.gson.JsonObject;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private static final String TAG = "CardAdapter";

    private final List<Card> cards;
    private final OnCardActionListener listener;

    public interface OnCardActionListener {
        void onCardAction(Card card, String action);
    }

    public CardAdapter(List<Card> cards, OnCardActionListener listener) {
        this.cards = cards;
        this.listener = listener;

        // Enable stable IDs để tránh position conflicts
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (position < 0 || position >= cards.size()) {
            Log.w(TAG, "Invalid position: " + position + ", list size: " + cards.size());
            return;
        }

        Card card = cards.get(position);

        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return cards != null ? cards.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        // Use card ID as stable ID để tránh position conflicts
        if (position >= 0 && position < cards.size()) {
            return cards.get(position).getFlashcardId();
        }
        return RecyclerView.NO_ID;
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCardType, tvCardContent;
        private final ImageButton btnEditCard, btnDeleteCard;
        private Card currentCard; // Store card reference instead of relying on position

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardType = itemView.findViewById(R.id.tvCardType);
            tvCardContent = itemView.findViewById(R.id.tvCardContent);
            btnEditCard = itemView.findViewById(R.id.btnEditCard);
            btnDeleteCard = itemView.findViewById(R.id.btnDeleteCard);
        }

        public void bind(Card card) {
            // Store card reference
            this.currentCard = card;

            // Hiển thị loại thẻ
            String typeText = "";
            int typeColor = R.color.accent_color;

            switch (card.getCardType()) {
                case BASIC:
                    typeText = "2 Mặt";
                    typeColor = R.color.primary_color;
                    break;
                case MULTIPLE_CHOICE:
                    typeText = "Trắc nghiệm";
                    typeColor = R.color.secondary_color;
                    break;
                case FILL_IN_BLANK:
                    typeText = "Điền từ";
                    typeColor = R.color.accent_color;
                    break;
            }

            tvCardType.setText(typeText);
            tvCardType.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.getContext(), typeColor)));

            // Hiển thị nội dung thẻ (rút gọn)
            String content = getCardPreviewContent(card);
            tvCardContent.setText(content);

            // Setup listeners với safe position checking
            btnEditCard.setOnClickListener(v -> {
                if (listener != null && currentCard != null) {
                    Log.d(TAG, "Edit clicked for card ID: " + currentCard.getFlashcardId());
                    listener.onCardAction(currentCard, "edit");
                }
            });

            btnDeleteCard.setOnClickListener(v -> {
                if (listener != null && currentCard != null) {
                    Log.d(TAG, "Delete clicked for card ID: " + currentCard.getFlashcardId());
                    listener.onCardAction(currentCard, "delete");
                }
            });

            // Click vào item để xem chi tiết
            itemView.setOnClickListener(v -> {
                if (listener != null && currentCard != null) {
                    Log.d(TAG, "View clicked for card ID: " + currentCard.getFlashcardId());
                    listener.onCardAction(currentCard, "view");
                }
            });
        }

        private String getCardPreviewContent(Card card) {
            // Get content from JsonObject for better accuracy
            JsonObject content = card.getContent();
            if (content == null) {
                return card.getDisplayContent(); // Fallback to display content
            }

            switch (card.getCardType()) {
                case BASIC:
                    // For basic cards: {"front": "question", "back": "answer"}
                    String front = content.has("front") ? content.get("front").getAsString() : "";
                    String back = content.has("back") ? content.get("back").getAsString() : "";

                    // Truncate if too long
                    if (front.length() > 50) {
                        front = front.substring(0, 47) + "...";
                    }
                    return "Mặt trước: " + front;

                case MULTIPLE_CHOICE:
                    // For multiple choice cards: {"question": "...", "options": [...], "answer": "..."}
                    String question = content.has("question") ? content.get("question").getAsString() : "";

                    if (question.length() > 50) {
                        question = question.substring(0, 47) + "...";
                    }
                    return "Câu hỏi: " + question;

                case FILL_IN_BLANK:
                    // For fill in blank cards: {"text": "question", "answer": "correct_answer"}
                    String text = content.has("text") ? content.get("text").getAsString() : "";

                    if (text.length() > 50) {
                        text = text.substring(0, 47) + "...";
                    }
                    return "Điền từ: " + text;

                default:
                    String displayContent = card.getDisplayContent();
                    if (displayContent != null && displayContent.length() > 50) {
                        displayContent = displayContent.substring(0, 47) + "...";
                    }
                    return displayContent != null ? displayContent : "Không có nội dung";
            }
        }
    }
}