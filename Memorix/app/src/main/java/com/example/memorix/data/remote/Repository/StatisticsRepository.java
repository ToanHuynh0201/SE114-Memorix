package com.example.memorix.data.remote.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorix.data.remote.api.StatisticsApi;
import com.example.memorix.data.remote.dto.Share.StreakResponse;
import com.example.memorix.data.remote.dto.Statistic.StatisticsResponse;
import com.example.memorix.data.remote.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsRepository {
    private final StatisticsApi statisticsApi;
    private final MutableLiveData<StatisticsResponse> statisticsLiveData;
    private final MutableLiveData<StreakResponse> streakLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;

    public StatisticsRepository() {
        statisticsApi = ApiClient.getClient().create(StatisticsApi.class);
        statisticsLiveData = new MutableLiveData<>();
        streakLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
    }

    public LiveData<StatisticsResponse> getStatisticsLiveData() {
        return statisticsLiveData;
    }

    public LiveData<StreakResponse> getStreakLiveData() {
        return streakLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void fetchStatistics() {
        try {
            loadingLiveData.setValue(true);

            Call<StatisticsResponse> call = statisticsApi.getStatistics();
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<StatisticsResponse> call, @NonNull Response<StatisticsResponse> response) {
                    try {
                        loadingLiveData.setValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            statisticsLiveData.setValue(response.body());
                            errorLiveData.setValue(null);
                        } else {
                            errorLiveData.setValue("Không thể tải dữ liệu thống kê");
                        }
                    } catch (Exception e) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Lỗi xử lý dữ liệu: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StatisticsResponse> call, @NonNull Throwable t) {
                    try {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Lỗi kết nối: " + t.getMessage());
                    } catch (Exception e) {
                        Log.e("Error", e.toString());
                    }
                }
            });
        } catch (Exception e) {
            loadingLiveData.setValue(false);
            errorLiveData.setValue("Lỗi khởi tạo API: " + e.getMessage());
        }
    }

    public void fetchStreak() {
        try {
            loadingLiveData.setValue(true);

            Call<StreakResponse> call = statisticsApi.getStreak();
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<StreakResponse> call, @NonNull Response<StreakResponse> response) {
                    try {
                        loadingLiveData.setValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            streakLiveData.setValue(response.body());
                            errorLiveData.setValue(null);
                        } else {
                            errorLiveData.setValue("Không thể tải dữ liệu streak");
                        }
                    } catch (Exception e) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Lỗi xử lý dữ liệu streak: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StreakResponse> call, @NonNull Throwable t) {
                    try {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Lỗi kết nối streak: " + t.getMessage());
                    } catch (Exception e) {
                        Log.e("Error", e.toString());
                    }
                }
            });
        } catch (Exception e) {
            loadingLiveData.setValue(false);
            errorLiveData.setValue("Lỗi khởi tạo API streak: " + e.getMessage());
        }
    }

    public void fetchAllData() {
        fetchStatistics();
        fetchStreak();
    }
}
