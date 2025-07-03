package com.example.memorix.data.remote.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorix.data.remote.api.DeckApi;
import com.example.memorix.data.remote.dto.Deck.CloneResponse;
import com.example.memorix.data.remote.dto.Deck.PublicDecksResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.model.Deck;
import com.example.memorix.model.PublicDeck;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class DeckLibraryRepository {
    private final DeckApi apiService;

    private final MutableLiveData<List<Deck>> publicDecksLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> cloneLoadingLiveData;
    private final MutableLiveData<String> cloneSuccessLiveData;
    private final MutableLiveData<String> cloneErrorLiveData;

    public DeckLibraryRepository() {
        apiService = ApiClient.getClient().create(DeckApi.class);
        publicDecksLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        cloneLoadingLiveData = new MutableLiveData<>();
        cloneSuccessLiveData = new MutableLiveData<>();
        cloneErrorLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Deck>> getPublicDecks() {
        return publicDecksLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getCloneLoading() {
        return cloneLoadingLiveData;
    }

    public LiveData<String> getCloneSuccess() {
        return cloneSuccessLiveData;
    }

    public LiveData<String> getCloneError() {
        return cloneErrorLiveData;
    }

    // Original method - get all public decks
    public void fetchPublicDecks() {
        loadingLiveData.setValue(true);

        apiService.getPublicDecks().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PublicDecksResponse> call, @NonNull Response<PublicDecksResponse> response) {
                loadingLiveData.setValue(false);
                handlePublicDecksResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<PublicDecksResponse> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // New method - search public decks with query and/or category
    public void searchPublicDecks(String searchQuery, String category) {
        loadingLiveData.setValue(true);

        Call<PublicDecksResponse> call;

        // Determine which API method to use based on parameters
        if ((searchQuery == null || searchQuery.trim().isEmpty()) &&
                (category == null || category.trim().isEmpty())) {
            call = apiService.getPublicDecks();
        } else if (searchQuery != null && !searchQuery.trim().isEmpty() &&
                category != null && !category.trim().isEmpty()) {
            // Both search query and category
            call = apiService.getPublicDecksWithSearchAndCategory(searchQuery.trim(), category);
        } else if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Search query only
            call = apiService.getPublicDecksWithSearch(searchQuery.trim());
        } else {
            // Category only
            call = apiService.getPublicDecksWithCategory(category);
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PublicDecksResponse> call, @NonNull Response<PublicDecksResponse> response) {
                loadingLiveData.setValue(false);
                handlePublicDecksResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<PublicDecksResponse> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Helper method to handle API response
    private void handlePublicDecksResponse(Response<PublicDecksResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            PublicDecksResponse apiResponse = response.body();

            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                List<Deck> deckList = new ArrayList<>();
                for (PublicDeck publicDeck : apiResponse.getData()) {
                    deckList.add(publicDeck.toDeck());
                }
                publicDecksLiveData.setValue(deckList);
            } else {
                errorLiveData.setValue("Không thể tải danh sách deck");
            }
        } else {
            errorLiveData.setValue("Lỗi kết nối: " + response.code());
        }
    }

    public void cloneDeck(long deckId, String token) {
        cloneLoadingLiveData.setValue(true);

        apiService.cloneDeck(token, deckId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CloneResponse> call, @NonNull Response<CloneResponse> response) {
                cloneLoadingLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    CloneResponse cloneResponse = response.body();

                    if (cloneResponse.isSuccess()) {
                        cloneSuccessLiveData.setValue(cloneResponse.getMessage());
                    } else {
                        cloneErrorLiveData.setValue("Không thể clone deck");
                    }
                } else {
                    cloneErrorLiveData.setValue("Lỗi kết nối: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CloneResponse> call, @NonNull Throwable t) {
                cloneLoadingLiveData.setValue(false);
                cloneErrorLiveData.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void clearCloneMessages() {
        cloneSuccessLiveData.setValue(null);
        cloneErrorLiveData.setValue(null);
    }
}