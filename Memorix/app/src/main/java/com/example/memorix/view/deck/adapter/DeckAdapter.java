package com.example.memorix.view.deck.adapter;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.model.Deck;

import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckViewHolder>{
    private final List<Deck> deckList;
    private final DeckActionListener listener;

    public DeckAdapter(List<Deck> deckList, DeckActionListener listener) {
        this.deckList = deckList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view, listener); // Truyền listener trực tiếp
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        if (position < 0 || position >= deckList.size()) {
            Log.w(TAG, "Invalid position: " + position + ", list size: " + deckList.size());
            return;
        }

        Deck deck = deckList.get(position);
        Log.d(TAG, "Binding position " + position +
                ", deck: " + deck.getName() +
                ", imageUrl: " + deck.getImageUrl());

        holder.bind(deck, position);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION &&
                    currentPosition < deckList.size() &&
                    listener != null) {

                Deck clickedDeck = deckList.get(currentPosition);
                Log.d(TAG, "Deck clicked at position: " + currentPosition + ", deck: " + clickedDeck.getName());
                listener.onDeckClick(clickedDeck, currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }
}
