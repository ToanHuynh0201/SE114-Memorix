package com.example.memorix.data.remote.dto.Flashcard;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class CreateFlashcardRequest {
    @SerializedName("deck_id")
    private int deckId;

    @SerializedName("card_type")
    private String cardType;

    @SerializedName("content")
    private JsonObject content;

    public CreateFlashcardRequest(int deckId, String cardType, JsonObject content) {
        this.deckId = deckId;
        this.cardType = cardType;
        this.content = content;
    }

    // Getters
    public int getDeckId() { return deckId; }
    public String getCardType() { return cardType; }
    public JsonObject getContent() { return content; }
}
