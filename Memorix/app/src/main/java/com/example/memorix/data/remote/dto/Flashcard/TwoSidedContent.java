package com.example.memorix.data.remote.dto.Flashcard;

public class TwoSidedContent {
    private String front;
    private String back;

    public TwoSidedContent() {}

    public TwoSidedContent(String front, String back) {
        this.front = front;
        this.back = back;
    }

    // Getters and Setters
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }
}
