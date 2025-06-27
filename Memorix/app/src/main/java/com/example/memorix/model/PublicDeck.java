package com.example.memorix.model;

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

    @SerializedName("owner_username")
    private String ownerUsername;

    // Constructors
    public PublicDeck() {}

    public PublicDeck(int deckId, String name, String description, String ownerUsername) {
        this.deckId = deckId;
        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.isPublic = true;
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
        return new Deck(deckId, name, description, 0, description, false);
    }
}
