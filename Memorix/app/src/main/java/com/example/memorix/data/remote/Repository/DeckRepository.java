package com.example.memorix.data.remote.Repository;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorix.data.remote.dto.Deck.DeckDeleteResponse;
import com.example.memorix.data.remote.dto.Deck.DeckUpdateRequest;
import com.example.memorix.model.Deck;
import com.example.memorix.data.remote.api.DeckApi;
import com.example.memorix.data.remote.dto.Deck.DeckCreateRequest;
import com.example.memorix.data.remote.dto.Deck.DeckCreateResponse;
import com.example.memorix.data.remote.dto.Deck.DeckResponse;
import com.example.memorix.data.remote.network.ApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeckRepository {
    private final DeckApi apiService;
    private final MutableLiveData<List<Deck>> decksLiveData;
    private final MutableLiveData<Boolean> loadingState;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Deck> deckDetailLiveData;
    private final MutableLiveData<Boolean> updateSuccessLiveData;
    private final MutableLiveData<DeckCreateResponse> createDeckSuccessLiveData;
    private final MutableLiveData<Boolean> deleteSuccessLiveData;

    public DeckRepository() {
        apiService = ApiClient.getClient().create(DeckApi.class);
        decksLiveData = new MutableLiveData<>();
        loadingState = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        deckDetailLiveData = new MutableLiveData<>();
        updateSuccessLiveData = new MutableLiveData<>();
        createDeckSuccessLiveData = new MutableLiveData<>();
        deleteSuccessLiveData = new MutableLiveData<>();
    }

    public LiveData<Deck> getDeckDetail() {
        return deckDetailLiveData;
    }

    public LiveData<List<Deck>> getDecks() {
        return decksLiveData;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public LiveData<DeckCreateResponse> getCreateDeckSuccess() {
        return createDeckSuccessLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccessLiveData;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccessLiveData;
    }

    public void fetchDecks(String token) {
        loadingState.setValue(true);

        Call<List<DeckResponse>> call = apiService.getDecks("Bearer " + token);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DeckResponse>> call, @NonNull Response<List<DeckResponse>> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Convert DeckResponse list to Deck list
                    List<Deck> deckList = new ArrayList<>();
                    for (DeckResponse deckResponse : response.body()) {
                        deckList.add(deckResponse.toDeck());
                    }
                    decksLiveData.setValue(deckList);
                } else {
                    handleFetchDecksError(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DeckResponse>> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void searchDecks(String token, String searchQuery) {
        loadingState.setValue(true);

        Call<List<DeckResponse>> call = apiService.getDecksWithSearch("Bearer " + token, searchQuery);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DeckResponse>> call, @NonNull Response<List<DeckResponse>> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Convert DeckResponse list to Deck list
                    List<Deck> deckList = new ArrayList<>();
                    for (DeckResponse deckResponse : response.body()) {
                        deckList.add(deckResponse.toDeck());
                    }
                    decksLiveData.setValue(deckList);
                } else {
                    handleFetchDecksError(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DeckResponse>> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    private void handleFetchDecksError(int responseCode) {
        switch (responseCode) {
            case 401:
                errorMessage.setValue("Phiên đăng nhập đã hết hạn");
                break;
            case 403:
                errorMessage.setValue("Không có quyền truy cập");
                break;
            case 404:
                errorMessage.setValue("Không tìm thấy dữ liệu");
                break;
            case 500:
                errorMessage.setValue("Lỗi máy chủ");
                break;
            default:
                errorMessage.setValue("Failed to load decks");
                break;
        }
    }

    public void fetchDeckById(long deckId, String token) {
        loadingState.setValue(true);

        Call<Deck> call = apiService.getDeckById("Bearer " + token, deckId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Deck> call, @NonNull Response<Deck> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    deckDetailLiveData.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to load deck details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Deck> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteDeck(long deckId, String token) {
        loadingState.setValue(true);

        Call<DeckDeleteResponse> call = apiService.deleteDeck("Bearer " + token, deckId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeckDeleteResponse> call, @NonNull Response<DeckDeleteResponse> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        deleteSuccessLiveData.setValue(true);
                        // Refresh danh sách decks sau khi xóa thành công
                        fetchDecks(token);
                    } else {
                        deleteSuccessLiveData.setValue(false);
                        errorMessage.setValue("Failed to delete deck");
                    }
                } else {
                    deleteSuccessLiveData.setValue(false);
                    handleDeleteDeckError(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeckDeleteResponse> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                deleteSuccessLiveData.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    private void handleDeleteDeckError(int responseCode) {
        switch (responseCode) {
            case 400:
                errorMessage.setValue("Yêu cầu không hợp lệ");
                break;
            case 401:
                errorMessage.setValue("Phiên đăng nhập đã hết hạn");
                break;
            case 403:
                errorMessage.setValue("Không có quyền xóa bộ thẻ này");
                break;
            case 404:
                errorMessage.setValue("Không tìm thấy bộ thẻ");
                break;
            case 500:
                errorMessage.setValue("Lỗi máy chủ");
                break;
            default:
                errorMessage.setValue("Failed to delete deck");
                break;
        }
    }
    /**
     * Convert color ID (1-6) to valid image URL
     */
    private String convertColorIdToImageUrl(String colorId) {
        if (colorId == null || colorId.trim().isEmpty()) {
            return "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Color1"; // Default color 1
        }

        try {
            int id = Integer.parseInt(colorId);
            switch (id) {
                case 1:
                    return "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Color1";
                case 2:
                    return "https://via.placeholder.com/300x200/A8E6CF/FFFFFF?text=Color2";
                case 3:
                    return "https://via.placeholder.com/300x200/FFD93D/FFFFFF?text=Color3";
                case 4:
                    return "https://via.placeholder.com/300x200/6C5CE7/FFFFFF?text=Color4";
                case 5:
                    return "https://via.placeholder.com/300x200/FD79A8/FFFFFF?text=Color5";
                case 6:
                    return "https://via.placeholder.com/300x200/00B894/FFFFFF?text=Color6";
                default:
                    return "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Color1";
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid color ID format: " + colorId + ", using default");
            return "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Color1";
        }
    }

    /**
     * Extract color ID from image URL
     */
    private String extractColorIdFromImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "1"; // Default color ID
        }

        // Check if it's already a color ID (1-6)
        try {
            int id = Integer.parseInt(imageUrl);
            if (id >= 1 && id <= 6) {
                return imageUrl; // It's already a color ID
            }
        } catch (NumberFormatException e) {
            // It's a URL, try to extract color ID from it
        }

        // Extract color ID from placeholder URL
        if (imageUrl.contains("Color1")) return "1";
        if (imageUrl.contains("Color2")) return "2";
        if (imageUrl.contains("Color3")) return "3";
        if (imageUrl.contains("Color4")) return "4";
        if (imageUrl.contains("Color5")) return "5";
        if (imageUrl.contains("Color6")) return "6";

        return "1"; // Default fallback
    }

    // Updated createDeck method to handle color ID conversion
    public void createDeck(String name, String description, String colorId, boolean isPublic, String token) {
        Log.d(TAG, "Creating deck with params:");
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Description: " + description);
        Log.d(TAG, "ColorId: " + colorId);
        Log.d(TAG, "IsPublic: " + isPublic);

        loadingState.setValue(true);

        // Validate input parameters
        if (name == null || name.trim().isEmpty()) {
            Log.e(TAG, "Deck name is null or empty");
            loadingState.setValue(false);
            errorMessage.setValue("Tên bộ thẻ không được để trống");
            return;
        }

        if (token == null || token.trim().isEmpty()) {
            Log.e(TAG, "Token is null or empty");
            loadingState.setValue(false);
            errorMessage.setValue("Phiên đăng nhập đã hết hạn");
            return;
        }

        // Convert color ID to valid image URL
        String imageUrl = convertColorIdToImageUrl(colorId);
        Log.d(TAG, "Converted colorId " + colorId + " to imageUrl: " + imageUrl);

        // Create request using the new DTO structure
        DeckCreateRequest request = new DeckCreateRequest(
                name.trim(),
                description != null ? description.trim() : "",
                imageUrl,  // Use converted URL instead of ID
                isPublic
        );

        Log.d(TAG, "Request object: " + request.toString());

        Call<DeckCreateResponse> call = apiService.createDeck("Bearer " + token, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeckCreateResponse> call, @NonNull Response<DeckCreateResponse> response) {
                loadingState.setValue(false);

                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Deck created successfully: " + response.body().toString());
                    createDeckSuccessLiveData.setValue(response.body());
                    // Refresh danh sách decks sau khi tạo thành công
                    fetchDecks(token);
                } else {
                    Log.e(TAG, "Create deck failed with code: " + response.code());

                    // Log error body if available
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to read error body", e);
                        }
                    }

                    handleCreateDeckError(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeckCreateResponse> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                Log.e(TAG, "Network error during deck creation", t);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    // Overloaded method for backward compatibility (without colorId)
    public void createDeck(String name, String description, boolean isPublic, String token) {
        Log.d(TAG, "Creating deck without colorId - using default color");
        createDeck(name, description, "1", isPublic, token); // Default to color ID 1
    }

    private void handleCreateDeckError(int responseCode) {
        String errorMsg;
        switch (responseCode) {
            case 400:
                errorMsg = "Thông tin bộ thẻ không hợp lệ";
                break;
            case 401:
                errorMsg = "Phiên đăng nhập đã hết hạn";
                break;
            case 403:
                errorMsg = "Không có quyền thực hiện hành động này";
                break;
            case 409:
                errorMsg = "Bộ thẻ với tên này đã tồn tại";
                break;
            case 422:
                errorMsg = "Dữ liệu không hợp lệ";
                break;
            case 500:
                errorMsg = "Lỗi máy chủ nội bộ";
                break;
            default:
                errorMsg = "Không thể tạo bộ thẻ (Mã lỗi: " + responseCode + ")";
                break;
        }
        Log.e(TAG, "Create deck error: " + errorMsg);
        errorMessage.setValue(errorMsg);
    }

    public void updateDeck(long deckId, String name, String description, String imageUrl, boolean isPublic, String token) {
        loadingState.setValue(true);

        String newUrl = convertColorIdToImageUrl(imageUrl);
        // Create request using the new DTO structure
        DeckUpdateRequest request = new DeckUpdateRequest(name, description, newUrl, isPublic);

        Call<DeckResponse> call = apiService.updateDeck("Bearer " + token, deckId, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeckResponse> call, @NonNull Response<DeckResponse> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    updateSuccessLiveData.setValue(true);
                    // Refresh danh sách decks sau khi cập nhật thành công
                    fetchDecks(token);
                } else {
                    updateSuccessLiveData.setValue(false);
                    handleUpdateDeckError(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeckResponse> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                updateSuccessLiveData.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void updateDeck(long deckId, String name, String description, boolean isPublic, String token) {
        updateDeck(deckId, name, description, null, isPublic, token);
    }

    // Old method for compatibility with existing Deck object
    public void updateDeck(long deckId, Deck updatedDeck, String token) {
        updateDeck(deckId, updatedDeck.getName(), updatedDeck.getDescription(), null, false, token);
    }

    private void handleUpdateDeckError(int responseCode) {
        switch (responseCode) {
            case 400:
                errorMessage.setValue("Thông tin bộ thẻ không hợp lệ");
                break;
            case 401:
                errorMessage.setValue("Phiên đăng nhập đã hết hạn");
                break;
            case 403:
                errorMessage.setValue("Không có quyền thực hiện hành động này");
                break;
            case 404:
                errorMessage.setValue("Không tìm thấy bộ thẻ");
                break;
            case 409:
                errorMessage.setValue("Bộ thẻ với tên này đã tồn tại");
                break;
            case 422:
                errorMessage.setValue("Dữ liệu không hợp lệ");
                break;
            default:
                errorMessage.setValue("Failed to update deck");
                break;
        }
    }
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

}