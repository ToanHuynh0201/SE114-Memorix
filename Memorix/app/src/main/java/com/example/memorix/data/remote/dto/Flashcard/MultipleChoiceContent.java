package com.example.memorix.data.remote.dto.Flashcard;

import java.util.List;

public class MultipleChoiceContent {
    private String question;
    private List<String> options;
    private String answer;

    public MultipleChoiceContent() {}

    public MultipleChoiceContent(String question, List<String> options, String answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
    }

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
