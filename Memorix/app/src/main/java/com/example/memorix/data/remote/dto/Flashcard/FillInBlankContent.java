package com.example.memorix.data.remote.dto.Flashcard;

public class FillInBlankContent {
    private String text;
    private String answer;

    public FillInBlankContent() {}

    public FillInBlankContent(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
