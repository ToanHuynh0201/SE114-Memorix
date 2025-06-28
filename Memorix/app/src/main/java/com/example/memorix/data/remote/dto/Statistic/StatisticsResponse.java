package com.example.memorix.data.remote.dto.Statistic;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatisticsResponse {
    @SerializedName("last7Days")
    private PeriodStats last7Days;

    @SerializedName("last30Days")
    private PeriodStats last30Days;

    // Getters and setters
    public PeriodStats getLast7Days() {
        return last7Days;
    }

    public void setLast7Days(PeriodStats last7Days) {
        this.last7Days = last7Days;
    }

    public PeriodStats getLast30Days() {
        return last30Days;
    }

    public void setLast30Days(PeriodStats last30Days) {
        this.last30Days = last30Days;
    }

    public static class PeriodStats {
        @SerializedName("total")
        private int total;

        @SerializedName("average")
        private double average;

        @SerializedName("daily")
        private List<DailyStats> daily;

        // Getters and setters
        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public double getAverage() {
            return average;
        }

        public void setAverage(double average) {
            this.average = average;
        }

        public List<DailyStats> getDaily() {
            return daily;
        }

        public void setDaily(List<DailyStats> daily) {
            this.daily = daily;
        }
    }

    public static class DailyStats {
        @SerializedName("date")
        private String date;

        @SerializedName("count")
        private String count;

        // Getters and setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public int getCountAsInt() {
            try {
                return Integer.parseInt(count);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}