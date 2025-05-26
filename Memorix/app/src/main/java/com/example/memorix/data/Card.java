package com.example.memorix.data;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
public class Card implements Serializable {
    private String id;
    private String deckId;
    private CardType type;
    private String question;
    private String answer;
    private List<String> options; // Cho multiple choice
    private String correctAnswer; // Cho fill in blank và multiple choice
    private long createdAt;
    private long updatedAt;
    private int reviewCount;
    private int correctCount;

    // Constructor mặc định
    public Card() {
        this.options = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.reviewCount = 0;
        this.correctCount = 0;
    }

    // Constructor cho thẻ 2 mặt
    public Card(String id, String deckId, String question, String answer) {
        this();
        this.id = id;
        this.deckId = deckId;
        this.type = CardType.BASIC;
        this.question = question;
        this.answer = answer;
    }

    // Constructor cho multiple choice
    public Card(String id, String deckId, String question, List<String> options, String correctAnswer) {
        this();
        this.id = id;
        this.deckId = deckId;
        this.type = CardType.MULTIPLE_CHOICE;
        this.question = question;
        this.options = new ArrayList<>(options);
        this.correctAnswer = correctAnswer;
    }

    // Constructor cho fill in blank
    public Card(String id, String deckId, String question, String correctAnswer, CardType type) {
        this();
        this.id = id;
        this.deckId = deckId;
        this.type = type;
        this.question = question;
        this.correctAnswer = correctAnswer;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDeckId() {
        return deckId;
    }

    public CardType getType() {
        return type;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public void setQuestion(String question) {
        this.question = question;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
        this.updatedAt = System.currentTimeMillis();
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    // Utility methods
    public void addOption(String option) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(option);
        this.updatedAt = System.currentTimeMillis();
    }

    public void removeOption(String option) {
        if (this.options != null) {
            this.options.remove(option);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public void incrementReviewCount() {
        this.reviewCount++;
        this.updatedAt = System.currentTimeMillis();
    }

    public void incrementCorrectCount() {
        this.correctCount++;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getAccuracyRate() {
        if (reviewCount == 0) return 0.0;
        return (double) correctCount / reviewCount * 100;
    }

    public boolean isCorrectAnswer(String userAnswer) {
        if (userAnswer == null) return false;

        switch (type) {
            case BASIC:
                return answer != null && answer.trim().equalsIgnoreCase(userAnswer.trim());
            case MULTIPLE_CHOICE:
            case FILL_IN_BLANK:
                return correctAnswer != null && correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
            default:
                return false;
        }
    }

    public String getDisplayContent() {
        switch (type) {
            case BASIC:
                return "Q: " + question + "\nA: " + answer;
            case MULTIPLE_CHOICE:
                StringBuilder sb = new StringBuilder();
                sb.append("Q: ").append(question).append("\n");
                for (int i = 0; i < options.size(); i++) {
                    sb.append((char)('A' + i)).append(". ").append(options.get(i)).append("\n");
                }
                sb.append("Đáp án: ").append(correctAnswer);
                return sb.toString();
            case FILL_IN_BLANK:
                return "Điền từ: " + question + "\nĐáp án: " + correctAnswer;
            default:
                return question;
        }
    }

    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", question='" + question + '\'' +
                ", reviewCount=" + reviewCount +
                ", accuracy=" + String.format("%.1f", getAccuracyRate()) + "%" +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return id != null && id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}