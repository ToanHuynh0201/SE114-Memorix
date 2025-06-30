package com.example.memorix.model;

public class Deck {
    private long id;
    private String name;
    private String description;
    private int totalCards;
    private int unlearnedCards;
    private int learnedCards;
    private int dueCards;
    private String imageUrl;
    private boolean isPublic;
    private String category;

    public Deck() {}

    // Constructor với tất cả fields mới
    public Deck(long id, String name, String description, int totalCards, int unlearnedCards,
                int learnedCards, int dueCards, String imageUrl, boolean isPublic, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalCards = totalCards;
        this.unlearnedCards = unlearnedCards;
        this.learnedCards = learnedCards;
        this.dueCards = dueCards;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
        this.category = category;
    }

    // Constructor cũ để backward compatibility
    public Deck(long id, String name, String description, int totalCards, String imageUrl, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalCards = totalCards;
        this.unlearnedCards = 0;
        this.learnedCards = 0;
        this.dueCards = 0;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
        this.category = "";
    }

    // Getters and Setters
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

    public void setTotalCards(int totalCards) {
        this.totalCards = totalCards;
    }

    public int getUnlearnedCards() {
        return unlearnedCards;
    }

    public void setUnlearnedCards(int unlearnedCards) {
        this.unlearnedCards = unlearnedCards;
    }

    public int getLearnedCards() {
        return learnedCards;
    }

    public void setLearnedCards(int learnedCards) {
        this.learnedCards = learnedCards;
    }

    public int getDueCards() {
        return dueCards;
    }

    public void setDueCards(int dueCards) {
        this.dueCards = dueCards;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Utility methods
    public int getProgressPercentage() {
        if (totalCards == 0) return 0;
        return (int) ((learnedCards * 100.0) / totalCards);
    }

    public boolean hasDueCards() {
        return dueCards > 0;
    }

    public boolean hasUnlearnedCards() {
        return unlearnedCards > 0;
    }
}