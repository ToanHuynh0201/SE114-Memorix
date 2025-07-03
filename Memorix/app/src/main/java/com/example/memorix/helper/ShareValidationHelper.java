package com.example.memorix.helper;

import android.util.Patterns;

public class ShareValidationHelper {
    public static String validateShareRequest(long deckId, String receiverEmail,
                                              String permissionLevel, String token) {

        // Validate deck ID
        if (deckId <= 0) {
            return "ID bộ thẻ không hợp lệ";
        }

        // Validate email
        if (receiverEmail == null || receiverEmail.trim().isEmpty()) {
            return "Email người nhận không được để trống";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(receiverEmail.trim()).matches()) {
            return "Email người nhận không đúng định dạng";
        }

        // Validate permission level
        if (permissionLevel == null || permissionLevel.trim().isEmpty()) {
            return "Quyền hạn không được để trống";
        }

        if (!isValidPermission(permissionLevel)) {
            return "Quyền hạn không hợp lệ";
        }

        // Validate token
        if (token == null || token.trim().isEmpty()) {
            return "Token xác thực không hợp lệ";
        }

        return null; // All valid
    }

    private static boolean isValidPermission(String permission) {
        return "view".equalsIgnoreCase(permission) ||
                "edit".equalsIgnoreCase(permission);
    }

    public static String formatEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
}