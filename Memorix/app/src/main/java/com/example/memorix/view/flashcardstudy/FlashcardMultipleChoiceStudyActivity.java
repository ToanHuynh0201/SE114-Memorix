package com.example.memorix.view.flashcardstudy;

import android.os.Bundle;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FlashcardMultipleChoiceStudyActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private MaterialCardView flashcardView;
    private CardView mcFrontLayout, mcBackLayout;
    private TextView tvQuestion, tvAnswerResult, tvProgress, tvSetTitle;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private MaterialButton btnPrevious, btnNext;
    AnimatorSet frontAnimation, backAnimation;

    private boolean isShowingFront = true;
    private boolean hasAnswered = false;
    private int selectedOptionIndex = -1;
    private int correctOptionIndex = -1;

    // List of flashcards
    private List<Card> flashcardList;
    // Current position in the list
    private int currentPosition = 0;
    // Animations for slide transition
    private Animation slideOutLeft, slideInRight, slideOutRight, slideInLeft;

    private final String deckName = "Sample Multiple Choice Set";
    private final List<Card> studiedCardsList = new ArrayList<>();
    private long studyStartTime;
    private final Map<String, String> cardDifficultyMap = new HashMap<>();
    private final Map<String, Boolean> cardCorrectMap = new HashMap<>();

    // Multiple choice options for each card
    private final List<List<String>> allOptions = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard_multiple_choice_study);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studyStartTime = System.currentTimeMillis();

        // Initialize flashcard list and options
        initFlashcardList();
        initMultipleChoiceOptions();

        // Initialize views
        initViews();

        // Setup card flip animation
        setupCardFlipAnimation();

        // Setup slide animations
        setupSlideAnimations();

        // Set up click listeners
        setupClickListeners();

        // Display first flashcard
        displayCurrentFlashcard();
    }
    private void initFlashcardList() {
        flashcardList = new ArrayList<>();

//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "What is the capital of France?", "Paris"));
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "What is the largest planet in our solar system?", "Jupiter"));
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "Who wrote 'Romeo and Juliet'?", "William Shakespeare"));
//        flashcardList.add(new Card(UUID.randomUUID().toString(), "1", "Who is the author of '1984'?", "George Orwell"));
    }

    private void initMultipleChoiceOptions() {
        allOptions.clear();

        // Options for "What is the capital of France?"
        List<String> options1 = new ArrayList<>();
        options1.add("London");
        options1.add("Berlin");
        options1.add("Paris");
        options1.add("Madrid");
        allOptions.add(options1);

        // Options for "What is the largest planet in our solar system?"
        List<String> options2 = new ArrayList<>();
        options2.add("Mars");
        options2.add("Jupiter");
        options2.add("Saturn");
        options2.add("Earth");
        allOptions.add(options2);

        // Options for "Who wrote 'Romeo and Juliet'?"
        List<String> options3 = new ArrayList<>();
        options3.add("Charles Dickens");
        options3.add("William Shakespeare");
        options3.add("Mark Twain");
        options3.add("Jane Austen");
        allOptions.add(options3);

        // Options for "Who is the author of '1984'?"
        List<String> options4 = new ArrayList<>();
        options4.add("Aldous Huxley");
        options4.add("Ray Bradbury");
        options4.add("George Orwell");
        options4.add("H.G. Wells");
        allOptions.add(options4);
    }

    private void initViews() {
        tvSetTitle = findViewById(R.id.tv_set_title);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        flashcardView = findViewById(R.id.flashcard_view);
        mcFrontLayout = findViewById(R.id.mc_front_layout);
        mcBackLayout = findViewById(R.id.mc_back_layout);
        tvQuestion = findViewById(R.id.tv_question);
        tvAnswerResult = findViewById(R.id.tv_answer_result);
        btnOption1 = findViewById(R.id.btn_option_1);
        btnOption2 = findViewById(R.id.btn_option_2);
        btnOption3 = findViewById(R.id.btn_option_3);
        btnOption4 = findViewById(R.id.btn_option_4);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);

        // Set progress bar max value
        progressBar.setMax(flashcardList.size());

        // Set deck name
        tvSetTitle.setText(deckName);

        setupCard();
    }

    private void setupCard() {
        mcBackLayout.clearAnimation();
        mcFrontLayout.clearAnimation();

        mcFrontLayout.setVisibility(View.VISIBLE);
        mcFrontLayout.setAlpha(1.0f);
        mcFrontLayout.setRotationY(0f);

        mcBackLayout.setVisibility(View.GONE);
        mcBackLayout.setAlpha(0.0f);
        mcBackLayout.setRotationY(0f);

        isShowingFront = true;
        hasAnswered = false;
        selectedOptionIndex = -1;
        correctOptionIndex = -1;

        // Reset option button colors
        resetOptionButtonColors();
    }

    @SuppressLint("ResourceType")
    private void setupCardFlipAnimation() {
        float scale = getResources().getDisplayMetrics().density;
        mcFrontLayout.setCameraDistance(8000 * scale);
        mcBackLayout.setCameraDistance(8000 * scale);

        frontAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_out);
        backAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_in);
    }

    private void setupSlideAnimations() {
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
//            tvQuestion.setText(currentCard.getQuestion());
//
//            // Set up multiple choice options
//            setupMultipleChoiceOptions();
//
//            updateProgressBar();
//        }

        checkButtonStatus();
    }

    private void setupMultipleChoiceOptions() {
//        if (currentPosition < allOptions.size()) {
//            List<String> options = new ArrayList<>(allOptions.get(currentPosition));
//            Card currentCard = flashcardList.get(currentPosition);
//            String correctAnswer = currentCard.getAnswer();
//
//            // Find correct answer index before shuffling
//            correctOptionIndex = options.indexOf(correctAnswer);
//
//            // Shuffle options
//            Collections.shuffle(options);
//
//            // Update correct answer index after shuffling
//            correctOptionIndex = options.indexOf(correctAnswer);
//
//            // Set options to buttons
//            btnOption1.setText(options.get(0));
//            btnOption2.setText(options.get(1));
//            btnOption3.setText(options.get(2));
//            btnOption4.setText(options.get(3));
//        }
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

    private void flipCard() {
        if (isShowingFront) {
            // Flip from front to back
            frontAnimation.setTarget(mcFrontLayout);
            backAnimation.setTarget(mcBackLayout);

            mcBackLayout.setVisibility(View.VISIBLE);

            frontAnimation.start();
            backAnimation.start();

            mcFrontLayout.postDelayed(() -> mcFrontLayout.setVisibility(View.GONE), 300);
        } else {
            // Flip from back to front
            @SuppressLint("ResourceType") AnimatorSet frontAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_in);
            @SuppressLint("ResourceType") AnimatorSet backAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_out);

            frontAnimator.setTarget(mcFrontLayout);
            backAnimator.setTarget(mcBackLayout);

            mcFrontLayout.setVisibility(View.VISIBLE);

            backAnimator.start();
            frontAnimator.start();

            mcBackLayout.postDelayed(() -> mcBackLayout.setVisibility(View.GONE), 300);
        }

        isShowingFront = !isShowingFront;
    }

    private void setupClickListeners() {
        // Click on flashcard to flip
        flashcardView.setOnClickListener(v -> {
            if (!isShowingFront || hasAnswered) {
                flipCard();
            }
        });

        // Option button listeners
        btnOption1.setOnClickListener(v -> selectOption(0, btnOption1));
        btnOption2.setOnClickListener(v -> selectOption(1, btnOption2));
        btnOption3.setOnClickListener(v -> selectOption(2, btnOption3));
        btnOption4.setOnClickListener(v -> selectOption(3, btnOption4));

        // Navigation buttons
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
    }

    private void selectOption(int optionIndex, Button selectedButton) {
        if (hasAnswered) return;

        selectedOptionIndex = optionIndex;
        hasAnswered = true;

        // Highlight selected option and show correct answer
        showAnswerResult(selectedButton);

        // Mark card with difficulty based on correctness
        boolean isCorrect = (selectedOptionIndex == correctOptionIndex);
        String difficulty = isCorrect ? "easy" : "hard";
        markCardAsDifficulty(difficulty, isCorrect);

        // Auto-advance after a short delay
        flashcardView.postDelayed(() -> {
            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession();
            } else {
                flipCard();
            }
        }, 500);
    }

    @SuppressLint("SetTextI18n")
    private void showAnswerResult(Button selectedButton) {
//        // Reset all button colors first
//        resetOptionButtonColors();
//
//        // Highlight correct answer in green
//        Button correctButton = getButtonByIndex(correctOptionIndex);
//        if (correctButton != null) {
//            correctButton.setBackgroundTintList(getColorStateList(R.color.secondary_color)); // Green
//        }
//
//        // If selected answer is wrong, highlight it in red
//        if (selectedOptionIndex != correctOptionIndex) {
//            selectedButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
//        }
//
//        // Set result text
//        Card currentCard = flashcardList.get(currentPosition);
//        boolean isCorrect = (selectedOptionIndex == correctOptionIndex);
//
//        if (isCorrect) {
//            tvAnswerResult.setText("Chính xác!\n\nĐáp án: " + currentCard.getAnswer());
//            tvAnswerResult.setTextColor(getColor(R.color.secondary_color));
//        } else {
//            tvAnswerResult.setText("Sai rồi!\n\nĐáp án đúng: " + currentCard.getAnswer());
//            tvAnswerResult.setTextColor(getColor(android.R.color.holo_red_dark));
//        }
    }

    private Button getButtonByIndex(int index) {
        switch (index) {
            case 0: return btnOption1;
            case 1: return btnOption2;
            case 2: return btnOption3;
            case 3: return btnOption4;
            default: return null;
        }
    }

    private void resetOptionButtonColors() {
        btnOption1.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        btnOption2.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        btnOption3.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        btnOption4.setBackgroundTintList(getColorStateList(R.color.option_default_color));
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
            if (!isShowingFront) {
                mcBackLayout.setVisibility(View.GONE);
                mcFrontLayout.setVisibility(View.VISIBLE);
                isShowingFront = true;
            }

            flashcardView.startAnimation(slideOutLeft);
        }
    }

    private void moveToPreviousCard() {
        if (currentPosition > 0) {
            if (!isShowingFront) {
                mcBackLayout.setVisibility(View.GONE);
                mcFrontLayout.setVisibility(View.VISIBLE);
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