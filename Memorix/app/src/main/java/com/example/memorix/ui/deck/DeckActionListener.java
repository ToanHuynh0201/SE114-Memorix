package com.example.memorix.ui.deck;

import com.example.memorix.data.Deck;

public interface DeckActionListener {
    void onEditDeck(int position);
    void onShareDeck(int position);
    void onResetProgress(int position);
    void onDeleteDeck(int position);
    void onDeckClick(Deck deck, int position);
}
