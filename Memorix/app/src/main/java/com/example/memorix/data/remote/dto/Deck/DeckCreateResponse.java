package com.example.memorix.data.remote.dto.Deck;

public class DeckCreateResponse {
    private int deck_id;
    private int user_id;
    private String name;
    private String description;
    private String image_url;
    private boolean is_public;
    private String category;

    public DeckCreateResponse() {
    }

    public DeckCreateResponse(int deck_id, int user_id, String name, String description, String image_url, boolean is_public, String category) {
        this.deck_id = deck_id;
        this.user_id = user_id;
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.is_public = is_public;
        this.category = category;
    }

    // Constructor without category (for backward compatibility)
    public DeckCreateResponse(int deck_id, int user_id, String name, String description, String image_url, boolean is_public) {
        this.deck_id = deck_id;
        this.user_id = user_id;
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.is_public = is_public;
        this.category = null;
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

    public String getCategory() {
        return category;
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

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "DeckCreateResponse{" +
                "deck_id=" + deck_id +
                ", user_id=" + user_id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image_url='" + image_url + '\'' +
                ", is_public=" + is_public +
                ", category='" + category + '\'' +
                '}';
    }
}