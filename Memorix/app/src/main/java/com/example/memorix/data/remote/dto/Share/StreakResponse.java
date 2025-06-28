package com.example.memorix.data.remote.dto.Share;

import com.google.gson.annotations.SerializedName;

public class StreakResponse {
    @SerializedName("currentStreak")
    private int currentStreak;

    @SerializedName("maxStreak")
    private int maxStreak;

    public StreakResponse() {}

    public StreakResponse(int currentStreak, int maxStreak) {
        this.currentStreak = currentStreak;
        this.maxStreak = maxStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    @Override
    public String toString() {
        return "StreakResponse{" +
                "currentStreak=" + currentStreak +
                ", maxStreak=" + maxStreak +
                '}';
    }
}
