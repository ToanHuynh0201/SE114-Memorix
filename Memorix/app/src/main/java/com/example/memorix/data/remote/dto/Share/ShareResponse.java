package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;

public class ShareResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private ShareData data;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ShareData getData() {
        return data;
    }

    public void setData(ShareData data) {
        this.data = data;
    }

    public static class ShareData {
        @SerializedName("share_id")
        private long shareId;

        @SerializedName("deck_id")
        private long deckId;

        @SerializedName("shared_by_user_id")
        private long sharedByUserId;

        @SerializedName("shared_with_user_id")
        private long sharedWithUserId;

        @SerializedName("permission_level")
        private String permissionLevel;

        @SerializedName("status")
        private String status;

        @SerializedName("created_at")
        private String createdAt;

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

        public String getPermissionLevel() {
            return permissionLevel;
        }

        public void setPermissionLevel(String permissionLevel) {
            this.permissionLevel = permissionLevel;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
