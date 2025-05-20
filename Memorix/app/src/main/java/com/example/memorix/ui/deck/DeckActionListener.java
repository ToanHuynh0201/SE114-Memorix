package com.example.memorix.ui.deck;

public interface DeckActionListener {
    void onEditDeck(int position);
    void onShareDeck(int position);
    void onResetProgress(int position);
    void onDeleteDeck(int position);
}
