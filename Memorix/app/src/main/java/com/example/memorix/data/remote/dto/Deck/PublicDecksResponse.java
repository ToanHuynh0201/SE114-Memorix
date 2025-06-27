package com.example.memorix.data.remote.dto.Deck;

import com.example.memorix.model.PublicDeck;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PublicDecksResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<PublicDeck> data;

    // Getters
    public boolean isSuccess() { return success; }
    public List<PublicDeck> getData() { return data; }
}
