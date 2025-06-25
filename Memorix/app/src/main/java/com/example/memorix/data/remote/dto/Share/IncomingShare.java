package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;

public class IncomingShare {
    @SerializedName("share_id")
    private long shareId;

    @SerializedName("deck_id")
    private long deckId;

    @SerializedName("shared_by_user_id")
    private long sharedByUserId;

    @SerializedName("shared_with_user_id")
    private long sharedWithUserId;

    @SerializedName("shared_with_group_id")
    private Long sharedWithGroupId; // nullable

    @SerializedName("permission_level")
    private String permissionLevel;

    @SerializedName("shared_at")  // THAY ĐỔI: shared_at thay vì created_at
    private String sharedAt;

    @SerializedName("status")
    private String status;

    // THÔNG TIN DECK - FLATTEN
    @SerializedName("deck_name")
    private String deckName;

    @SerializedName("deck_description")
    private String deckDescription;

    // Constructors
    public IncomingShare() {}

    // Getters and Setters
    public long getShareId() {
        return shareId;
    }

    public void setShareId(long shareId) {
        this.shareId = shareId;
    }

    public long getDeckId() {
        return deckId;
    }

    public void setDeckId(long deckId) {
        this.deckId = deckId;
    }

    public long getSharedByUserId() {
        return sharedByUserId;
    }

    public void setSharedByUserId(long sharedByUserId) {
        this.sharedByUserId = sharedByUserId;
    }

    public long getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(long sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public Long getSharedWithGroupId() {
        return sharedWithGroupId;
    }

    public void setSharedWithGroupId(Long sharedWithGroupId) {
        this.sharedWithGroupId = sharedWithGroupId;
    }

    public String getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(String permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public String getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(String sharedAt) {
        this.sharedAt = sharedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public String getDeckDescription() {
        return deckDescription;
    }

    public void setDeckDescription(String deckDescription) {
        this.deckDescription = deckDescription;
    }

    // HELPER METHODS để tương thích với code cũ
    public SharedDeck getDeck() {
        // Tạo SharedDeck object từ flatten fields
        SharedDeck deck = new SharedDeck();
        deck.setDeckId(this.deckId);
        deck.setName(this.deckName);
        deck.setDescription(this.deckDescription);
        deck.setPublic(false); // default value
        deck.setImageUrl(null); // không có trong response
        return deck;
    }

    public FromUser getFromUser() {
        // Response không có thông tin user, trả về null hoặc placeholder
        // Có thể cần gọi API khác để lấy thông tin user từ sharedByUserId
        return null;
    }

    public String getCreatedAt() {
        // Map sang field cũ để tương thích
        return this.sharedAt;
    }

    @Override
    public String toString() {
        return "IncomingShare{" +
                "shareId=" + shareId +
                ", deckId=" + deckId +
                ", sharedByUserId=" + sharedByUserId +
                ", sharedWithUserId=" + sharedWithUserId +
                ", sharedWithGroupId=" + sharedWithGroupId +
                ", permissionLevel='" + permissionLevel + '\'' +
                ", sharedAt='" + sharedAt + '\'' +
                ", status='" + status + '\'' +
                ", deckName='" + deckName + '\'' +
                ", deckDescription='" + deckDescription + '\'' +
                '}';
    }
}
