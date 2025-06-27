package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.data.remote.Repository.DeckLibraryRepository;
import com.example.memorix.model.Deck;
import java.util.ArrayList;
import java.util.List;

public class DeckLibraryViewModel extends ViewModel {
    private DeckLibraryRepository repository;
    private MutableLiveData<List<Deck>> filteredDecksLiveData;
    private List<Deck> originalDecks;

    public DeckLibraryViewModel() {
        repository = new DeckLibraryRepository();
        filteredDecksLiveData = new MutableLiveData<>();
        originalDecks = new ArrayList<>();
    }

    // Existing methods
    public LiveData<List<Deck>> getPublicDecks() {
        return repository.getPublicDecks();
    }

    public LiveData<List<Deck>> getFilteredDecks() {
        return filteredDecksLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return repository.getLoading();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    // New methods for clone functionality
    public LiveData<Boolean> getCloneLoading() {
        return repository.getCloneLoading();
    }

    public LiveData<String> getCloneSuccess() {
        return repository.getCloneSuccess();
    }

    public LiveData<String> getCloneError() {
        return repository.getCloneError();
    }

    public void loadPublicDecks() {
        repository.fetchPublicDecks();
    }

    public void setOriginalDecks(List<Deck> decks) {
        this.originalDecks = new ArrayList<>(decks);
        filteredDecksLiveData.setValue(decks);
    }

    public void filterDecks(String query) {
        if (originalDecks == null) return;

        if (query == null || query.trim().isEmpty()) {
            filteredDecksLiveData.setValue(originalDecks);
        } else {
            List<Deck> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase().trim();

            for (Deck deck : originalDecks) {
                if (deck.getName().toLowerCase().contains(lowerCaseQuery) ||
                        (deck.getDescription() != null && deck.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(deck);
                }
            }

            filteredDecksLiveData.setValue(filteredList);
        }
    }

    public void cloneDeck(Deck deck, String token) {
        repository.cloneDeck(deck.getId(), token);
    }

    public void clearCloneMessages() {
        repository.clearCloneMessages();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        clearCloneMessages();
    }
}