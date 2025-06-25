package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.Flashcard.CardResponse;
import com.example.memorix.data.remote.dto.Flashcard.CreateFlashcardRequest;
import com.example.memorix.data.remote.dto.Flashcard.CreateFlashcardResponse;
import com.example.memorix.data.remote.dto.Flashcard.DeleteFlashcardResponse;
import com.example.memorix.data.remote.dto.Flashcard.FlashcardDto;
import com.example.memorix.data.remote.dto.Flashcard.UpdateFlashcardRequest;
import com.example.memorix.data.remote.dto.Flashcard.UpdateFlashcardResponse;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FlashcardApi {
    @POST("/api/flashcards")
    Call<CreateFlashcardResponse> createFlashcard(
            @Body CreateFlashcardRequest request,
            @Header("Authorization") String authorization
    );
    @GET("/api/flashcards/deck/{deckId}")
    Call<List<FlashcardDto>> getFlashcardsByDeck(
            @Path("deckId") long deckId,
            @Header("Authorization") String token
    );

    @GET("/api/flashcards/{flashcardId}")
    Call<CardResponse> getFlashcardById(@Path("flashcardId") int flashcardId);


    @PUT("/api/flashcards/{flashcardId}")
    Call<UpdateFlashcardResponse> updateFlashcard(
            @Path("flashcardId") int flashcardId,
            @Body UpdateFlashcardRequest request
    );

    @DELETE("/api/flashcards/{flashcardId}")
    Call<DeleteFlashcardResponse> deleteFlashcard(
            @Path("flashcardId") int flashcardId,
            @Header("Authorization") String authorization
    );

}