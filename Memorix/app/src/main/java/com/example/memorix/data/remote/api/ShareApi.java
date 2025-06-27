package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.Share.AcceptShareResponse;
import com.example.memorix.data.remote.dto.Share.DeclineShareResponse;
import com.example.memorix.data.remote.dto.Share.IncomingSharesResponse;
import com.example.memorix.data.remote.dto.Share.ShareRequest;
import com.example.memorix.data.remote.dto.Share.ShareResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;

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

    @POST("/api/shares/{shareId}/accept")
    Call<AcceptShareResponse> acceptShare(
            @Header("Authorization") String authToken,
            @Path("shareId") long shareId
    );

    @POST("/api/shares/{shareId}/decline")
    Call<DeclineShareResponse> declineShare(
            @Header("Authorization") String authToken,
            @Path("shareId") long shareId
    );
}
