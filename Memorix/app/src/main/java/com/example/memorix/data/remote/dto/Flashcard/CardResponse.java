package com.example.memorix.data.remote.dto.Flashcard;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class CardResponse {
    @SerializedName("flashcard_id")
    private int flashcardId;

    @SerializedName("deck_id")
    private int deckId;

    @SerializedName("card_type")
    private String cardType;

    @SerializedName("content")
    private JsonObject content;

    // Getters
    public int getFlashcardId() { return flashcardId; }
    public int getDeckId() { return deckId; }
    public String getCardType() { return cardType; }
    public JsonObject getContent() { return content; }
}
