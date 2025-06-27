package com.example.memorix.model;

public class Deck {
    private long id;
    private String name;
    private String description;
    private int totalCards;
    private String imageUrl;
    private boolean isPublic;


    public Deck() {}

    public Deck(long id, String name, String description, int totalCards, String imageUrl, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalCards = totalCards;
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
    public int getTotalCards() {
        return totalCards;
    }
    public void setTotalCards(int cardCount) {
        this.totalCards = cardCount;
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