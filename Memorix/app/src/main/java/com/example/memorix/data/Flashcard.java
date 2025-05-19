package com.example.memorix.data;

public class Flashcard {
    private long id;                 // ID trong cơ sở dữ liệu
    private String front;            // Nội dung mặt trước
    private String back;             // Nội dung mặt sau
    private String difficulty;       // Độ khó: "easy", "medium", "hard"
    private long lastReviewTime;     // Thời gian ôn tập lần cuối (timestamp)
    private int reviewCount;         // Số lần đã ôn tập

    /**
     * Constructor đơn giản cho Flashcard
     * @param front Nội dung mặt trước
     * @param back Nội dung mặt sau
     */
    public Flashcard(String front, String back) {
        this.front = front;
        this.back = back;
        this.difficulty = "medium";  // Mặc định là trung bình
        this.lastReviewTime = System.currentTimeMillis();
        this.reviewCount = 0;
    }

    /**
     * Constructor đầy đủ cho Flashcard
     * @param id ID trong cơ sở dữ liệu
     * @param front Nội dung mặt trước
     * @param back Nội dung mặt sau
     * @param difficulty Độ khó
     * @param lastReviewTime Thời gian ôn tập lần cuối
     * @param reviewCount Số lần đã ôn tập
     */
    public Flashcard(long id, String front, String back, String difficulty, long lastReviewTime, int reviewCount) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.difficulty = difficulty;
        this.lastReviewTime = lastReviewTime;
        this.reviewCount = reviewCount;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public long getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(long lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    /**
     * Tăng số lần ôn tập lên 1 và cập nhật thời gian ôn tập
     */
    public void incrementReviewCount() {
        this.reviewCount++;
        this.lastReviewTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "id=" + id +
                ", front='" + front + '\'' +
                ", back='" + back + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", reviewCount=" + reviewCount +
                '}';
    }
}
