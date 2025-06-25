package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.memorix.model.Deck;
import com.example.memorix.data.remote.dto.Deck.DeckCreateResponse;
import com.example.memorix.data.remote.Repository.DeckRepository;
import com.example.memorix.data.remote.Repository.ShareRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final DeckRepository deckRepository;
    private final ShareRepository shareRepository;

    public HomeViewModel() {
        deckRepository = new DeckRepository();
        shareRepository = new ShareRepository();
    }

    // ========== DECK OPERATIONS ==========
    public LiveData<List<Deck>> getDecks() {
        return deckRepository.getDecks();
    }

    public LiveData<Boolean> getLoadingState() {
        return deckRepository.getLoadingState();
    }

    public LiveData<String> getErrorMessage() {
        return deckRepository.getErrorMessage();
    }

    public void loadDecks(String token) {
        deckRepository.fetchDecks(token);
    }

    public void searchDecks(String token, String searchQuery) {
        deckRepository.searchDecks(token, searchQuery);
    }

    // Create Deck
    public LiveData<DeckCreateResponse> getCreateDeckSuccess() {
        return deckRepository.getCreateDeckSuccess();
    }

    public void createDeck(String name, String description, String imageUrl, boolean isPublic, String token) {
        deckRepository.createDeck(name, description, imageUrl, isPublic, token);
    }

    public void createDeck(String name, String description, boolean isPublic, String token) {
        deckRepository.createDeck(name, description, isPublic, token);
    }

    // Update Deck
    public LiveData<Boolean> getUpdateSuccess() {
        return deckRepository.getUpdateSuccess();
    }

    public void updateDeck(long deckId, String name, String description, String imageUrl, boolean isPublic, String token) {
        deckRepository.updateDeck(deckId, name, description, imageUrl, isPublic, token);
    }

    public void updateDeck(long deckId, String name, String description, boolean isPublic, String token) {
        deckRepository.updateDeck(deckId, name, description, isPublic, token);
    }

    public void updateDeck(long deckId, Deck updatedDeck, String token) {
        deckRepository.updateDeck(deckId, updatedDeck, token);
    }

    // Delete Deck
    public LiveData<Boolean> getDeleteSuccess() {
        return deckRepository.getDeleteSuccess();
    }

    public void deleteDeck(long deckId, String token) {
        deckRepository.deleteDeck(deckId, token);
    }

    // ========== SHARE OPERATIONS ==========
    public LiveData<Boolean> getShareSuccess() {
        return shareRepository.getShareSuccess();
    }

    public LiveData<String> getShareError() {
        return shareRepository.getShareError();
    }

    public LiveData<Boolean> getShareLoading() {
        return shareRepository.getShareLoading();
    }

    /**
     * Share deck với user khác
     * @param deckId ID của deck cần share
     * @param receiverEmail Email của người nhận
     * @param permissionLevel Quyền hạn ("view", "edit", etc.)
     * @param token Auth token
     */
    public void shareDeck(long deckId, String receiverEmail, String permissionLevel, String token) {
        shareRepository.shareDeck(deckId, receiverEmail, permissionLevel, token);
    }

    /**
     * Reset share states - useful when starting a new share operation
     */
    public void resetShareStates() {
        shareRepository.resetShareStates();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up resources when ViewModel is destroyed
        shareRepository.clearStates();
    }
}