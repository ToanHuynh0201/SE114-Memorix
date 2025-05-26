package com.example.memorix.helper;
import com.example.memorix.data.Card;
import com.example.memorix.data.CardType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class StudyStatisticsHelper {
    /**
     * Class để lưu trữ thống kê của từng loại thẻ
     */
    public static class CardTypeStats {
        public int totalCards;
        public int totalCorrect;
        public int totalReviewed;
        public double accuracyRate;

        public CardTypeStats() {
            this.totalCards = 0;
            this.totalCorrect = 0;
            this.totalReviewed = 0;
            this.accuracyRate = 0.0;
        }

        public void calculateAccuracy() {
            this.accuracyRate = totalReviewed > 0 ? (double) totalCorrect / totalReviewed * 100 : 0;
        }
    }

    /**
     * Class để lưu trữ tổng thể thống kê học tập
     */
    public static class StudySessionStats {
        public int totalCards;
        public int totalCorrect;
        public int totalReviewed;
        public double overallAccuracy;
        public long studyDuration;
        public double averageTimePerCard;
        public Map<CardType, CardTypeStats> cardTypeStats;

        public StudySessionStats() {
            this.cardTypeStats = new HashMap<>();
            this.cardTypeStats.put(CardType.BASIC, new CardTypeStats());
            this.cardTypeStats.put(CardType.MULTIPLE_CHOICE, new CardTypeStats());
            this.cardTypeStats.put(CardType.FILL_IN_BLANK, new CardTypeStats());
        }
    }

    /**
     * Tính toán thống kê từ danh sách thẻ đã học
     */
    public static StudySessionStats calculateStats(List<Card> studiedCards,
                                                   long studyStartTime,
                                                   long studyEndTime) {
        StudySessionStats stats = new StudySessionStats();

        if (studiedCards == null || studiedCards.isEmpty()) {
            return stats;
        }

        // Tính toán thống kê tổng thể
        stats.totalCards = studiedCards.size();
        stats.studyDuration = studyEndTime - studyStartTime;

        // Tính toán thống kê cho từng loại thẻ
        for (Card card : studiedCards) {
            CardType type = card.getType();
            CardTypeStats typeStats = stats.cardTypeStats.get(type);

            if (typeStats != null) {
                typeStats.totalCards++;
                typeStats.totalCorrect += card.getCorrectCount();
                typeStats.totalReviewed += card.getReviewCount();

                // Cập nhật tổng thể
                stats.totalCorrect += card.getCorrectCount();
                stats.totalReviewed += card.getReviewCount();
            }
        }

        // Tính toán tỷ lệ chính xác
        stats.overallAccuracy = stats.totalReviewed > 0 ?
                (double) stats.totalCorrect / stats.totalReviewed * 100 : 0;

        for (CardTypeStats typeStats : stats.cardTypeStats.values()) {
            typeStats.calculateAccuracy();
        }

        // Tính thời gian trung bình mỗi thẻ
        stats.averageTimePerCard = stats.totalReviewed > 0 ?
                (double) stats.studyDuration / 1000 / stats.totalReviewed : 0;

        return stats;
    }

    /**
     * Định dạng thời gian học thành chuỗi dễ đọc
     */
    public static String formatStudyTime(long durationMs) {
        long minutes = durationMs / 60000;
        long seconds = (durationMs % 60000) / 1000;

        if (minutes > 0) {
            return String.format("%d phút %d giây", minutes, seconds);
        } else {
            return String.format("%d giây", seconds);
        }
    }

    /**
     * Lấy mô tả hiệu suất dựa trên tỷ lệ chính xác
     */
    public static String getPerformanceDescription(double accuracy) {
        if (accuracy >= 90) {
            return "Xuất sắc! 🌟";
        } else if (accuracy >= 80) {
            return "Tốt! 👍";
        } else if (accuracy >= 70) {
            return "Khá tốt! 👌";
        } else if (accuracy >= 60) {
            return "Cần cải thiện 📚";
        } else {
            return "Hãy luyện tập thêm! 💪";
        }
    }

    /**
     * Tính điểm học tập dựa trên hiệu suất
     */
    public static int calculateStudyScore(StudySessionStats stats) {
        double accuracyWeight = 0.6;  // 60% dựa trên độ chính xác
        double completionWeight = 0.3; // 30% dựa trên tỷ lệ hoàn thành
        double speedWeight = 0.1;      // 10% dựa trên tốc độ

        // Điểm độ chính xác (0-100)
        double accuracyScore = stats.overallAccuracy;

        // Điểm hoàn thành (0-100)
        double completionScore = stats.totalReviewed > 0 ?
                Math.min(100, (double) stats.totalReviewed / stats.totalCards * 100) : 0;

        // Điểm tốc độ (càng nhanh càng cao, tối đa 100)
        double speedScore = 100;
        if (stats.averageTimePerCard > 0) {
            // Giả sử thời gian lý tưởng là 10 giây/thẻ
            double idealTime = 10.0;
            speedScore = Math.max(0, Math.min(100,
                    (idealTime / stats.averageTimePerCard) * 100));
        }

        return (int) (accuracyScore * accuracyWeight +
                completionScore * completionWeight +
                speedScore * speedWeight);
    }

    /**
     * Tạo thông điệp khuyến khích dựa trên kết quả học tập
     */
    public static String getEncouragementMessage(StudySessionStats stats) {
        double accuracy = stats.overallAccuracy;

        if (accuracy >= 90) {
            return "Bạn đã thành thạo chủ đề này rồi! Hãy thử thách bản thân với những chủ đề khó hơn.";
        } else if (accuracy >= 80) {
            return "Kết quả tốt! Hãy tiếp tục luyện tập để đạt mức độ thành thạo.";
        } else if (accuracy >= 70) {
            return "Bạn đang tiến bộ! Hãy xem lại những thẻ sai để cải thiện hơn.";
        } else if (accuracy >= 60) {
            return "Đừng nản lòng! Hãy tập trung vào những thẻ khó và luyện tập thêm.";
        } else {
            return "Mọi chuyện đều bắt đầu từ những bước đi đầu tiên. Hãy kiên trì luyện tập!";
        }
    }
}
