package com.example.memorix.data.remote.dto.Progress;

public class ProgressResponse {
    public ProgressData progress;

    public static class ProgressData {
        public int user_id;
        public int flashcard_id;
        public String user_rating;
    }
}
