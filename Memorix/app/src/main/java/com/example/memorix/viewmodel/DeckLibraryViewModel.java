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

    public void loadPublicDecks() {
        repository.fetchPublicDecks();
    }

    public void setOriginalDecks(List<Deck> decks) {
        this.originalDecks = new ArrayList<>(decks);
        filteredDecksLiveData.setValue(decks);
    }

    // Updated filter method to support both search and category
    public void filterDecksWithCategory(String searchQuery, String category) {
        if (originalDecks == null || originalDecks.isEmpty()) {
            filteredDecksLiveData.setValue(new ArrayList<>());
            return;
        }

        List<Deck> filtered = new ArrayList<>();
        String query = searchQuery != null ? searchQuery.toLowerCase().trim() : "";

        for (Deck deck : originalDecks) {
            boolean matchesSearch = true;
            boolean matchesCategory = true;

            // Check search query
            if (!query.isEmpty()) {
                matchesSearch = deck.getName().toLowerCase().contains(query) ||
                        (deck.getDescription() != null &&
                                deck.getDescription().toLowerCase().contains(query));
            }

            // Check category
            if (category != null && !category.isEmpty()) {
                matchesCategory = category.equals(deck.getCategory());
            }

            // Add deck if it matches both criteria
            if (matchesSearch && matchesCategory) {
                filtered.add(deck);
            }
        }

        filteredDecksLiveData.setValue(filtered);
    }

    // Original filter method (search only) - maintained for backward compatibility
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

    // Method to filter by category only
    public void filterByCategory(String category) {
        if (originalDecks == null || originalDecks.isEmpty()) {
            filteredDecksLiveData.setValue(new ArrayList<>());
            return;
        }

        if (category == null || category.isEmpty()) {
            filteredDecksLiveData.setValue(originalDecks);
            return;
        }

        List<Deck> filtered = new ArrayList<>();

        for (Deck deck : originalDecks) {
            if (category.equals(deck.getCategory())) {
                filtered.add(deck);
            }
        }

        filteredDecksLiveData.setValue(filtered);
    }

    // Method to clear all filters
    public void clearFilters() {
        if (originalDecks != null) {
            filteredDecksLiveData.setValue(originalDecks);
        }
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