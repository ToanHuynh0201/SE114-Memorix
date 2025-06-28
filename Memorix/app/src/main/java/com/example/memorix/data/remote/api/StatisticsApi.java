package com.example.memorix.data.remote.api;

import com.example.memorix.data.remote.dto.Share.StreakResponse;
import com.example.memorix.data.remote.dto.Statistic.StatisticsResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StatisticsApi {
    @GET("api/progress/stats")
    Call<StatisticsResponse> getStatistics();
    @GET("api/progress/streak")
    Call<StreakResponse> getStreak();
}
