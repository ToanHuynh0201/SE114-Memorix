package com.example.memorix.ui.deck.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.data.Deck;

import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckViewHolder>{
    private List<Deck> deckList;
    private DeckActionListener listener;

    public DeckAdapter(List<Deck> deckList, DeckActionListener listener) {
        this.deckList = deckList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = deckList.get(position);
        holder.bind(deck, position);
        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onDeckClick(deck, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }
}
