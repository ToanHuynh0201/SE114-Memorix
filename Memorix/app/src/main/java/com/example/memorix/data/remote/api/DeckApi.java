package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.Deck.CloneResponse;
import com.example.memorix.data.remote.dto.Deck.DeckDeleteResponse;
import com.example.memorix.data.remote.dto.Deck.DeckUpdateRequest;
import com.example.memorix.data.remote.dto.Deck.PublicDecksResponse;
import com.example.memorix.data.remote.dto.Deck.DeckCreateRequest;
import com.example.memorix.data.remote.dto.Deck.DeckCreateResponse;
import com.example.memorix.data.remote.dto.Deck.DeckResponse;
import com.example.memorix.model.Deck;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DeckApi {
    @GET("api/decks")
    Call<List<DeckResponse>> getDecks(@Header("Authorization") String token);

    @GET("api/decks")
    Call<List<DeckResponse>> getDecksWithSearch(
            @Header("Authorization") String token,
            @Query("q") String searchQuery
    );

    @GET("api/decks")
    Call<List<DeckResponse>> getDecksWithCategory(
            @Header("Authorization") String token,
            @Query("category") String category
    );

    @GET("api/decks")
    Call<List<DeckResponse>> getDecksWithSearchAndCategory(
            @Header("Authorization") String token,
            @Query("q") String searchQuery,
            @Query("category") String category
    );

    @GET("api/decks/{deckId}")
    Call<Deck> getDeckById(@Header("Authorization") String token, @Path("deckId") long deckId);

    @DELETE("api/decks/{id}")
    Call<DeckDeleteResponse> deleteDeck(
            @Header("Authorization") String token,
            @Path("id") long deckId
    );

    @PUT("api/decks/{id}")
    Call<DeckResponse> updateDeck(
            @Header("Authorization") String token,
            @Path("id") long deckId,
            @Body DeckUpdateRequest request
    );

    @POST("api/decks")
    Call<DeckCreateResponse> createDeck(@Header("Authorization") String token, @Body DeckCreateRequest request);

    @GET("api/shares/public-decks")
    Call<PublicDecksResponse> getPublicDecks();
    @GET("api/shares/public-decks")
    Call<PublicDecksResponse> getPublicDecksWithSearch(@Query("q") String searchQuery);

    @GET("api/shares/public-decks")
    Call<PublicDecksResponse> getPublicDecksWithCategory(@Query("category") String category);
    @GET("api/shares/public-decks")
    Call<PublicDecksResponse> getPublicDecksWithSearchAndCategory(
            @Query("q") String searchQuery,
            @Query("category") String category
    );
    @POST("api/shares/clone/{deckId}")
    Call<CloneResponse> cloneDeck(
            @Header("Authorization") String token,
            @Path("deckId") long deckId
    );
}