package com.example.memorix.data.remote.dto.Flashcard;

import com.example.memorix.model.CardType;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class UpdateFlashcardRequest {
    @SerializedName("card_type")
    private String cardType;

    @SerializedName("content")
    private JsonObject content;

    public UpdateFlashcardRequest(String cardType, JsonObject content) {
        this.cardType = cardType;
        this.content = content;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public JsonObject getContent() {
        return content;
    }

    public void setContent(JsonObject content) {
        this.content = content;
    }
}
