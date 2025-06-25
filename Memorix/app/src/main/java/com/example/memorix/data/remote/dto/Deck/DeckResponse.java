package com.example.memorix.data.remote.dto.Deck;

import com.example.memorix.model.Deck;

public class DeckResponse {
    private int deck_id;
    private int user_id;
    private String name;
    private String description;
    private String image_url;
    private boolean is_public;

    public DeckResponse() {
    }

    public DeckResponse(int deck_id, int user_id, String name, String description, String image_url, boolean is_public) {
        this.deck_id = deck_id;
        this.user_id = user_id;
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.is_public = is_public;
    }

    // Getters
    public int getDeck_id() {
        return deck_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }

    public boolean isIs_public() {
        return is_public;
    }

    // Setters
    public void setDeck_id(int deck_id) {
        this.deck_id = deck_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }

    // Utility method to convert to Deck object
    public Deck toDeck() {
        return new Deck(
                this.deck_id,
                this.name,
                this.description != null ? this.description : "",
                0,
                this.image_url,
                false// cardCount - có thể cần update từ API khác
                  // masteredCount - có thể cần update từ API khác
        );
    }
}
