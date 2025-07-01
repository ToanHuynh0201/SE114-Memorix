package com.example.memorix.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Card implements Serializable {
    @SerializedName("flashcard_id")
    private int flashcardId;
    @SerializedName("deck_id")
    private int deckId;
    private CardType cardType;
    private JsonObject content;

    // Getters and setters
    public int getFlashcardId() { return flashcardId; }
    public void setFlashcardId(int flashcardId) { this.flashcardId = flashcardId; }
    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }

    public JsonObject getContent() { return content; }
    public void setContent(JsonObject content) { this.content = content; }
    public static Card createBasicCard(int deckId, String question, String answer) {
        Card card = new Card();
        card.setDeckId(deckId);
        card.setCardType(CardType.BASIC);

        // Tạo content theo format API: two_sided
        JsonObject content = new JsonObject();
        content.addProperty("front", question);
        content.addProperty("back", answer);
        card.setContent(content);

        return card;
    }

    public static Card createMultipleChoiceCard(int deckId, String question, List<String> options, String correctAnswer) {
        Card card = new Card();
        card.setDeckId(deckId);
        card.setCardType(CardType.MULTIPLE_CHOICE);

        // Tạo content theo format API: multiple_choice
        JsonObject content = new JsonObject();
        content.addProperty("question", question);

        // Thêm options array
        JsonArray optionsArray = new JsonArray();
        for (String option : options) {
            optionsArray.add(option);
        }
        content.add("options", optionsArray);
        content.addProperty("answer", correctAnswer);
        card.setContent(content);

        return card;
    }

    public static Card createFillInBlankCard(int deckId, String question, String correctAnswer) {
        Card card = new Card();
        card.setDeckId(deckId);
        card.setCardType(CardType.FILL_IN_BLANK);

        // Tạo content theo format API: fill_in_blank
        JsonObject content = new JsonObject();
        content.addProperty("text", question);
        content.addProperty("answer", correctAnswer);
        card.setContent(content);

        return card;
    }

    // Helper method để lấy thông tin display từ JsonObject content
    public String getDisplayContent() {
        if (content == null) return "";

        switch (cardType) {
            case BASIC:
                String front = content.has("front") ? content.get("front").getAsString() : "";
                String back = content.has("back") ? content.get("back").getAsString() : "";
                return "Q: " + front + "\nA: " + back;

            case MULTIPLE_CHOICE:
                StringBuilder sb = new StringBuilder();
                String question = content.has("question") ? content.get("question").getAsString() : "";
                sb.append("Q: ").append(question).append("\n");

                if (content.has("options")) {
                    com.google.gson.JsonArray options = content.getAsJsonArray("options");
                    for (int i = 0; i < options.size(); i++) {
                        sb.append((char)('A' + i)).append(": ").append(options.get(i).getAsString()).append("\n");
                    }
                }

                String answer = content.has("answer") ? content.get("answer").getAsString() : "";
                sb.append("Correct: ").append(answer);
                return sb.toString();

            case FILL_IN_BLANK:
                String text = content.has("text") ? content.get("text").getAsString() : "";
                String fillAnswer = content.has("answer") ? content.get("answer").getAsString() : "";
                return "Q: " + text + "\nA: " + fillAnswer;

            default:
                return "";
        }
    }

    @Override
    public String toString() {
        String question = "";
        if (content != null) {
            if (cardType == CardType.BASIC && content.has("front")) {
                question = content.get("front").getAsString();
            } else if (cardType == CardType.MULTIPLE_CHOICE && content.has("question")) {
                question = content.get("question").getAsString();
            } else if (cardType == CardType.FILL_IN_BLANK && content.has("text")) {
                question = content.get("text").getAsString();
            }
        }

        return "Card{" +
                "flashcardId=" + flashcardId +
                ", deckId=" + deckId +
                ", cardType=" + cardType +
                ", question='" + question + '\'' +
                '}';
    }
}