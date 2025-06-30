package com.example.memorix.data.remote.dto.Deck;

public class DeckUpdateRequest {
    private String name;
    private String description;
    private String image_url;
    private boolean is_public;
    private String category;

    public DeckUpdateRequest(String name, String description, String image_url, boolean is_public, String category) {
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.is_public = is_public;
        this.category = category;
    }

    // Constructor without category (for backward compatibility)
    public DeckUpdateRequest(String name, String description, String image_url, boolean is_public) {
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.is_public = is_public;
        this.category = null;
    }

    // Constructor without image_url for backward compatibility
    public DeckUpdateRequest(String name, String description, boolean is_public) {
        this.name = name;
        this.description = description;
        this.image_url = null;
        this.is_public = is_public;
        this.category = null;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public boolean is_public() {
        return is_public;
    }

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "DeckUpdateRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image_url='" + image_url + '\'' +
                ", is_public=" + is_public +
                ", category='" + category + '\'' +
                '}';
    }
}