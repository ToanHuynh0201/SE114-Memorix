package com.example.memorix.data.remote.dto.Deck;

import com.example.memorix.model.Deck;

public class DeckResponse {
    private int deck_id;
    private int user_id;
    private String name;
    private String description;
    private String image_url;
    private boolean is_public;
    private int total_cards;
    private int unlearned_cards;
    private int learned_cards;
    private int due_cards;
    private String category;

    public DeckResponse() {
    }

    public DeckResponse(int deck_id, int user_id, String name, String description, String image_url,
                        boolean is_public, int total_cards, int unlearned_cards, int learned_cards,
                        int due_cards, String category) {
        this.deck_id = deck_id;
        this.user_id = user_id;
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.is_public = is_public;
        this.total_cards = total_cards;
        this.unlearned_cards = unlearned_cards;
        this.learned_cards = learned_cards;
        this.due_cards = due_cards;
        this.category = category;
    }

    // Getters
    public int getDeck_id() {
        return deck_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }

    public boolean isIs_public() {
        return is_public;
    }

    public int getTotal_cards() {
        return total_cards;
    }

    public int getUnlearned_cards() {
        return unlearned_cards;
    }

    public int getLearned_cards() {
        return learned_cards;
    }

    public int getDue_cards() {
        return due_cards;
    }

    public String getCategory() {
        return category;
    }

    // Setters
    public void setDeck_id(int deck_id) {
        this.deck_id = deck_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }

    public void setTotal_cards(int total_cards) {
        this.total_cards = total_cards;
    }

    public void setUnlearned_cards(int unlearned_cards) {
        this.unlearned_cards = unlearned_cards;
    }

    public void setLearned_cards(int learned_cards) {
        this.learned_cards = learned_cards;
    }

    public void setDue_cards(int due_cards) {
        this.due_cards = due_cards;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Utility method to convert to Deck object
    public Deck toDeck() {
        return new Deck(
                this.deck_id,
                this.name,
                this.description != null ? this.description : "",
                this.total_cards,
                this.unlearned_cards,
                this.learned_cards,
                this.due_cards,
                this.image_url,
                this.is_public,
                this.category != null ? this.category : ""
        );
    }
}