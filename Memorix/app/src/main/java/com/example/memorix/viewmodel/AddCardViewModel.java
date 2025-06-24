package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.data.remote.Repository.FlashcardRepository;
import com.example.memorix.model.Card;

public class AddCardViewModel extends ViewModel {
    private final FlashcardRepository flashcardRepository;

    public AddCardViewModel() {
        flashcardRepository = new FlashcardRepository();
    }

    public void createFlashcard(Card card, String token) {
        flashcardRepository.createFlashcard(card, token);
    }

    public LiveData<Boolean> getCreateCardSuccess() {
        return flashcardRepository.getCreateCardSuccess();
    }

    public LiveData<Boolean> getLoadingState() {
        return flashcardRepository.getLoadingState();
    }

    public LiveData<String> getErrorMessage() {
        return flashcardRepository.getErrorMessage();
    }

    public void clearErrorMessage() {
        flashcardRepository.clearErrorMessage();
    }
}
