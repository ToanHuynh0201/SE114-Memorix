package com.example.memorix.data.remote.Repository;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorix.data.remote.api.DeckApi;
import com.example.memorix.data.remote.api.ShareApi;
import com.example.memorix.data.remote.dto.Deck.CloneResponse;
import com.example.memorix.data.remote.dto.Deck.PublicDecksResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.model.Deck;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
public class DeckLibraryRepository {
    private DeckApi apiService;

    private MutableLiveData<List<Deck>> publicDecksLiveData;
    private MutableLiveData<Boolean> loadingLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<Boolean> cloneLoadingLiveData;
    private MutableLiveData<String> cloneSuccessLiveData;
    private MutableLiveData<String> cloneErrorLiveData;

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

    public void fetchPublicDecks() {
        loadingLiveData.setValue(true);

        apiService.getPublicDecks().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PublicDecksResponse> call, Response<PublicDecksResponse> response) {
                loadingLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    PublicDecksResponse apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Deck> deckList = new ArrayList<>();
                        for (com.example.memorix.model.PublicDeck publicDeck : apiResponse.getData()) {
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

            @Override
            public void onFailure(Call<PublicDecksResponse> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void cloneDeck(long deckId, String token) {
        cloneLoadingLiveData.setValue(true);

        apiService.cloneDeck(token, deckId).enqueue(new Callback<CloneResponse>() {
            @Override
            public void onResponse(Call<CloneResponse> call, Response<CloneResponse> response) {
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
            public void onFailure(Call<CloneResponse> call, Throwable t) {
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
