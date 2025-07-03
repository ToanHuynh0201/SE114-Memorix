package com.example.memorix.data.remote.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorix.data.remote.dto.Flashcard.CardResponse;
import com.example.memorix.data.remote.dto.Flashcard.DeleteFlashcardResponse;
import com.example.memorix.data.remote.dto.Flashcard.UpdateFlashcardRequest;
import com.example.memorix.data.remote.dto.Flashcard.UpdateFlashcardResponse;
import com.example.memorix.model.Card;
import com.example.memorix.model.CardType;
import com.example.memorix.data.remote.api.FlashcardApi;
import com.example.memorix.data.remote.dto.Flashcard.CreateFlashcardRequest;
import com.example.memorix.data.remote.dto.Flashcard.CreateFlashcardResponse;
import com.example.memorix.data.remote.dto.Flashcard.FlashcardDto;
import com.example.memorix.data.remote.network.ApiClient;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlashcardRepository {
    private final FlashcardApi flashcardApi;
    private final MutableLiveData<List<Card>> flashcardsLiveData;
    private final MutableLiveData<Boolean> loadingStateLiveData;
    private final MutableLiveData<String> errorMessageLiveData;
    private final MutableLiveData<Boolean> createCardSuccessLiveData;
    private final MutableLiveData<Card> createdCardLiveData;
    private final MutableLiveData<Card> flashcardDetailLiveData;
    private final MutableLiveData<Boolean> updateSuccessLiveData;
    private final MutableLiveData<CreateFlashcardResponse> createFlashcardSuccessLiveData;

    public FlashcardRepository() {
        flashcardApi = ApiClient.getClient().create(FlashcardApi.class);
        flashcardsLiveData = new MutableLiveData<>();
        loadingStateLiveData = new MutableLiveData<>();
        errorMessageLiveData = new MutableLiveData<>();
        createCardSuccessLiveData = new MutableLiveData<>();
        createdCardLiveData = new MutableLiveData<>();
        flashcardDetailLiveData = new MutableLiveData<>();
        updateSuccessLiveData = new MutableLiveData<>();
        createFlashcardSuccessLiveData = new MutableLiveData<>();
    }
    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccessLiveData;
    }
    public void setLoadingState(boolean isLoading) {
        loadingStateLiveData.setValue(isLoading);
    }

    public void setErrorMessage(String message) {
        errorMessageLiveData.setValue(message);
    }
    public LiveData<CreateFlashcardResponse> getCreateFlashcardSuccess() {
        return createFlashcardSuccessLiveData;
    }

    public LiveData<List<Card>> getFlashcards() {
        return flashcardsLiveData;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingStateLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<Boolean> getCreateCardSuccess() {
        return createCardSuccessLiveData;
    }

    public LiveData<Card> getCreatedCard() {
        return createdCardLiveData;
    }

    public LiveData<Card> getFlashcardDetail() {
        return flashcardDetailLiveData;
    }

    public void fetchFlashcardsByDeck(long deckId, String token) {
        loadingStateLiveData.setValue(true);

        String authHeader = "Bearer " + token;
        Call<List<FlashcardDto>> call = flashcardApi.getFlashcardsByDeck(deckId, authHeader);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<FlashcardDto>> call, @NonNull Response<List<FlashcardDto>> response) {
                loadingStateLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Card> cards = convertToCards(response.body());
                    flashcardsLiveData.setValue(cards);
                } else {
                    errorMessageLiveData.setValue("Không thể tải danh sách thẻ");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FlashcardDto>> call, @NonNull Throwable t) {
                loadingStateLiveData.setValue(false);
                errorMessageLiveData.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private List<Card> convertToCards(List<FlashcardDto> flashcardDtos) {
        List<Card> cards = new ArrayList<>();
        for (FlashcardDto dto : flashcardDtos) {
            Card card = convertToCard(dto);
            if (card != null) {
                cards.add(card);
            }
        }

        return cards;
    }
    private Card convertToCard(FlashcardDto dto) {
        if (dto.getContent() == null || dto.getCardType() == null) return null;

        Card card = new Card();
        card.setFlashcardId(dto.getFlashcardId());
        card.setDeckId(dto.getDeckId());
        card.setCardType(CardType.fromCode(dto.getCardType()));
        card.setContent(dto.getContent());

        return card;
    }
    // New method to convert CreateFlashcardResponse to Card
    private Card convertResponseToCard(CreateFlashcardResponse response) {
        if (response.getContent() == null || response.getCardType() == null) return null;

        Card card = new Card();
        card.setFlashcardId(response.getFlashcardId());
        card.setDeckId(response.getDeckId());
        card.setCardType(CardType.fromCode(response.getCardType()));

        // Convert content Object to JsonObject
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonObject contentJson = gson.toJsonTree(response.getContent()).getAsJsonObject();
        card.setContent(contentJson);

        return card;
    }

    public void clearErrorMessage() {
        errorMessageLiveData.setValue(null);
    }
    public void createFlashcard(Card card, String token) {

            loadingStateLiveData.setValue(true);

            CreateFlashcardRequest request = new CreateFlashcardRequest(
                    card.getDeckId(),
                    card.getCardType().getCode(),
                    card.getContent()
            );
            String authHeader = "Bearer " + token;

            Call<CreateFlashcardResponse> call = flashcardApi.createFlashcard(request, authHeader);

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<CreateFlashcardResponse> call, @NonNull Response<CreateFlashcardResponse> response) {
                    loadingStateLiveData.setValue(false);

                    if (response.isSuccessful() && response.body() != null) {
                        Card createdCard = convertResponseToCard(response.body());
                        createdCardLiveData.setValue(createdCard);
                        createCardSuccessLiveData.setValue(true);
                    } else {
                        // Log chi tiết lỗi
                        String errorBody = "";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e("FlashcardRepository", "Error reading error body", e);
                        }

                        createCardSuccessLiveData.setValue(false);
                        errorMessageLiveData.setValue("Lỗi API: " + response.code() + " - " + errorBody);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CreateFlashcardResponse> call, @NonNull Throwable t) {
                    loadingStateLiveData.setValue(false);
                    createCardSuccessLiveData.setValue(false);

                    errorMessageLiveData.setValue("Lỗi kết nối: " + t.getMessage());
                }
            });

    }

    public LiveData<Card> getCardById(int flashcardId) {
        MutableLiveData<Card> cardLiveData = new MutableLiveData<>();

        flashcardApi.getFlashcardById(flashcardId).enqueue(new Callback<CardResponse>() {
            @Override
            public void onResponse(Call<CardResponse> call, Response<CardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CardResponse cardResponse = response.body();
                    Card card = convertToCard(cardResponse);
                    cardLiveData.setValue(card);
                } else {
                    cardLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<CardResponse> call, Throwable t) {
                cardLiveData.setValue(null);
            }
        });

        return cardLiveData;
    }

    private Card convertToCard(CardResponse response) {
        Card card = new Card();
        card.setFlashcardId(response.getFlashcardId());
        card.setDeckId(response.getDeckId());

        // Convert card_type từ API thành CardType enum
        switch (response.getCardType()) {
            case "two_sided":
                card.setCardType(CardType.BASIC);
                break;
            case "multiple_choice":
                card.setCardType(CardType.MULTIPLE_CHOICE);
                break;
            case "fill_in_blank":
                card.setCardType(CardType.FILL_IN_BLANK);
                break;
        }

        card.setContent(response.getContent());
        return card;
    }

    public LiveData<UpdateFlashcardResponse> updateCard(int flashcardId, String cardType, JsonObject content) {
        MutableLiveData<UpdateFlashcardResponse> result = new MutableLiveData<>();

        // Tạo request DTO
        UpdateFlashcardRequest request = new UpdateFlashcardRequest(cardType, content);

        // Gọi API PUT
        flashcardApi.updateFlashcard(flashcardId, request).enqueue(new Callback<>() {

            @Override
            public void onResponse(Call<UpdateFlashcardResponse> call, Response<UpdateFlashcardResponse> response) {
                Log.d("UpdateCard", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("UpdateCard", "Update successful");
                    result.setValue(response.body());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<UpdateFlashcardResponse> call, Throwable t) {
                result.setValue(null);
            }
        });

        return result;
    }

    public void deleteFlashcard(int flashcardId, String authToken, Callback<DeleteFlashcardResponse> callback) {
        // Create the API call
        Call<DeleteFlashcardResponse> call = flashcardApi.deleteFlashcard(flashcardId, "Bearer " + authToken);

        // Enqueue the call with the provided callback
        call.enqueue(callback);
    }
}