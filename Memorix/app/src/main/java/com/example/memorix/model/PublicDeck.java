package com.example.memorix.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class PublicDeck {
    @SerializedName("deck_id")
    private int deckId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String imageUrl;


    @SerializedName("is_public")
    private boolean isPublic;

    @SerializedName("total_cards")
    private int totalCards;
    @SerializedName("category")
    private String category;
    @SerializedName("owner_username")
    private String ownerUsername;

    // Constructors
    public PublicDeck() {}

    public PublicDeck(int deckId, String name, String description, String category, int totalCards, String ownerUsername) {
        this.deckId = deckId;
        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.totalCards = totalCards;
        this.category = category;
        this.isPublic = true;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotal_cards() {
        return totalCards;
    }

    public void setTotal_cards(int totalCards) {
        this.totalCards = totalCards;
    }

    // Getters
    public int getDeckId() { return deckId; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public boolean isPublic() { return isPublic; }
    public String getOwnerUsername() { return ownerUsername; }

    // Convert to Deck object for adapter compatibility
    public Deck toDeck() {
        Log.e("Error", this.totalCards + " " + this.name + " " + this.category);
        return new Deck(deckId, name, description, totalCards, false, category);
    }
}
