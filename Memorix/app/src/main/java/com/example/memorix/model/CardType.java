package com.example.memorix.model;

import com.google.gson.annotations.SerializedName;

public enum CardType {
    @SerializedName("two_sided")
    BASIC("two_sided", "2 Mặt"),
    @SerializedName("multiple_choice")
    MULTIPLE_CHOICE("multiple_choice", "Trắc nghiệm"),
    @SerializedName("fill_in_blank")
    FILL_IN_BLANK("fill_in_blank", "Điền từ");

    private final String code;
    private final String displayName;

    CardType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CardType fromCode(String code) {
        for (CardType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return BASIC; // default
    }

    public static CardType fromDisplayName(String displayName) {
        for (CardType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return BASIC; // Default
    }

    @Override
    public String toString() {
        return displayName;
    }
}
