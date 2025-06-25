package com.example.memorix.data.remote.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorix.data.remote.api.ShareApi;
import com.example.memorix.data.remote.dto.Share.ErrorResponse;
import com.example.memorix.data.remote.dto.Share.IncomingShare;
import com.example.memorix.data.remote.dto.Share.IncomingSharesResponse;
import com.example.memorix.data.remote.dto.Share.ShareRequest;
import com.example.memorix.data.remote.dto.Share.ShareResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.helper.ShareValidationHelper;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareRepository {
    private ShareApi shareApiService;

    // LiveData for share operations
    private final MutableLiveData<Boolean> shareSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> shareError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> shareLoading = new MutableLiveData<>();

    // LiveData for incoming shares
    private final MutableLiveData<List<IncomingShare>> incomingShares = new MutableLiveData<>();
    private final MutableLiveData<String> incomingSharesError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> incomingSharesLoading = new MutableLiveData<>();

    public ShareRepository() {
        shareApiService = ApiClient.getClient().create(ShareApi.class);
    }

    // Getters for share LiveData
    public LiveData<Boolean> getShareSuccess() {
        return shareSuccess;
    }

    public LiveData<String> getShareError() {
        return shareError;
    }

    public LiveData<Boolean> getShareLoading() {
        return shareLoading;
    }

    // Getters for incoming shares LiveData
    public LiveData<List<IncomingShare>> getIncomingShares() {
        return incomingShares;
    }

    public LiveData<String> getIncomingSharesError() {
        return incomingSharesError;
    }

    public LiveData<Boolean> getIncomingSharesLoading() {
        return incomingSharesLoading;
    }

    /**
     * Lấy danh sách các deck được share đến tài khoản của user
     * @param token Auth token
     */
    public void getIncomingShares(String token) {
        // Reset previous states
        incomingShares.setValue(null);
        incomingSharesError.setValue(null);
        incomingSharesLoading.setValue(true);

        // Validate token
        if (token == null || token.trim().isEmpty()) {
            incomingSharesLoading.setValue(false);
            incomingSharesError.setValue("Token không hợp lệ");
            return;
        }

        // Format token with "Bearer " prefix if not already present
        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        // Log request for debugging
        android.util.Log.d("ShareRepository", "Getting incoming shares");

        // Make API call
        Call<IncomingSharesResponse> call = shareApiService.getIncomingShares(authToken);

        call.enqueue(new Callback<IncomingSharesResponse>() {
            @Override
            public void onResponse(Call<IncomingSharesResponse> call, Response<IncomingSharesResponse> response) {
                incomingSharesLoading.setValue(false);

                // Log response for debugging
                android.util.Log.d("ShareRepository", "Incoming shares response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    IncomingSharesResponse sharesResponse = response.body();
                    android.util.Log.d("ShareRepository", "Response success: " + sharesResponse.isSuccess());

                    // LOG CHI TIẾT DATA
                    if (sharesResponse.getData() != null) {
                        android.util.Log.d("ShareRepository", "Data size: " + sharesResponse.getData().size());
                        for (int i = 0; i < sharesResponse.getData().size(); i++) {
                            IncomingShare share = sharesResponse.getData().get(i);
                            android.util.Log.d("ShareRepository", "Share " + i + ": " + share.toString());
                        }
                    } else {
                        android.util.Log.d("ShareRepository", "Data is null");
                    }

                    if (sharesResponse.isSuccess()) {
                        incomingShares.setValue(sharesResponse.getData());
                        incomingSharesError.setValue(null);
                        android.util.Log.d("ShareRepository", "Successfully loaded " +
                                (sharesResponse.getData() != null ? sharesResponse.getData().size() : 0) + " incoming shares");
                    } else {
                        incomingShares.setValue(null);
                        incomingSharesError.setValue("Không thể lấy danh sách chia sẻ. Vui lòng thử lại.");
                    }
                } else {
                    // Handle HTTP error codes
                    incomingShares.setValue(null);

                    // Try to get error body for more details
                    String errorBody = "";
                    String detailedErrorMessage = null;

                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("ShareRepository", "Error body: " + errorBody);
                            detailedErrorMessage = parseErrorResponse(errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("ShareRepository", "Could not read error body", e);
                        }
                    }

                    String errorMessage = handleIncomingSharesError(response.code(), errorBody, detailedErrorMessage);
                    incomingSharesError.setValue(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<IncomingSharesResponse> call, Throwable t) {
                incomingSharesLoading.setValue(false);
                incomingShares.setValue(null);

                // Log failure for debugging
                android.util.Log.e("ShareRepository", "Network failure for incoming shares", t);

                // Handle network errors
                if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    incomingSharesError.setValue("Kết nối timeout. Vui lòng kiểm tra mạng và thử lại.");
                } else if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    incomingSharesError.setValue("Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.");
                } else {
                    incomingSharesError.setValue("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "Không xác định"));
                }
            }
        });
    }

    /**
     * Share deck với user khác
     * @param deckId ID của deck cần share
     * @param receiverEmail Email của người nhận
     * @param permissionLevel Quyền hạn ("view", "edit", etc.)
     * @param token Auth token
     */
    public void shareDeck(long deckId, String receiverEmail, String permissionLevel, String token) {
        // Reset previous states
        shareSuccess.setValue(null);
        shareError.setValue(null);
        shareLoading.setValue(true);

        // Validate input trước khi gửi request
        String validationError = ShareValidationHelper.validateShareRequest(
                deckId, receiverEmail, permissionLevel, token);

        if (validationError != null) {
            shareLoading.setValue(false);
            shareSuccess.setValue(false);
            shareError.setValue(validationError);
            return;
        }

        // Format email
        String formattedEmail = ShareValidationHelper.formatEmail(receiverEmail);

        // Log request for debugging (remove in production)
        ShareValidationHelper.logShareRequest(deckId, formattedEmail, permissionLevel, token);
        ShareValidationHelper.debugShareIssues(deckId, formattedEmail, permissionLevel, token);

        // Create request
        ShareRequest request = new ShareRequest(deckId, formattedEmail, permissionLevel);

        // Format token with "Bearer " prefix if not already present
        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        // Log request for debugging
        android.util.Log.d("ShareRepository", "Sharing deck - ID: " + deckId +
                ", Email: " + formattedEmail + ", Permission: " + permissionLevel);

        // Make API call
        Call<ShareResponse> call = shareApiService.shareDeck(authToken, request);

        call.enqueue(new Callback<ShareResponse>() {
            @Override
            public void onResponse(Call<ShareResponse> call, Response<ShareResponse> response) {
                shareLoading.setValue(false);

                // Log response for debugging
                android.util.Log.d("ShareRepository", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ShareResponse shareResponse = response.body();
                    android.util.Log.d("ShareRepository", "Response success: " + shareResponse.isSuccess());

                    if (shareResponse.isSuccess()) {
                        shareSuccess.setValue(true);
                        shareError.setValue(null);
                    } else {
                        shareSuccess.setValue(false);
                        shareError.setValue("Chia sẻ thất bại. Vui lòng thử lại.");
                    }
                } else {
                    // Handle HTTP error codes with detailed logging
                    shareSuccess.setValue(false);

                    // Try to get error body for more details
                    String errorBody = "";
                    String detailedErrorMessage = null;

                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("ShareRepository", "Error body: " + errorBody);

                            // Try to parse error response JSON
                            detailedErrorMessage = parseErrorResponse(errorBody);

                        } catch (Exception e) {
                            android.util.Log.e("ShareRepository", "Could not read error body", e);
                        }
                    }

                    String errorMessage = handleShareError(response.code(), errorBody, detailedErrorMessage);
                    shareError.setValue(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ShareResponse> call, Throwable t) {
                shareLoading.setValue(false);
                shareSuccess.setValue(false);

                // Log failure for debugging
                android.util.Log.e("ShareRepository", "Network failure", t);

                // Handle network errors
                if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    shareError.setValue("Kết nối timeout. Vui lòng kiểm tra mạng và thử lại.");
                } else if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    shareError.setValue("Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.");
                } else {
                    shareError.setValue("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "Không xác định"));
                }
            }
        });
    }

    /**
     * Handle different HTTP error codes for incoming shares operation
     */
    private String handleIncomingSharesError(int code, String errorBody, String detailedError) {
        android.util.Log.e("ShareRepository", "Incoming shares HTTP Error " + code + ", Body: " + errorBody);

        String baseMessage;
        switch (code) {
            case 401:
                baseMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại";
                break;
            case 403:
                baseMessage = "Bạn không có quyền truy cập thông tin này";
                break;
            case 429:
                baseMessage = "Bạn đã thực hiện quá nhiều yêu cầu. Vui lòng thử lại sau";
                break;
            case 500:
                if (detailedError != null) {
                    baseMessage = "Lỗi server: " + detailedError;
                } else {
                    baseMessage = "Server đang gặp sự cố. Vui lòng thử lại sau";
                }
                break;
            case 502:
            case 503:
            case 504:
                baseMessage = "Server tạm thời không khả dụng. Vui lòng thử lại sau";
                break;
            default:
                if (detailedError != null) {
                    baseMessage = "Lỗi: " + detailedError + " (mã lỗi: " + code + ")";
                } else {
                    baseMessage = "Không thể tải danh sách chia sẻ (mã lỗi: " + code + "). Vui lòng thử lại";
                }
                break;
        }

        return baseMessage;
    }

    /**
     * Parse error response JSON to get detailed error message
     */
    private String parseErrorResponse(String errorBody) {
        if (errorBody == null || errorBody.isEmpty()) {
            return null;
        }

        try {
            Gson gson = new Gson();
            ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);

            android.util.Log.d("ShareRepository", "Parsed error: " + errorResponse.toString());

            // Return the most detailed message available
            if (errorResponse.getDetails() != null && !errorResponse.getDetails().isEmpty()) {
                return errorResponse.getDetails();
            } else if (errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                return errorResponse.getMessage();
            } else if (errorResponse.getError() != null && !errorResponse.getError().isEmpty()) {
                return errorResponse.getError();
            }

        } catch (Exception e) {
            android.util.Log.e("ShareRepository", "Failed to parse error response JSON", e);
        }

        return null;
    }

    /**
     * Handle different HTTP error codes for share operation
     * @param code HTTP status code
     * @param errorBody Response error body (có thể chứa thông tin chi tiết từ server)
     * @param detailedError Chi tiết lỗi từ server nếu có
     */
    private String handleShareError(int code, String errorBody, String detailedError) {
        // Log error details for debugging
        android.util.Log.e("ShareRepository", "HTTP Error " + code + ", Body: " + errorBody);

        String baseMessage;
        switch (code) {
            case 400:
                baseMessage = "Email người nhận không hợp lệ hoặc thông tin không đúng";
                break;
            case 401:
                baseMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại";
                break;
            case 403:
                baseMessage = "Bạn không có quyền chia sẻ bộ thẻ này";
                break;
            case 404:
                baseMessage = "Bộ thẻ không tồn tại hoặc đã bị xóa";
                break;
            case 409:
                baseMessage = "Bạn đã chia sẻ bộ thẻ này với người dùng này rồi";
                break;
            case 422:
                baseMessage = "Email người nhận không tồn tại trong hệ thống";
                break;
            case 429:
                baseMessage = "Bạn đã thực hiện quá nhiều yêu cầu. Vui lòng thử lại sau";
                break;
            case 500:
                // Specific handling for server errors
                if (detailedError != null) {
                    if (detailedError.toLowerCase().contains("failed to share deck")) {
                        baseMessage = "Không thể chia sẻ bộ thẻ. Có thể do:\n" +
                                "• Email người nhận không tồn tại\n" +
                                "• Bộ thẻ đã được chia sẻ trước đó\n" +
                                "• Lỗi hệ thống server";
                    } else {
                        baseMessage = "Lỗi server: " + detailedError;
                    }
                } else {
                    baseMessage = "Server đang gặp sự cố. Vui lòng thử lại sau";
                }
                break;
            case 502:
            case 503:
            case 504:
                baseMessage = "Server tạm thời không khả dụng. Vui lòng thử lại sau";
                break;
            default:
                if (detailedError != null) {
                    baseMessage = "Lỗi: " + detailedError + " (mã lỗi: " + code + ")";
                } else {
                    baseMessage = "Lỗi không xác định (mã lỗi: " + code + "). Vui lòng thử lại";
                }
                break;
        }

        return baseMessage;
    }

    /**
     * Reset share states - useful when starting a new share operation
     */
    public void resetShareStates() {
        shareSuccess.setValue(null);
        shareError.setValue(null);
        shareLoading.setValue(false);
    }

    /**
     * Reset incoming shares states
     */
    public void resetIncomingSharesStates() {
        incomingShares.setValue(null);
        incomingSharesError.setValue(null);
        incomingSharesLoading.setValue(false);
    }

    /**
     * Clear all states - useful for cleanup
     */
    public void clearStates() {
        shareSuccess.setValue(null);
        shareError.setValue(null);
        shareLoading.setValue(null);
        incomingShares.setValue(null);
        incomingSharesError.setValue(null);
        incomingSharesLoading.setValue(null);
    }
}