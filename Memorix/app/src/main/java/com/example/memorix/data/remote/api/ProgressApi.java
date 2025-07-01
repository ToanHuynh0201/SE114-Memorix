package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.Progress.ProgressDueResponse;
import com.example.memorix.data.remote.dto.Progress.ProgressRequest;
import com.example.memorix.data.remote.dto.Progress.ProgressResponse;
import com.example.memorix.data.remote.dto.Progress.ProgressUnlearnedLearnedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ProgressApi {
    @GET("/api/progress/due")
    Call<ProgressDueResponse> getDueFlashcards();

    @POST("/api/progress")
    Call<ProgressResponse> postProgress(@Body ProgressRequest request);

    @GET("/api/progress/unlearnedAndlearned")
    Call<ProgressUnlearnedLearnedResponse> getUnlearnedAndLearned();

    @GET("/api/progress/dueByDeck/{deckId}")
    Call<ProgressDueResponse> getDueByDeck(@retrofit2.http.Path("deckId") long deckId);

    @PUT("/api/progress")
    Call<ProgressResponse> updateProgress(@Body ProgressRequest request);



}
