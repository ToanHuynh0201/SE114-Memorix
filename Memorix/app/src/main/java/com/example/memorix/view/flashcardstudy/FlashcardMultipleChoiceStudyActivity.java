package com.example.memorix.view.flashcardstudy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.example.memorix.data.remote.api.ProgressApi;
import com.example.memorix.data.remote.dto.Progress.ProgressDueResponse;
import com.example.memorix.data.remote.dto.Progress.ProgressRequest;
import com.example.memorix.data.remote.dto.Progress.ProgressResponse;
import com.example.memorix.data.remote.dto.Progress.ProgressUnlearnedLearnedResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.model.Card;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    private List<Card> mainFlashcardList;
    // Current position in the list
    private int currentPosition = 0;
    // Animations for slide transition
    private Animation slideOutLeft, slideInRight, slideOutRight, slideInLeft;

    private final String deckName = "Sample Multiple Choice Set";
    private final List<Card> studiedCardsList = new ArrayList<>();
    private long studyStartTime;
    private final Map<String, String> cardDifficultyMap = new HashMap<>();
    private final Map<String, Boolean> cardCorrectMap = new HashMap<>();
    private long deckId;
    private int wrongAttemptsForCurrentCard = 0;


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

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        deckId = getIntent().getLongExtra("deck_id", -1L);
        String deckName = getIntent().getStringExtra("deck_name");

        Log.d("DEBUG_DECK", "Deck ID nhận từ Intent = " + deckId);
        Log.d("DEBUG_DECK", "Deck Name nhận từ Intent = " + deckName);


        if (deckId == -1 || deckName == null) {
            Toast.makeText(this, "Lỗi: Không có thông tin bộ thẻ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        studyStartTime = System.currentTimeMillis();

        // Initialize flashcard list and options
        initFlashcardList();

        // Initialize views
        initViews();
        tvSetTitle.setText(deckName);
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
        mainFlashcardList = new ArrayList<>();

        ProgressApi api = ApiClient.getClient().create(ProgressApi.class);

        Call<ProgressDueResponse> dueCall = api.getDueByDeck(deckId);
        Call<ProgressUnlearnedLearnedResponse> unlearnedCall = api.getUnlearnedAndLearned();

        dueCall.enqueue(new Callback<ProgressDueResponse>() {
            @Override
            public void onResponse(Call<ProgressDueResponse> call, Response<ProgressDueResponse> response) {
                List<Card> dueCards = extractDueMultipleChoiceCards(response);

                unlearnedCall.enqueue(new Callback<ProgressUnlearnedLearnedResponse>() {
                    @Override
                    public void onResponse(Call<ProgressUnlearnedLearnedResponse> call, Response<ProgressUnlearnedLearnedResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            handleUnlearnedAndLearned(response.body(), dueCards);
                        } else {
                            Toast.makeText(FlashcardMultipleChoiceStudyActivity.this, "Lỗi tải thẻ chưa học", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProgressUnlearnedLearnedResponse> call, Throwable t) {
                        Toast.makeText(FlashcardMultipleChoiceStudyActivity.this, "Lỗi kết nối (unlearned): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ProgressDueResponse> call, Throwable t) {
                Toast.makeText(FlashcardMultipleChoiceStudyActivity.this, "Lỗi kết nối (due): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Card> extractDueMultipleChoiceCards(Response<ProgressDueResponse> response) {
        List<Card> dueCards = new ArrayList<>();

        if (response.isSuccessful() && response.body() != null) {
            ProgressDueResponse.DueCards due = response.body().getDue();
            if (due != null && due.getMultipleChoice() != null) {
                for (Card c : due.getMultipleChoice()) {
                    if (c.getDeckId() == deckId) {
                        dueCards.add(c);
                    }
                }
            }
        }

        return dueCards;
    }

    private void handleUnlearnedAndLearned(ProgressUnlearnedLearnedResponse body, List<Card> dueCards) {
        List<Card> unlearnedCards = new ArrayList<>();
        List<Card> learnedCards = new ArrayList<>();

        ProgressUnlearnedLearnedResponse.UnlearnedCards unlearned = body.getUnlearned();
        ProgressUnlearnedLearnedResponse.LearnedCards learned = body.getLearned();

        if (unlearned != null) {
            unlearnedCards.addAll(filterCardsByDeck(unlearned.getMultipleChoice()));
        }

        if (learned != null) {
            learnedCards.addAll(filterCardsByDeck(learned.getMultipleChoice()));
        }

        mainFlashcardList.clear();
        mainFlashcardList.addAll(dueCards);
        mainFlashcardList.addAll(unlearnedCards);

        if (mainFlashcardList.isEmpty() && !learnedCards.isEmpty()) {
            Toast.makeText(this, "Bạn đã học hết. Học lại từ đầu nhé!", Toast.LENGTH_LONG).show();
            mainFlashcardList.addAll(learnedCards);
        }

        if (mainFlashcardList.isEmpty()) {
            Toast.makeText(this, "Bộ thẻ này không có dữ liệu!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            flashcardList = new ArrayList<>(mainFlashcardList);
            currentPosition = 0;
            setupCard();
            displayCurrentFlashcard();
            updateProgressBar();
        }
    }

    private List<Card> filterCardsByDeck(List<Card> inputList) {
        List<Card> result = new ArrayList<>();
        if (inputList != null) {
            for (Card c : inputList) {
                if (c.getDeckId() == deckId) {
                    result.add(c);
                }
            }
        }
        return result;
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
        enableAllOptionButtons();
    }

    private void enableAllOptionButtons() {
        btnOption1.setEnabled(true);
        btnOption1.setAlpha(1.0f);
        btnOption2.setEnabled(true);
        btnOption2.setAlpha(1.0f);
        btnOption3.setEnabled(true);
        btnOption3.setAlpha(1.0f);
        btnOption4.setEnabled(true);
        btnOption4.setAlpha(1.0f);
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
            public void onAnimationStart(Animation animation) {
            }

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
            public void onAnimationRepeat(Animation animation) {
            }
        });

        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

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
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayCurrentFlashcard() {
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Card currentCard = flashcardList.get(currentPosition);

            // Lấy câu hỏi từ JsonObject content
            if (currentCard.getContent() != null && currentCard.getContent().has("question")) {
                String question = currentCard.getContent().get("question").getAsString();
                tvQuestion.setText(question);
            }

            // Set up multiple choice options
            setupMultipleChoiceOptions();

            updateProgressBar();
        }

        checkButtonStatus();
    }

    private void setupMultipleChoiceOptions() {
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Card currentCard = flashcardList.get(currentPosition);

            if (currentCard.getContent() != null) {
                // Lấy options từ JsonArray trong content
                if (currentCard.getContent().has("options")) {
                    com.google.gson.JsonArray optionsArray = currentCard.getContent().getAsJsonArray("options");
                    List<String> options = new ArrayList<>();

                    // Convert JsonArray to List<String>
                    for (int i = 0; i < optionsArray.size(); i++) {
                        options.add(optionsArray.get(i).getAsString());
                    }

                    // Lấy đáp án đúng
                    String correctAnswer = "";
                    if (currentCard.getContent().has("answer")) {
                        correctAnswer = currentCard.getContent().get("answer").getAsString();
                    }

                    // Tìm index của đáp án đúng trước khi shuffle
                    correctOptionIndex = options.indexOf(correctAnswer);

                    // Shuffle options để random hóa thứ tự
                    Collections.shuffle(options);

                    // Cập nhật lại index của đáp án đúng sau khi shuffle
                    correctOptionIndex = options.indexOf(correctAnswer);

                    // Set text cho các button options
                    if (options.size() >= 4) {
                        btnOption1.setText(options.get(0));
                        btnOption2.setText(options.get(1));
                        btnOption3.setText(options.get(2));
                        btnOption4.setText(options.get(3));
                    } else {
                        // Nếu không đủ 4 options, set text trống cho các button còn lại
                        btnOption1.setText(!options.isEmpty() ? options.get(0) : "");
                        btnOption2.setText(options.size() > 1 ? options.get(1) : "");
                        btnOption3.setText(options.size() > 2 ? options.get(2) : "");
                        btnOption4.setText(options.size() > 3 ? options.get(3) : "");

                        // Ẩn các button không cần thiết
                        btnOption1.setVisibility(!options.isEmpty() ? View.VISIBLE : View.GONE);
                        btnOption2.setVisibility(options.size() > 1 ? View.VISIBLE : View.GONE);
                        btnOption3.setVisibility(options.size() > 2 ? View.VISIBLE : View.GONE);
                        btnOption4.setVisibility(options.size() > 3 ? View.VISIBLE : View.GONE);
                    }
                }
            }
        }
    }

    private String getCurrentCorrectAnswer() {
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Card currentCard = flashcardList.get(currentPosition);
            if (currentCard.getContent() != null && currentCard.getContent().has("answer")) {
                return currentCard.getContent().get("answer").getAsString();
            }
        }
        return "";
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
        boolean isCorrect = (selectedOptionIndex == correctOptionIndex);

        if (isCorrect) {
            hasAnswered = true;
            selectedButton.setBackgroundTintList(getColorStateList(R.color.secondary_color)); // xanh

            showCorrectAnswerResult();

            // Xác định mức rating
            int rating = calculateRating(wrongAttemptsForCurrentCard);

            markCardAsDifficulty(ratingToDifficulty(rating), true);
            submitProgress(flashcardList.get(currentPosition), ratingToDifficulty(rating),isCorrect);

            flashcardView.postDelayed(() -> {
                if (currentPosition >= flashcardList.size() - 1) {
                    finishStudySession();
                } else {
                    flipCard();
                }
            }, 1500);

        } else {
            wrongAttemptsForCurrentCard++;
            selectedButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
            selectedButton.setEnabled(false);
            selectedButton.setAlpha(0.5f);
            showIncorrectAnswerFeedback();
        }
    }

    private int calculateRating(int wrongAttempts) {
        if (wrongAttempts == 0) return 3; // easy
        else if (wrongAttempts == 1) return 2; // good
        else if (wrongAttempts == 2) return 1; // hard
        else return 0; // again
    }

    private String ratingToDifficulty(int rating) {
        switch (rating) {
            case 3:
                return "easy";
            case 2:
                return "good";
            case 1:
                return "hard";
            default:
                return "again";
        }
    }

    @SuppressLint("SetTextI18n")
    private void showCorrectAnswerResult() {
        String correctAnswer = getCurrentCorrectAnswer();
        tvAnswerResult.setText("🎉 Chính xác!\n\nĐáp án: " + correctAnswer);
        tvAnswerResult.setTextColor(getColor(R.color.secondary_color));
    }

    private void showIncorrectAnswerFeedback() {
        // Hiển thị feedback ngay trên card mà không cần flip
        Toast.makeText(this, "Sai rồi! Hãy thử lại với đáp án khác", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void showAnswerResult(Button selectedButton) {
        // Reset all button colors first
        // Reset all button colors first (trừ button đã chọn đúng)
        resetOtherButtonColors(selectedButton);

        // Highlight correct answer in green (đã được set trong selectOption)
        String correctAnswer = getCurrentCorrectAnswer();
        tvAnswerResult.setText("🎉 Chính xác!\n\nĐáp án: " + correctAnswer);
        tvAnswerResult.setTextColor(getColor(R.color.secondary_color));
    }

    private Button getButtonByIndex(int index) {
        switch (index) {
            case 0:
                return btnOption1;
            case 1:
                return btnOption2;
            case 2:
                return btnOption3;
            case 3:
                return btnOption4;
            default:
                return null;
        }
    }

    private void resetOtherButtonColors(Button excludeButton) {
        if (btnOption1 != excludeButton) {
            btnOption1.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        }
        if (btnOption2 != excludeButton) {
            btnOption2.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        }
        if (btnOption3 != excludeButton) {
            btnOption3.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        }
        if (btnOption4 != excludeButton) {
            btnOption4.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        }
    }

    private void resetOptionButtonColors() {
        btnOption1.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        btnOption2.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        btnOption3.setBackgroundTintList(getColorStateList(R.color.option_default_color));
        btnOption4.setBackgroundTintList(getColorStateList(R.color.option_default_color));
    }

    private void markCardAsDifficulty(String difficulty, boolean isCorrect) {
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Card currentCard = flashcardList.get(currentPosition);
            String cardId = String.valueOf(currentCard.getFlashcardId());

            cardDifficultyMap.put(cardId, difficulty);
            cardCorrectMap.put(cardId, isCorrect);

            if (!studiedCardsList.contains(currentCard)) {
                studiedCardsList.add(currentCard);
            }
        }
    }

    private void submitProgress(Card card, String difficulty, boolean isCorrect) {
        if (card == null) return;

        int flashcardId = card.getFlashcardId();

        // Lưu local cho thống kê
        cardDifficultyMap.put(String.valueOf(flashcardId), difficulty);
        cardCorrectMap.put(String.valueOf(flashcardId), isCorrect);


        if (!studiedCardsList.contains(card)) {
            studiedCardsList.add(card);
        }

        // Tính ngày ôn tiếp theo
        int daysLater;
        switch (difficulty) {
            case "easy": daysLater = 10; break;
            case "good": daysLater = 5; break;
            case "hard": daysLater = 2; break;
            default: daysLater = 0; break;
        }
        String nextReviewDate = calculateNextReviewDate(daysLater);

        ProgressApi api = ApiClient.getClient().create(ProgressApi.class);

        // Gửi POST
        ProgressRequest postRequest = new ProgressRequest(flashcardId, difficulty);
        api.postProgress(postRequest).enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("Progress", "POST thành công: " + difficulty);
                } else {
                    Log.e("Progress", "POST lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                Log.e("Progress", "POST thất bại: " + t.getMessage());
            }
        });

        // Gửi PUT
        ProgressRequest putRequest = new ProgressRequest();
        putRequest.setFlashcard_id(flashcardId);
        putRequest.setRating(difficulty);
        putRequest.setNext_review_at(nextReviewDate);

        api.updateProgress(putRequest).enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("Progress", "PUT thành công: " + nextReviewDate);
                } else {
                    Log.e("Progress", "PUT lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                Log.e("Progress", "PUT thất bại: " + t.getMessage());
            }
        });
    }

    private String calculateNextReviewDate(int minutesLater) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        java.util.Calendar calendar = java.util.Calendar.getInstance();
//        calendar.add(java.util.Calendar.DAY_OF_YEAR, daysLater);
        calendar.add(java.util.Calendar.MINUTE, minutesLater); // Thay vì add ngày
        return sdf.format(calendar.getTime());
    }

    private void moveToNextCard() {
        if (currentPosition < flashcardList.size() - 1) {
            if (!isShowingFront) {
                mcBackLayout.setVisibility(View.GONE);
                mcFrontLayout.setVisibility(View.VISIBLE);
                isShowingFront = true;
            }
            wrongAttemptsForCurrentCard = 0;
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
            wrongAttemptsForCurrentCard = 0;
            flashcardView.startAnimation(slideOutRight);
        }
    }

    private void finishStudySession() {
            try {
                // Tính toán các thống kê cần thiết
                long studyEndTime = System.currentTimeMillis();
                int totalCorrectAnswers = 0;
                int totalReviewedCards = studiedCardsList.size();

                Log.d("DEBUG", "Tổng số thẻ đã học: " + totalReviewedCards);

                //Đếm số câu trả lời đúng
                for (Card card : studiedCardsList) {
                    String id = String.valueOf(card.getFlashcardId());
                    String diff = cardDifficultyMap.get(id);
                    Log.d("StudySummary", "Card ID: " + id + ", difficulty = " + diff);
                    if ("easy".equals(diff) || "good".equals(diff)) {
                        totalCorrectAnswers++;
                    }
                }
                Log.d("DEBUG", "Tổng câu đúng: " + totalCorrectAnswers);

//             Kiểm tra StudyStatisticsHelper có tồn tại không
//             StudyStatisticsHelper.StudySessionStats stats =
//                     StudyStatisticsHelper.calculateStats(studiedCardsList, studyStartTime, studyEndTime);

                // Navigate to StudySummaryActivity với proper error handling
                Intent intent = new Intent(this, StudySummaryActivity.class);
                intent.putExtra("deck_name", deckName);
                ArrayList<String> studiedCardSummaries = new ArrayList<>();
                for (Card card : studiedCardsList) {
                    studiedCardSummaries.add(card.toString());
                }
                intent.putStringArrayListExtra("studied_cards", studiedCardSummaries); // Đảm bảo Serializable
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
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Kết thúc học?")
                .setMessage("Bạn có chắc muốn dừng học giữa chừng?")
                .setPositiveButton("Dừng", (dialog, which) -> {
                    finishStudySession(); // lưu kết quả
                    super.onBackPressed(); // thoát sau khi lưu
                })
                .setNegativeButton("Tiếp tục", null)
                .show();
    }
}