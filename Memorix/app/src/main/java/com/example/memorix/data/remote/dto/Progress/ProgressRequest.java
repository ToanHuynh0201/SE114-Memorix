package com.example.memorix.data.remote.dto.Progress;

public class ProgressRequest {
    private int flashcard_id;
    private String rating;
    private String next_review_at;

    public ProgressRequest() {}

    public ProgressRequest(int flashcard_id, String rating) {
        this.flashcard_id = flashcard_id;
        this.rating = rating;
    }

    // Setters & Getters
    public void setFlashcard_id(int flashcard_id) { this.flashcard_id = flashcard_id; }
    public void setRating(String rating) { this.rating = rating; }
    public void setNext_review_at(String next_review_at) { this.next_review_at = next_review_at; }

    public int getFlashcard_id() { return flashcard_id; }
    public String getRating() { return rating; }
    public String getNext_review_at() { return next_review_at; }
}
