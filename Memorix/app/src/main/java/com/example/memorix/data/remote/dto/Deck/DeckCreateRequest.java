package com.example.memorix.data.remote.dto.Deck;

public class DeckCreateRequest {
    private String name;
    private String description;
    private String image_url;
    private boolean is_public;

    public DeckCreateRequest(String name, String description, String image_url, boolean is_public) {
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.is_public = is_public;
    }

    // Constructor without image_url (for cases where image is optional)
    public DeckCreateRequest(String name, String description, boolean is_public) {
        this.name = name;
        this.description = description;
        this.image_url = null;
        this.is_public = is_public;
    }

    // Getters
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
}
