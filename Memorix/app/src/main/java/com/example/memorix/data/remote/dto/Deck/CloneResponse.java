package com.example.memorix.data.remote.dto.Deck;

import com.example.memorix.model.Deck;
import com.google.gson.annotations.SerializedName;

public class CloneResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private CloneData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public CloneData getData() {
        return data;
    }

    public static class CloneData {
        @SerializedName("deck_id")
        private long deckId;

        @SerializedName("user_id")
        private long userId;

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("image_url")
        private String imageUrl;

        @SerializedName("is_public")
        private boolean isPublic;

        // Getters
        public long getDeckId() { return deckId; }
        public long getUserId() { return userId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getImageUrl() { return imageUrl; }
        public boolean isPublic() { return isPublic; }

        public Deck toDeck() {
            Deck deck = new Deck();
            deck.setId(deckId);
            deck.setName(name);
            deck.setDescription(description);
            deck.setImageUrl(imageUrl);
            deck.setPublic(isPublic);
            return deck;
        }
    }
}
