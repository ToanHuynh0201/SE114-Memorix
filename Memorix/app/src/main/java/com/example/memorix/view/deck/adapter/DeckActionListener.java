package com.example.memorix.view.deck.adapter;

import com.example.memorix.model.Deck;

public interface DeckActionListener {
    void onEditDeck(int position);
    void onShareDeck(int position);
    void onResetProgress(int position);
    void onDeleteDeck(int position);
    void onDeckClick(Deck deck, int position);
}
