package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.model.Deck;
import com.example.memorix.data.remote.dto.Deck.DeckCreateResponse;
import com.example.memorix.data.remote.Repository.DeckRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final DeckRepository repository;

    public HomeViewModel() {
        repository = new DeckRepository();
    }

    public LiveData<List<Deck>> getDecks() {
        return repository.getDecks();
    }

    public LiveData<Boolean> getLoadingState() {
        return repository.getLoadingState();
    }

    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }

    public void loadDecks(String token) {
        repository.fetchDecks(token);
    }

    public void searchDecks(String token, String searchQuery) {
        repository.searchDecks(token, searchQuery);
    }

    public LiveData<DeckCreateResponse> getCreateDeckSuccess() {
        return repository.getCreateDeckSuccess();
    }

    // Updated method with imageUrl parameter
    public void createDeck(String name, String description, String imageUrl, boolean isPublic, String token) {
        repository.createDeck(name, description, imageUrl, isPublic, token);
    }

    // Overloaded method for backward compatibility
    public void createDeck(String name, String description, boolean isPublic, String token) {
        repository.createDeck(name, description, isPublic, token);
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return repository.getUpdateSuccess();
    }

    public void updateDeck(long deckId, String name, String description, String imageUrl, boolean isPublic, String token) {
        repository.updateDeck(deckId, name, description, imageUrl, isPublic, token);
    }

    // Overloaded method for backward compatibility (without imageUrl)
    public void updateDeck(long deckId, String name, String description, boolean isPublic, String token) {
        repository.updateDeck(deckId, name, description, isPublic, token);
    }

    // Old method for compatibility with existing Deck object
    public void updateDeck(long deckId, Deck updatedDeck, String token) {
        repository.updateDeck(deckId, updatedDeck, token);
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return repository.getDeleteSuccess();
    }

    public void deleteDeck(long deckId, String token) {
        repository.deleteDeck(deckId, token);
    }
}