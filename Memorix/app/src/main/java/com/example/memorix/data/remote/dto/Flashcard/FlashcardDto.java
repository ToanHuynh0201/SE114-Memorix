package com.example.memorix.data.remote.dto.Flashcard;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class FlashcardDto {
    @SerializedName("flashcard_id")
    private int flashcardId;

    @SerializedName("deck_id")
    private int deckId;

    @SerializedName("card_type")
    private String cardType;

    @SerializedName("content")
    private JsonObject content;

    // Getters and setters
    public int getFlashcardId() { return flashcardId; }
    public void setFlashcardId(int flashcardId) { this.flashcardId = flashcardId; }

    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public JsonObject getContent() { return content; }
    public void setContent(JsonObject content) { this.content = content; }
}
