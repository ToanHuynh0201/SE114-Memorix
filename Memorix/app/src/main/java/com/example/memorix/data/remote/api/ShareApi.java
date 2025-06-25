package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.Share.IncomingSharesResponse;
import com.example.memorix.data.remote.dto.Share.ShareRequest;
import com.example.memorix.data.remote.dto.Share.ShareResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.GET;

public interface ShareApi {
    @POST("/api/shares")
    Call<ShareResponse> shareDeck(
            @Header("Authorization") String token,
            @Body ShareRequest request
    );
    @GET("/api/shares/incoming")
    Call<IncomingSharesResponse> getIncomingShares(
            @Header("Authorization") String authToken
    );
}
