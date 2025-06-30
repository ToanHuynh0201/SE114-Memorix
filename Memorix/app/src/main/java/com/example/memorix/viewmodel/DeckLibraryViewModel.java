package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.data.remote.Repository.DeckLibraryRepository;
import com.example.memorix.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class DeckLibraryViewModel extends ViewModel {
    private final DeckLibraryRepository repository;
    private final MutableLiveData<List<Deck>> filteredDecksLiveData;
    private List<Deck> originalDecks;

    // Track current search state
    private String currentSearchQuery = "";
    private String currentCategory = "";

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

    // Clone functionality methods
    public LiveData<Boolean> getCloneLoading() {
        return repository.getCloneLoading();
    }

    public LiveData<String> getCloneSuccess() {
        return repository.getCloneSuccess();
    }

    public LiveData<String> getCloneError() {
        return repository.getCloneError();
    }

    // Load all public decks (initial load)
    public void loadPublicDecks() {
        repository.fetchPublicDecks();
    }

    // API-based search method
    public void searchDecks(String searchQuery, String category) {
        // Update current search state
        currentSearchQuery = searchQuery != null ? searchQuery.trim() : "";
        currentCategory = category != null ? category.trim() : "";

        // Use repository to search via API
        repository.searchPublicDecks(currentSearchQuery, currentCategory);
    }

    // Convenience methods for different search scenarios
    public void searchByQuery(String searchQuery) {
        searchDecks(searchQuery, currentCategory);
    }

    public void searchByCategory(String category) {
        searchDecks(currentSearchQuery, category);
    }

    public void clearSearch() {
        currentSearchQuery = "";
        currentCategory = "";
        repository.fetchPublicDecks(); // Load all decks
    }

    // Getters for current search state
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    public String getCurrentCategory() {
        return currentCategory;
    }

    // Keep these methods for compatibility with existing code
    public void setOriginalDecks(List<Deck> decks) {
        this.originalDecks = new ArrayList<>(decks);
        filteredDecksLiveData.setValue(decks);
    }

    // Legacy local filtering methods - kept for backward compatibility
    // but now they trigger API calls instead of local filtering
    public void filterDecksWithCategory(String searchQuery, String category) {
        searchDecks(searchQuery, category);
    }

    public void filterDecks(String query) {
        searchDecks(query, currentCategory);
    }

    public void filterByCategory(String category) {
        searchDecks(currentSearchQuery, category);
    }

    public void clearFilters() {
        clearSearch();
    }

    // Utility methods
    public int getOriginalDecksCount() {
        return originalDecks != null ? originalDecks.size() : 0;
    }

    public int getFilteredDecksCount() {
        List<Deck> current = filteredDecksLiveData.getValue();
        return current != null ? current.size() : 0;
    }

    // Clone methods
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