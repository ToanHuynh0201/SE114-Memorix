package com.example.memorix.view.deck.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Deck> newDeckList) {
        this.deckList = newDeckList;
        notifyDataSetChanged();
    }

    public class DeckViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDeckName;
        private TextView tvDeckDescription;
        private CardView btnClone;
        private View itemView;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            tvDeckName = itemView.findViewById(R.id.tv_deck_name);
            tvDeckDescription = itemView.findViewById(R.id.tv_deck_description);
            btnClone = itemView.findViewById(R.id.btn_clone);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Deck deck, int position) {
            // Set deck name
            tvDeckName.setText(deck.getName());

            // Set description
            String description = deck.getDescription();
            if (description == null || description.trim().isEmpty()) {
                tvDeckDescription.setText("Một bộ flashcard thú vị đang chờ bạn khám phá! 🚀");
            } else {
                tvDeckDescription.setText(description);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeckClick(deck, position);
                }
            });

            btnClone.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCloneDeck(deck, position);
                }
            });
        }
    }
}