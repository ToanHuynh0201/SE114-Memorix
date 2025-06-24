package com.example.memorix.data.remote.dto.Flashcard;

public class DeleteFlashcardResponse {
    private boolean success;

    public DeleteFlashcardResponse() {}

    public DeleteFlashcardResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "DeleteFlashcardResponse{" +
                "success=" + success +
                '}';
    }
}
