package com.example.memorix.data.remote.dto.Flashcard;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class CreateFlashcardResponse {
    @SerializedName("flashcard_id")
    private int flashcardId;
    @SerializedName("deck_id")
    private int deckId;
    @SerializedName("card_type")
    private String cardType;
    private Object content;
    public CreateFlashcardResponse() {}

    // Getters and Setters
    public int getFlashcardId() { return flashcardId; }
    public void setFlashcardId(int flashcardId) { this.flashcardId = flashcardId; }
    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public Object getContent() { return content; }
    public void setContent(Object content) { this.content = content; }

    // Helper methods to get typed content
    public TwoSidedContent getTwoSidedContent() {
        if ("two_sided".equals(cardType) && content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            return new TwoSidedContent(
                    (String) map.get("front"),
                    (String) map.get("back")
            );
        }
        return null;
    }

    public MultipleChoiceContent getMultipleChoiceContent() {
        if ("multiple_choice".equals(cardType) && content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            return new MultipleChoiceContent(
                    (String) map.get("question"),
                    (List<String>) map.get("options"),
                    (String) map.get("answer")
            );
        }
        return null;
    }

    public FillInBlankContent getFillInBlankContent() {
        if ("fill_in_blank".equals(cardType) && content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            return new FillInBlankContent(
                    (String) map.get("text"),
                    (String) map.get("answer")
            );
        }
        return null;
    }
}
