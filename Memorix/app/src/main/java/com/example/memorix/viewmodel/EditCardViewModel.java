package com.example.memorix.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.data.remote.Repository.FlashcardRepository;
import com.example.memorix.model.Card;
import com.google.gson.JsonObject;

public class EditCardViewModel extends ViewModel {
    private final FlashcardRepository cardRepository;
    private final MutableLiveData<Card> cardLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> saveErrorMessage = new MutableLiveData<>();

    public EditCardViewModel(FlashcardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getSaveErrorMessage() {
        return saveErrorMessage;
    }

    public LiveData<Card> getCardLiveData() {
        return cardLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadCard(int flashcardId) {
        isLoading.setValue(true);
        cardRepository.getCardById(flashcardId).observeForever(card -> {
            isLoading.setValue(false);
            if (card != null) {
                cardLiveData.setValue(card);
            } else {
                errorMessage.setValue("Không thể tải thông tin thẻ");
            }
        });
    }

    public void updateCard(Card card) {
        isLoading.setValue(true);

        JsonObject content = new JsonObject();

        switch (card.getCardType()) {
            case BASIC:
                JsonObject basicContent = card.getContent();
                content.addProperty("front", basicContent.get("front").getAsString());
                content.addProperty("back", basicContent.get("back").getAsString());
                break;

            case MULTIPLE_CHOICE:
                JsonObject mcContent = card.getContent();
                content.addProperty("question", mcContent.get("question").getAsString());
                content.add("options", mcContent.getAsJsonArray("options"));
                content.addProperty("answer", mcContent.get("answer").getAsString());
                break;

            case FILL_IN_BLANK:
                JsonObject fibContent = card.getContent();
                content.addProperty("text", fibContent.get("front").getAsString());
                content.addProperty("answer", fibContent.get("back").getAsString());
                break;
        }


        // Sử dụng CardType.getCode() thay vì switch case
        String cardTypeString = card.getCardType().getCode();

        // Gọi API update
        cardRepository.updateCard(card.getFlashcardId(), cardTypeString, content)
                .observeForever(response -> {
                    isLoading.setValue(false);
                    if (response != null) {
                        saveSuccess.setValue(true);
                    } else {
                        saveErrorMessage.setValue("Không thể cập nhật thẻ");
                    }
                });
    }
}