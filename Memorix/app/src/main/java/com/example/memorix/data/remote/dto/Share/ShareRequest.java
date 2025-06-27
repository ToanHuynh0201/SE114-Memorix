package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;

public class ShareRequest {
    @SerializedName("deck_id")
    private long deckId;

    @SerializedName("receiver_email")
    private String receiverEmail;

    @SerializedName("permission_level")
    private String permissionLevel;

    public ShareRequest(long deckId, String receiverEmail, String permissionLevel) {
        this.deckId = deckId;
        this.receiverEmail = receiverEmail;
        this.permissionLevel = permissionLevel;
    }

    // Getters and Setters
    public long getDeckId() {
        return deckId;
    }

    public void setDeckId(long deckId) {
        this.deckId = deckId;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(String permissionLevel) {
        this.permissionLevel = permissionLevel;
    }
}
