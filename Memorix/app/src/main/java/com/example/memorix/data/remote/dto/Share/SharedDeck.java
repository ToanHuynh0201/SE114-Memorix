package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;

public class SharedDeck {
    @SerializedName("deck_id")
    private long deckId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("is_public")
    private boolean isPublic;

    @SerializedName("image_url")
    private String imageUrl;

    // Constructors
    public SharedDeck() {}

    // Getters and Setters
    public long getDeckId() { return deckId; }
    public void setDeckId(long deckId) { this.deckId = deckId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "SharedDeck{" +
                "deckId=" + deckId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isPublic=" + isPublic +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
