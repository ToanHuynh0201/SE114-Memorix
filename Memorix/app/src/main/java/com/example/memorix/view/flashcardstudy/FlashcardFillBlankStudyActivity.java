package com.example.memorix.view.flashcardstudy;

import android.os.Bundle;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
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
import com.example.memorix.model.Card;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlashcardFillBlankStudyActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private MaterialCardView flashcardView;
    private CardView cardFront, cardBack;
    private TextView tvCardFront, tvCardBack, tvProgress, tvSetTitle;
    private TextView tvResultStatus, tvUserAnswer, tvExplanation;
    private TextInputLayout inputLayout;
    private TextInputEditText etAnswerInput;
    private LinearLayout difficultyButtonsLayout;
    private MaterialButton btnHard, btnMedium, btnEasy, btnPrevious, btnNext;
    AnimatorSet frontAnimation, backAnimation;

    private boolean isShowingFront = true;
    private String userAnswer = "";

    // List of flashcards
    private List<Card> flashcardList;
    // Current position in the list
    private int currentPosition = 0;
    // Animations for slide transition
    private Animation slideOutLeft, slideInRight, slideOutRight, slideInLeft;

    private final String deckName = "Tiếng Anh - Ngữ pháp cơ bản"; // Có thể lấy từ Intent
    private final List<Card> studiedCardsList = new ArrayList<>();
    private long studyStartTime;
    private final Map<String, String> cardDifficultyMap = new HashMap<>(); // Lưu độ khó của từng thẻ
    private final Map<String, Boolean> cardCorrectMap = new HashMap<>(); // Lưu trạng thái đúng/sai của từng thẻ
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard_fill_blank_study);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studyStartTime = System.currentTimeMillis();

        // Khởi tạo danh sách flashcard
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
        // Initialize the flashcard list with fill-in-the-blank questions
        flashcardList = new ArrayList<>();

        // Sử dụng format: question|answer|explanation (explanation có thể null)
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1",
//                "She _____ to school every day.", "goes", CardType.FILL_IN_BLANK));
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1",
//                "They _____ playing football now.", "are", CardType.FILL_IN_BLANK));
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1",
//                "I _____ finished my homework yesterday.", "have", CardType.FILL_IN_BLANK));
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1",
//                "He _____ not like vegetables.", "does", CardType.FILL_IN_BLANK));
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1",
//                "We _____ watching TV when she called.", "were", CardType.FILL_IN_BLANK));
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
        tvResultStatus = findViewById(R.id.tv_result_status);
        tvUserAnswer = findViewById(R.id.tv_user_answer);
        tvExplanation = findViewById(R.id.tv_explanation);
        inputLayout = findViewById(R.id.input_layout);
        etAnswerInput = findViewById(R.id.et_answer_input);
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
        setupInputField();
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

        // Clear input field
        etAnswerInput.setText("");
        userAnswer = "";
    }

    private void setupInputField() {
        // Xử lý khi người dùng nhấn Done trên bàn phím
        etAnswerInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswerAndFlipCard();
                return true;
            }
            return false;
        });
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

        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                currentPosition++;
                if (currentPosition >= flashcardList.size()) {
                    currentPosition = flashcardList.size() - 1;
                    return;
                }

                setupCard();
                displayCurrentFlashcard();
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
                currentPosition--;
                if (currentPosition < 0) {
                    currentPosition = 0;
                    return;
                }

                setupCard();
                displayCurrentFlashcard();
                flashcardView.startAnimation(slideInLeft);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayCurrentFlashcard() {
//        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
//            Card currentCard = flashcardList.get(currentPosition);
//            tvCardFront.setText(currentCard.getQuestion());
//
//            // Hiển thị câu trả lời hoàn chỉnh trên mặt sau
//            String completeAnswer = currentCard.getQuestion().replace("_____", currentCard.getCorrectAnswer());
//            tvCardBack.setText(completeAnswer);
//
//            updateProgressBar();
//        }

        checkButtonStatus();
    }

    @SuppressLint("SetTextI18n")
    private void updateProgressBar() {
        progressBar.setProgress(currentPosition + 1);
        tvProgress.setText((currentPosition + 1) + "/" + flashcardList.size());
    }

    private void checkButtonStatus() {
        btnPrevious.setEnabled(currentPosition > 0);
        btnPrevious.setAlpha(currentPosition > 0 ? 1.0f : 0.5f);

        btnNext.setEnabled(currentPosition < flashcardList.size() - 1);
        btnNext.setAlpha(currentPosition < flashcardList.size() - 1 ? 1.0f : 0.5f);
    }

    private void checkAnswerAndFlipCard() {
        userAnswer = Objects.requireNonNull(etAnswerInput.getText()).toString().trim();

        if (TextUtils.isEmpty(userAnswer)) {
            Toast.makeText(this, "Vui lòng nhập đáp án", Toast.LENGTH_SHORT).show();
            return;
        }

        flipCard();
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
            displayAnswerResult();
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

    @SuppressLint("SetTextI18n")
    private void displayAnswerResult() {
//        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
//            Card currentCard = flashcardList.get(currentPosition);
//            String correctAnswer = currentCard.getCorrectAnswer().toLowerCase().trim();
//            String userAnswerLower = userAnswer.toLowerCase().trim();
//
//            boolean isCorrect = correctAnswer.equals(userAnswerLower);
//
//            // Hiển thị trạng thái đúng/sai
//            if (isCorrect) {
//                tvResultStatus.setText(getString(R.string.correct));
//                tvResultStatus.setTextColor(getResources().getColor(R.color.secondary_color, null));
//            } else {
//                tvResultStatus.setText(getString(R.string.incorrect));
//                tvResultStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
//            }
//
//            // Hiển thị câu trả lời của người dùng
//            tvUserAnswer.setText(getString(R.string.your_answer_is) + " " + userAnswer);
////
////            // Hiển thị giải thích nếu có
////            String explanation = currentCard.getExplanation();
////            if (!TextUtils.isEmpty(explanation)) {
////                tvExplanation.setText("Giải thích: " + explanation);
////                tvExplanation.setVisibility(View.VISIBLE);
////            } else {
////                tvExplanation.setVisibility(View.GONE);
////            }
//            String completeAnswer = currentCard.getQuestion().replace("_____", currentCard.getCorrectAnswer());
//            SpannableString spannableString = new SpannableString(completeAnswer);
//
//            // Tìm vị trí của đáp án đúng trong câu hoàn chỉnh
//            int startIndex = completeAnswer.indexOf(currentCard.getCorrectAnswer());
//            int endIndex = startIndex + currentCard.getCorrectAnswer().length();
//
//            if (startIndex != -1) {
//                // Chọn màu dựa trên kết quả đúng/sai
//                int color = isCorrect ?
//                        getResources().getColor(R.color.secondary_color, null) :
//                        getResources().getColor(android.R.color.holo_red_dark, null);
//
//                // Áp dụng màu cho phần đáp án
//                spannableString.setSpan(
//                        new android.text.style.ForegroundColorSpan(color),
//                        startIndex,
//                        endIndex,
//                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                );
//
//                // Có thể thêm bold để làm nổi bật hơn
//                spannableString.setSpan(
//                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
//                        startIndex,
//                        endIndex,
//                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                );
//            }
//        }
    }

    private void setupClickListeners() {
        // Click vào flashcard để kiểm tra đáp án (chỉ khi ở mặt trước)
        flashcardView.setOnClickListener(v -> {
            if (isShowingFront) {
                checkAnswerAndFlipCard();
            } else {
                flipCard();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPosition < flashcardList.size() - 1) {
                moveToNextCard();
            } else {
                Toast.makeText(this, "Đã đến thẻ cuối cùng", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPosition > 0) {
                moveToPreviousCard();
            } else {
                Toast.makeText(this, "Đây là thẻ đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý các nút đánh giá mức độ khó
        btnEasy.setOnClickListener(v -> {
            boolean isCorrect = checkIfAnswerCorrect();
            markCardAsDifficulty("easy", isCorrect);

            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession();
            } else {
                moveToNextCard();
            }
        });

        btnMedium.setOnClickListener(v -> {
            boolean isCorrect = checkIfAnswerCorrect();
            markCardAsDifficulty("medium", isCorrect);

            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession();
            } else {
                moveToNextCard();
            }
        });

        btnHard.setOnClickListener(v -> {
            markCardAsDifficulty("hard", false);

            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession();
            } else {
                moveToNextCard();
            }
        });
    }

    private boolean checkIfAnswerCorrect() {
//        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
//            Card currentCard = flashcardList.get(currentPosition);
//            String correctAnswer = currentCard.getCorrectAnswer().toLowerCase().trim();
//            String userAnswerLower = userAnswer.toLowerCase().trim();
//            return correctAnswer.equals(userAnswerLower);
//        }
        return false;
    }

    private void markCardAsDifficulty(String difficulty, boolean isCorrect) {
//        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
//            Card currentCard = flashcardList.get(currentPosition);
//
//            cardDifficultyMap.put(currentCard.getId(), difficulty);
//            cardCorrectMap.put(currentCard.getId(), isCorrect);
//
//            if (!studiedCardsList.contains(currentCard)) {
//                studiedCardsList.add(currentCard);
//            }
//        }
    }

    private void moveToNextCard() {
        if (currentPosition < flashcardList.size() - 1) {
            if (difficultyButtonsLayout != null) {
                difficultyButtonsLayout.setVisibility(View.GONE);
            }

            if (!isShowingFront) {
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                isShowingFront = true;
            }

            flashcardView.startAnimation(slideOutLeft);
        }
    }

    private void moveToPreviousCard() {
        if (currentPosition > 0) {
            if (difficultyButtonsLayout != null) {
                difficultyButtonsLayout.setVisibility(View.GONE);
            }

            if (!isShowingFront) {
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                isShowingFront = true;
            }

            flashcardView.startAnimation(slideOutRight);
        }
    }

    private void finishStudySession() {
//        try {
//            long studyEndTime = System.currentTimeMillis();
//            int totalCorrectAnswers = 0;
//            int totalReviewedCards = studiedCardsList.size();
//
//            for (Card card : studiedCardsList) {
//                Boolean isCorrect = cardCorrectMap.get(card.getId());
//                if (isCorrect != null && isCorrect) {
//                    totalCorrectAnswers++;
//                }
//            }
//
//            Intent intent = new Intent(this, StudySummaryActivity.class);
//            intent.putExtra("deck_name", deckName);
//            intent.putExtra("studied_cards", new ArrayList<>(studiedCardsList));
//            intent.putExtra("study_start_time", studyStartTime);
//            intent.putExtra("study_end_time", studyEndTime);
//            intent.putExtra("total_correct", totalCorrectAnswers);
//            intent.putExtra("total_reviewed", totalReviewedCards);
//
//            startActivity(intent);
//            finish();
//
//        } catch (Exception e) {
//            Log.e("error", Objects.requireNonNull(e.getMessage()));
//            Toast.makeText(this, "Lỗi khi chuyển đến trang tóm tắt: " + e.getMessage(),
//                    Toast.LENGTH_LONG).show();
//        }
    }
}