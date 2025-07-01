package com.example.memorix.data.remote.dto.Progress;

public class ProgressRequest {
    private int flashcard_id;
    private String rating;

    public ProgressRequest(int flashcard_id, String rating) {
        this.flashcard_id = flashcard_id;
        this.rating = rating;
    }
}
