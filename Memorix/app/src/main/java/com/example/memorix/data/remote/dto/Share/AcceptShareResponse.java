package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;

public class AcceptShareResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ClonedDeck data;

    // Constructors
    public AcceptShareResponse() {}

    public AcceptShareResponse(boolean success, String message, ClonedDeck data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ClonedDeck getData() {
        return data;
    }

    public void setData(ClonedDeck data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AcceptShareResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    // Inner class for cloned deck data
    public static class ClonedDeck {
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

        // Constructors
        public ClonedDeck() {}

        // Getters and Setters
        public long getDeckId() {
            return deckId;
        }

        public void setDeckId(long deckId) {
            this.deckId = deckId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
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

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public boolean isPublic() {
            return isPublic;
        }

        public void setPublic(boolean isPublic) {
            this.isPublic = isPublic;
        }

        @Override
        public String toString() {
            return "ClonedDeck{" +
                    "deckId=" + deckId +
                    ", userId=" + userId +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", isPublic=" + isPublic +
                    '}';
        }
    }
}
