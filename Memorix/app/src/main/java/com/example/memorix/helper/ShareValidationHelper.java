package com.example.memorix.helper;

import android.util.Patterns;

public class ShareValidationHelper {

    /**
     * Validate share request data
     * @param deckId ID của deck
     * @param receiverEmail Email người nhận
     * @param permissionLevel Quyền hạn
     * @param token Auth token
     * @return Error message nếu có lỗi, null nếu valid
     */
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

    /**
     * Check if permission level is valid
     */
    private static boolean isValidPermission(String permission) {
        return "view".equals(permission.toLowerCase()) ||
                "edit".equals(permission.toLowerCase());
    }

    /**
     * Format email to lowercase and trim
     */
    public static String formatEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    /**
     * Debug helper - log request details (remove in production)
     */
    public static void logShareRequest(long deckId, String receiverEmail,
                                       String permissionLevel, String token) {
        android.util.Log.d("ShareValidation", "=== SHARE REQUEST DEBUG ===");
        android.util.Log.d("ShareValidation", "Deck ID: " + deckId);
        android.util.Log.d("ShareValidation", "Receiver Email: " + receiverEmail);
        android.util.Log.d("ShareValidation", "Permission: " + permissionLevel);
        android.util.Log.d("ShareValidation", "Token present: " + (token != null && !token.isEmpty()));
        android.util.Log.d("ShareValidation", "Token length: " + (token != null ? token.length() : 0));
        android.util.Log.d("ShareValidation", "Token starts with Bearer: " + (token != null && token.startsWith("Bearer")));
        android.util.Log.d("ShareValidation", "===========================");
    }

    /**
     * Comprehensive debugging - check common issues
     */
    public static void debugShareIssues(long deckId, String receiverEmail,
                                        String permissionLevel, String token) {
        android.util.Log.d("ShareDebug", "=== DEBUGGING SHARE ISSUES ===");

        // Check deck ID
        if (deckId <= 0) {
            android.util.Log.e("ShareDebug", "❌ Invalid deck ID: " + deckId);
        } else {
            android.util.Log.i("ShareDebug", "✅ Deck ID valid: " + deckId);
        }

        // Check email format
        if (receiverEmail == null || receiverEmail.isEmpty()) {
            android.util.Log.e("ShareDebug", "❌ Email is null or empty");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(receiverEmail).matches()) {
            android.util.Log.e("ShareDebug", "❌ Email format invalid: " + receiverEmail);
        } else {
            android.util.Log.i("ShareDebug", "✅ Email format valid: " + receiverEmail);
        }

        // Check permission
        if (!"view".equals(permissionLevel) && !"edit".equals(permissionLevel)) {
            android.util.Log.e("ShareDebug", "❌ Invalid permission: " + permissionLevel);
        } else {
            android.util.Log.i("ShareDebug", "✅ Permission valid: " + permissionLevel);
        }

        // Check token
        if (token == null || token.isEmpty()) {
            android.util.Log.e("ShareDebug", "❌ Token is null or empty");
        } else if (token.length() < 20) {
            android.util.Log.w("ShareDebug", "⚠️ Token seems too short: " + token.length() + " chars");
        } else {
            android.util.Log.i("ShareDebug", "✅ Token present: " + token.length() + " chars");
        }

        android.util.Log.d("ShareDebug", "===============================");
    }
}