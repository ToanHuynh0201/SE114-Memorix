package com.example.memorix.view.deck.adapter;

import com.example.memorix.model.Deck;

public interface DeckActionListener {
    void onDeckClick(Deck deck, int position);
    void onEditDeck(long deckId);     // Dùng ID thay vì position
    void onShareDeck(long deckId);    // Dùng ID thay vì position
    void onResetProgress(long deckId); // Dùng ID thay vì position
    void onDeleteDeck(long deckId);   // Dùng ID thay vì position
}
