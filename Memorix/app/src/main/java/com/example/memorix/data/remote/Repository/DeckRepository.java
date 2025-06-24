package com.example.memorix.data.remote.Repository;

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

    // Updated createDeck method to match the new API
    public void createDeck(String name, String description, String imageUrl, boolean isPublic, String token) {
        loadingState.setValue(true);

        // Create request using the new DTO structure
        DeckCreateRequest request = new DeckCreateRequest(name, description, imageUrl, isPublic);

        Call<DeckCreateResponse> call = apiService.createDeck("Bearer " + token, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeckCreateResponse> call, @NonNull Response<DeckCreateResponse> response) {
                loadingState.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    createDeckSuccessLiveData.setValue(response.body());
                    // Refresh danh sách decks sau khi tạo thành công
                    fetchDecks(token);
                } else {
                    handleCreateDeckError(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeckCreateResponse> call, @NonNull Throwable t) {
                loadingState.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    // Overloaded method for backward compatibility (without imageUrl)
    public void createDeck(String name, String description, boolean isPublic, String token) {
        createDeck(name, description, null, isPublic, token);
    }

    private void handleCreateDeckError(int responseCode) {
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
            case 409:
                errorMessage.setValue("Bộ thẻ với tên này đã tồn tại");
                break;
            case 422:
                errorMessage.setValue("Dữ liệu không hợp lệ");
                break;
            default:
                errorMessage.setValue("Failed to create deck");
                break;
        }
    }

    public void updateDeck(long deckId, String name, String description, String imageUrl, boolean isPublic, String token) {
        loadingState.setValue(true);

        // Create request using the new DTO structure
        DeckUpdateRequest request = new DeckUpdateRequest(name, description, imageUrl, isPublic);

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