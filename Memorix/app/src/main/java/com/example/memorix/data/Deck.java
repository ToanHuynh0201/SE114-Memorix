package com.example.memorix.data;

public class Deck {
    private long id;
    private String name;
    private String description;
    private int cardCount;
    private int masteredCount;

    public Deck(long id, String name, String description, int cardCount, int masteredCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cardCount = cardCount;
        this.masteredCount = masteredCount;
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

    public int getMasteredCount() {
        return masteredCount;
    }

    public void setMasteredCount(int masteredCount) {
        this.masteredCount = masteredCount;
    }
}