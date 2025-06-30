package com.example.memorix.view.deck.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memorix.R;
import com.example.memorix.model.Deck;
import java.util.List;

public class DeckLibraryAdapter extends RecyclerView.Adapter<DeckLibraryAdapter.DeckViewHolder> {
    private List<Deck> deckList;
    private DeckLibraryListener listener;

    public interface DeckLibraryListener {
        void onDeckClick(Deck deck, int position);
        void onCloneDeck(Deck deck, int position);
    }

    public DeckLibraryAdapter(List<Deck> deckList, DeckLibraryListener listener) {
        this.deckList = deckList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deck_library, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = deckList.get(position);
        holder.bind(deck, position);
    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }


    public class DeckViewHolder extends RecyclerView.ViewHolder {
        // Main Views
        private TextView tvDeckName;
        private TextView tvDeckDescription;
        private TextView tvCardCount;
        private TextView tvCategory;
        private TextView tvAuthor;

        // Card containers
        private CardView cardCategory;
        private CardView cardTotalBadge;
        private CardView btnClone;


        private View itemView;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            // Initialize main views
            tvDeckName = itemView.findViewById(R.id.tv_deck_name);
            tvDeckDescription = itemView.findViewById(R.id.tv_deck_description);
            tvCardCount = itemView.findViewById(R.id.tv_card_count);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAuthor = itemView.findViewById(R.id.tv_author);

            // Initialize card containers
            cardCategory = itemView.findViewById(R.id.card_category);
            cardTotalBadge = itemView.findViewById(R.id.card_total_badge);
            btnClone = itemView.findViewById(R.id.btn_clone);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Deck deck, int position) {
            // Set deck name
            tvDeckName.setText(deck.getName());
            Log.e("ERROR", deck.getTotalCards() + " " + deck.getCategory());
            // Set total cards count in badge
            String cardCountText = deck.getTotalCards() + " thẻ";
            tvCardCount.setText(cardCountText);

            // Set category
            setupCategory(deck);

            // Set description
            setupDescription(deck);

            // Set author info (if available from PublicDeck)
            setupAuthorInfo(deck);


            // Set click listeners
            setupClickListeners(deck, position);
        }

        private void setupCategory(Deck deck) {
            if (deck.getCategory() != null && !deck.getCategory().trim().isEmpty()) {
                tvCategory.setText(deck.getCategory());
                cardCategory.setVisibility(View.VISIBLE);
            } else {
                cardCategory.setVisibility(View.GONE);
            }
        }

        private void setupDescription(Deck deck) {
            String description = deck.getDescription();
            if (description == null || description.trim().isEmpty()) {
                tvDeckDescription.setText("Một bộ flashcard thú vị đang chờ bạn khám phá! 🚀");
            } else {
                tvDeckDescription.setText(description);
            }
        }

        private void setupAuthorInfo(Deck deck) {
            // Default author text
            tvAuthor.setText("Tác giả không xác định");

        }


        private void setupClickListeners(Deck deck, int position) {
            // Main item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeckClick(deck, position);
                }
            });

            // Clone button click
            btnClone.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCloneDeck(deck, position);
                }
            });
        }
    }

    // Utility methods for adapter
    public boolean isEmpty() {
        return deckList == null || deckList.isEmpty();
    }


    public Deck getDeckAt(int position) {
        if (position >= 0 && position < deckList.size()) {
            return deckList.get(position);
        }
        return null;
    }
}