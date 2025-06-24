package com.example.memorix.data.remote.dto.Deck;

public class DeckDeleteResponse {
    private boolean success;

    public DeckDeleteResponse() {
    }

    public DeckDeleteResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
