package com.example.memorix.ui.deck;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.data.Card;
import com.example.memorix.data.Flashcard;
import com.example.memorix.R;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> cards;
    private OnCardActionListener listener;

    public interface OnCardActionListener {
        void onCardAction(Card card, String action);
    }

    public CardAdapter(List<Card> cards, OnCardActionListener listener) {
        this.cards = cards;
        this.listener = listener;
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
        Card card = cards.get(position);
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCardType, tvCardContent;
        private ImageButton btnEditCard, btnDeleteCard;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardType = itemView.findViewById(R.id.tvCardType);
            tvCardContent = itemView.findViewById(R.id.tvCardContent);
            btnEditCard = itemView.findViewById(R.id.btnEditCard);
            btnDeleteCard = itemView.findViewById(R.id.btnDeleteCard);
        }

        public void bind(Card card) {
            // Hiển thị loại thẻ
            String typeText = "";
            int typeColor = R.color.accent_color;

            switch (card.getType()) {
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

            // Setup listeners
            btnEditCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardAction(card, "edit");
                }
            });

            btnDeleteCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardAction(card, "delete");
                }
            });

            // Click vào item để xem chi tiết
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardAction(card, "view");
                }
            });
        }

        private String getCardPreviewContent(Card card) {
            switch (card.getType()) {
                case BASIC:
                    return "Câu hỏi: " + card.getQuestion();

                case MULTIPLE_CHOICE:
                    return "Câu hỏi: " + card.getQuestion() +
                            "\nA. " + card.getOptions().get(0);

                case FILL_IN_BLANK:
                    return "Điền từ: " + card.getQuestion().replace("___", "[___]");

                default:
                    return card.getQuestion();
            }
        }
    }
}