package com.example.memorix.ui.deck;

public enum CardType {
    BASIC("BASIC", "2 Mặt"),
    MULTIPLE_CHOICE("MULTIPLE_CHOICE", "Trắc nghiệm"),
    FILL_IN_BLANK("FILL_IN_BLANK", "Điền từ");

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
        return BASIC; // Default
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
