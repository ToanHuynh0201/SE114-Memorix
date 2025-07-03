package com.example.memorix.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.data.remote.dto.Flashcard.DeleteFlashcardResponse;
import com.example.memorix.model.Card;
import com.example.memorix.model.Deck;
import com.example.memorix.data.remote.Repository.DeckRepository;
import com.example.memorix.data.remote.Repository.FlashcardRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeckManagementViewModel extends ViewModel {
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final MutableLiveData<Boolean> deleteResult = new MutableLiveData<>();

    public DeckManagementViewModel() {
        deckRepository = new DeckRepository();
        flashcardRepository = new FlashcardRepository();
    }

    // Deck related methods
    public LiveData<Deck> getDeckDetail() {
        return deckRepository.getDeckDetail();
    }

    public void loadDeckById(long deckId, String token) {
        deckRepository.fetchDeckById(deckId, token);
    }

    // Flashcard related methods
    public LiveData<List<Card>> getFlashcards() {
        return flashcardRepository.getFlashcards();
    }

    public void loadFlashcardsByDeck(long deckId, String token) {
        flashcardRepository.fetchFlashcardsByDeck(deckId, token);
    }

    // Loading and error state methods
    public LiveData<Boolean> getLoadingState() {
        return deckRepository.getLoadingState();
    }

    public LiveData<String> getErrorMessage() {
        return deckRepository.getErrorMessage();
    }

    public void clearErrorMessage() {
        deckRepository.clearErrorMessage();
        flashcardRepository.clearErrorMessage();
    }

    public LiveData<Boolean> getDeleteResult() {
        return deleteResult;
    }

    public void clearDeleteResult() {
        deleteResult.setValue(null);
    }

    public void deleteFlashcard(int flashcardId, String authToken) {
        // Set loading state through repository
        flashcardRepository.setLoadingState(true);

        // Call repository to delete flashcard
        flashcardRepository.deleteFlashcard(flashcardId, authToken, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeleteFlashcardResponse> call, @NonNull Response<DeleteFlashcardResponse> response) {
                // Set loading state through repository
                flashcardRepository.setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    DeleteFlashcardResponse responseBody = response.body();

                    // Check if the response indicates success
                    if (responseBody.isSuccess()) {
                        deleteResult.postValue(true);
                    } else {
                        flashcardRepository.setErrorMessage("Xóa flashcard thất bại");
                        deleteResult.postValue(false);
                    }
                } else {
                    // Handle HTTP error codes
                    String errorMsg = "Lỗi server: " + response.code();
                    if (response.code() == 404) {
                        errorMsg = "Không tìm thấy flashcard";
                    } else if (response.code() == 401) {

                        errorMsg = "Phiên đăng nhập đã hết hạn";
                    } else if (response.code() == 403) {
                        errorMsg = "Không có quyền xóa flashcard này";
                    }
                    flashcardRepository.setErrorMessage(errorMsg);
                    deleteResult.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteFlashcardResponse> call, @NonNull Throwable t) {
                flashcardRepository.setLoadingState(false);
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                flashcardRepository.setErrorMessage(errorMsg);
                deleteResult.postValue(false);
            }
        });
    }
}