package com.example.memorix.model;

public class Deck {
    private long id;
    private String name;
    private String description;
    private int cardCount;
    private String imageUrl;
    private boolean isPublic;

    public Deck(long id, String name, String description, int cardCount, String imageUrl, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cardCount = cardCount;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
    public int getCardCount() {
        return cardCount;
    }
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}