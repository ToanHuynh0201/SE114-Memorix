package com.example.memorix.ui.flashcardstudy;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.example.memorix.data.Card;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FlashcardBasicStudyActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private MaterialCardView flashcardView;
    private CardView cardFront, cardBack;
    private TextView tvCardFront, tvCardBack, tvProgress, tvSetTitle;
    private LinearLayout difficultyButtonsLayout;
    private MaterialButton btnHard, btnMedium, btnEasy, btnPrevious, btnNext;
    AnimatorSet frontAnimation, backAnimation;

    private boolean isShowingFront = true;

    // List of flashcards
    private List<Card> flashcardList;
    // Current position in the list
    private int currentPosition = 0;
    // Animations for slide transition
    private Animation slideOutLeft, slideInRight, slideOutRight, slideInLeft;

    private final String deckName = "Sample Flashcard Set"; // Có thể lấy từ Intent
    private final List<Card> studiedCardsList = new ArrayList<>();
    private long studyStartTime;
    private final Map<String, String> cardDifficultyMap = new HashMap<>(); // Lưu độ khó của từng thẻ
    private final Map<String, Boolean> cardCorrectMap = new HashMap<>(); // Lưu trạng thái đúng/sai của từng thẻ
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard_basic_study);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studyStartTime = System.currentTimeMillis();

        // Khởi tạo danh sách flashcard (có thể được thay thế bằng dữ liệu thực tế từ database)
        initFlashcardList();

        // Khởi tạo các view
        initViews();

        // Thiết lập animation flip
        setupCardFlipAnimation();

        // Thiết lập animation trượt
        setupSlideAnimations();

        // Set up click listeners
        setupClickListeners();

        // Hiển thị flashcard đầu tiên
        displayCurrentFlashcard();
    }

    private void initFlashcardList() {
        // Initialize the flashcard list (replace with actual data from database)
        flashcardList = new ArrayList<>();

        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "What is the capital of France?", "Paris"));
        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "What is the largest planet in our solar system?", "Jupiter"));
        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "Who wrote 'Romeo and Juliet'?", "William Shakespeare"));
        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "Who is the author of '1984'?", "George Orwell"));
    }

    private void initViews() {
        tvSetTitle = findViewById(R.id.tv_set_title);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        flashcardView = findViewById(R.id.flashcard_view);
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        tvCardFront = findViewById(R.id.tv_card_front);
        tvCardBack = findViewById(R.id.tv_card_back);
        difficultyButtonsLayout = findViewById(R.id.difficulty_buttons_layout);
        btnHard = findViewById(R.id.btn_hard);
        btnMedium = findViewById(R.id.btn_medium);
        btnEasy = findViewById(R.id.btn_easy);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);

        // Set progress bar max value
        progressBar.setMax(flashcardList.size());

        // Set deck name
        tvSetTitle.setText(deckName);

        setupCard();
    }

    private void setupCard(){
        cardBack.clearAnimation();
        cardFront.clearAnimation();

        cardFront.setVisibility(View.VISIBLE);
        cardFront.setAlpha(1.0f);
        cardFront.setRotationY(0f);

        cardBack.setVisibility(View.GONE);
        cardBack.setAlpha(0.0f);
        cardBack.setRotationY(0f);

        isShowingFront = true;

        // Đảm bảo difficulty buttons bị ẩn
        difficultyButtonsLayout.setVisibility(View.GONE);
    }

    @SuppressLint("ResourceType")
    private void setupCardFlipAnimation() {
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        frontAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_out);
        backAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_in);
    }

    private void setupSlideAnimations() {
        // Load animations từ resource
        slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);

        // Thiết lập listeners cho animations
        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Chuyển đến thẻ tiếp theo
                currentPosition++;
                if (currentPosition >= flashcardList.size()) {
                    currentPosition = flashcardList.size() - 1;
                    return;
                }

                // Reset thẻ về trạng thái ban đầu trước khi hiển thị
                setupCard();
                displayCurrentFlashcard();

                // Bắt đầu animation slide in
                flashcardView.startAnimation(slideInRight);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Chuyển đến thẻ trước đó
                currentPosition--;
                if (currentPosition < 0) {
                    currentPosition = 0;
                    return;
                }

                // Reset thẻ về trạng thái ban đầu trước khi hiển thị
                setupCard();
                displayCurrentFlashcard();

                // Bắt đầu animation slide in
                flashcardView.startAnimation(slideInLeft);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayCurrentFlashcard() {
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Card currentCard = flashcardList.get(currentPosition);
            tvCardFront.setText(currentCard.getQuestion());
            tvCardBack.setText(currentCard.getAnswer());

            // Cập nhật thanh tiến độ và số thẻ
            updateProgressBar();
        }

        // Kiểm tra và vô hiệu hóa các nút nếu ở đầu hoặc cuối danh sách
        checkButtonStatus();
    }

    @SuppressLint("SetTextI18n")
    private void updateProgressBar() {
        progressBar.setProgress(currentPosition + 1);
        tvProgress.setText((currentPosition + 1) + "/" + flashcardList.size());
    }

    private void checkButtonStatus() {
        // Vô hiệu hóa nút Previous nếu ở thẻ đầu tiên
        btnPrevious.setEnabled(currentPosition > 0);
        btnPrevious.setAlpha(currentPosition > 0 ? 1.0f : 0.5f);

        // Vô hiệu hóa nút Next nếu ở thẻ cuối cùng
        btnNext.setEnabled(currentPosition < flashcardList.size() - 1);
        btnNext.setAlpha(currentPosition < flashcardList.size() - 1 ? 1.0f : 0.5f);
    }

    private void flipCard() {
        if (isShowingFront) {
            // Lật từ mặt trước sang mặt sau
            frontAnimation.setTarget(cardFront);
            backAnimation.setTarget(cardBack);

            // Hiển thị cardBack trước khi bắt đầu animation
            cardBack.setVisibility(View.VISIBLE);

            frontAnimation.start();
            backAnimation.start();

            // Đặt Handler để ẩn cardFront sau khi animation hoàn thành
            cardFront.postDelayed(() -> cardFront.setVisibility(View.GONE), 300);

            // Hiển thị các nút đánh giá độ khó khi lật sang mặt sau
            difficultyButtonsLayout.setVisibility(View.VISIBLE);
        } else {
            // Lật từ mặt sau sang mặt trước
            @SuppressLint("ResourceType") AnimatorSet frontAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_in);
            @SuppressLint("ResourceType") AnimatorSet backAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_out);

            frontAnimator.setTarget(cardFront);
            backAnimator.setTarget(cardBack);

            // Hiển thị cardFront trước khi bắt đầu animation
            cardFront.setVisibility(View.VISIBLE);

            backAnimator.start();
            frontAnimator.start();

            // Đặt Handler để ẩn cardBack sau khi animation hoàn thành
            cardBack.postDelayed(() -> cardBack.setVisibility(View.GONE), 300);

            // Ẩn các nút đánh giá độ khó khi lật về mặt trước
            difficultyButtonsLayout.setVisibility(View.GONE);
        }

        isShowingFront = !isShowingFront;
    }

    private void setupClickListeners() {
        // Click vào flashcard để lật
        flashcardView.setOnClickListener(v -> flipCard());

        // Xử lý các nút điều hướng
        btnNext.setOnClickListener(v -> {
            // Chuyển sang thẻ tiếp theo với animation trượt
            if (currentPosition < flashcardList.size() - 1) {
                moveToNextCard();
            } else {
                Toast.makeText(this, "Đã đến thẻ cuối cùng", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            // Quay lại thẻ trước với animation trượt
            if (currentPosition > 0) {
                moveToPreviousCard();
            } else {
                Toast.makeText(this, "Đây là thẻ đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý các nút đánh giá mức độ khó
        btnEasy.setOnClickListener(v -> {
            // Xử lý khi người dùng đánh giá "Dễ"
            markCardAsDifficulty("easy", true);

            // Kiểm tra xem có phải thẻ cuối cùng không
            if (currentPosition >= flashcardList.size() - 1) {
                // Thẻ cuối cùng - kết thúc session
                finishStudySession();
            } else {
                // Chưa phải thẻ cuối - chuyển tiếp
                moveToNextCard();
            }
        });

        btnMedium.setOnClickListener(v -> {
            // Xử lý khi người dùng đánh giá "Vừa"
            markCardAsDifficulty("medium", true);

            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession();
            } else {
                moveToNextCard();
            }
        });

        btnHard.setOnClickListener(v -> {
            // Xử lý khi người dùng đánh giá "Khó"
            markCardAsDifficulty("hard", false);

            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession();
            } else {
                moveToNextCard();
            }
        });
    }

    private void markCardAsDifficulty(String difficulty, boolean isCorrect) {
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Card currentCard = flashcardList.get(currentPosition);

            // Lưu độ khó và trạng thái đúng/sai
            cardDifficultyMap.put(currentCard.getId(), difficulty);
            cardCorrectMap.put(currentCard.getId(), isCorrect);

            // Thêm thẻ vào danh sách đã học nếu chưa có
            if (!studiedCardsList.contains(currentCard)) {
                studiedCardsList.add(currentCard);
            }

            // Cập nhật thông tin thẻ (nếu Card class có các phương thức này)
            // currentCard.incrementReviewCount();
            // if (isCorrect) {
            //     currentCard.incrementCorrectCount();
            // }

            // Lưu đánh giá vào cơ sở dữ liệu
        }
    }

    private void moveToNextCard() {
        // Kiểm tra xem có thể di chuyển đến thẻ tiếp theo không
        if (currentPosition < flashcardList.size() - 1) {
            // Ẩn nút đánh giá độ khó ngay lập tức nếu đang hiển thị
            if (difficultyButtonsLayout != null) {
                difficultyButtonsLayout.setVisibility(View.GONE);
            }

            // Đảm bảo thẻ hiển thị mặt trước trước khi chuyển sang thẻ mới
            if (!isShowingFront) {
                // Đặt lại thẻ về mặt trước mà không cần animation
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                isShowingFront = true;
            }

            // Bắt đầu animation trượt ra ngoài sang trái
            flashcardView.startAnimation(slideOutLeft);
        }
    }

    private void moveToPreviousCard() {
        // Kiểm tra xem có thể di chuyển đến thẻ trước đó không
        if (currentPosition > 0) {
            // Ẩn nút đánh giá độ khó ngay lập tức nếu đang hiển thị
            if (difficultyButtonsLayout != null) {
                difficultyButtonsLayout.setVisibility(View.GONE);
            }

            // Đảm bảo thẻ hiển thị mặt trước trước khi chuyển sang thẻ mới
            if (!isShowingFront) {
                // Đặt lại thẻ về mặt trước mà không cần animation
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                isShowingFront = true;
            }

            // Bắt đầu animation trượt ra ngoài sang phải
            flashcardView.startAnimation(slideOutRight);
        }
    }

    private void finishStudySession() {
        try {
            // Tính toán các thống kê cần thiết
            long studyEndTime = System.currentTimeMillis();
            int totalCorrectAnswers = 0;
            int totalReviewedCards = studiedCardsList.size();

            // Đếm số câu trả lời đúng
            for (Card card : studiedCardsList) {
                Boolean isCorrect = cardCorrectMap.get(card.getId());
                if (isCorrect != null && isCorrect) {
                    totalCorrectAnswers++;
                }
            }

//             Kiểm tra StudyStatisticsHelper có tồn tại không
//             StudyStatisticsHelper.StudySessionStats stats =
//                     StudyStatisticsHelper.calculateStats(studiedCardsList, studyStartTime, studyEndTime);

            // Navigate to StudySummaryActivity với proper error handling
            Intent intent = new Intent(this, StudySummaryActivity.class);
            intent.putExtra("deck_name", deckName);
            intent.putExtra("studied_cards", new ArrayList<>(studiedCardsList)); // Đảm bảo Serializable
            intent.putExtra("study_start_time", studyStartTime);
            intent.putExtra("study_end_time", studyEndTime);
            intent.putExtra("total_correct", totalCorrectAnswers);
            intent.putExtra("total_reviewed", totalReviewedCards);

            startActivity(intent);
            finish(); // Optional: finish current activity

        } catch (Exception e) {
            Log.e("error", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(this, "Lỗi khi chuyển đến trang tóm tắt: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}