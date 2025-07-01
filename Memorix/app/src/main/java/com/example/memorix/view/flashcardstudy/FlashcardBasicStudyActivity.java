package com.example.memorix.view.flashcardstudy;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.memorix.data.remote.api.DeckApi;
import com.example.memorix.data.remote.api.ProgressApi;
import com.example.memorix.data.remote.dto.Progress.ProgressDueResponse;
import com.example.memorix.data.remote.dto.Progress.ProgressRequest;
import com.example.memorix.data.remote.dto.Progress.ProgressResponse;
import com.example.memorix.data.remote.dto.Progress.ProgressUnlearnedLearnedResponse;
import com.example.memorix.data.remote.network.ApiClient;
import com.example.memorix.model.Card;
import com.example.memorix.model.Deck;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlashcardBasicStudyActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private MaterialCardView flashcardView;
    private CardView cardFront, cardBack;
    private TextView tvCardFront, tvCardBack, tvProgress, tvSetTitle;
    private LinearLayout difficultyButtonsLayout;
    private MaterialButton btnHard, btnMedium, btnEasy, btnPrevious, btnNext , btnAgain;
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
    private List<Card> mainFlashcardList = new ArrayList<>(); // thẻ chính

    private final List<Card> cardsNeedReviewList = new ArrayList<>();

    private long deckId;



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

        initFlashcardList();


        // Khởi tạo các view
        initViews();
        tvSetTitle.setText(deckName);

        // Khởi tạo danh sách flashcard (có thể được thay thế bằng dữ liệu thực tế từ database)




        // Thiết lập animation flip
        setupCardFlipAnimation();

        // Thiết lập animation trượt
        setupSlideAnimations();

        // Set up click listeners
        setupClickListeners();

        // Hiển thị flashcard đầu tiên
        displayCurrentFlashcard();
    }

    /**
     * MỤC ĐÍCH:
     * - Là điểm bắt đầu khi người dùng nhấn vào học flashcard.
     * - Gọi song song 2 API: lấy thẻ cần ôn (tới hạn) và thẻ chưa học/đã học.
     * - Ưu tiên học thẻ chưa học + cần ôn. Nếu không có → học lại toàn bộ thẻ đã học.
     *
     * LUỒNG:
     * 1. Gọi API lấy thẻ tới hạn (`getDueByDeck`)
     * 2. Trong callback, tiếp tục gọi API `getUnlearnedAndLearned`
     * 3. Kết quả cả hai được đưa vào hàm `handleUnlearnedAndLearned`
     */
    private void initFlashcardList() {
        flashcardList = new ArrayList<>();
        mainFlashcardList = new ArrayList<>();

        ProgressApi api = ApiClient.getClient().create(ProgressApi.class);
        Call<ProgressDueResponse> dueCall = api.getDueByDeck(deckId);
        Call<ProgressUnlearnedLearnedResponse> unlearnedCall = api.getUnlearnedAndLearned();

        dueCall.enqueue(new Callback<ProgressDueResponse>() {
            @Override
            public void onResponse(Call<ProgressDueResponse> call, Response<ProgressDueResponse> response) {
                List<Card> dueCards = extractDueCards(response);

                unlearnedCall.enqueue(new Callback<ProgressUnlearnedLearnedResponse>() {
                    @Override
                    public void onResponse(Call<ProgressUnlearnedLearnedResponse> call, Response<ProgressUnlearnedLearnedResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            handleUnlearnedAndLearned(response.body(), dueCards);
                        } else {
                            Toast.makeText(FlashcardBasicStudyActivity.this, "Lỗi tải thẻ chưa học", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProgressUnlearnedLearnedResponse> call, Throwable t) {
                        Toast.makeText(FlashcardBasicStudyActivity.this, "Lỗi kết nối (unlearned): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ProgressDueResponse> call, Throwable t) {
                Toast.makeText(FlashcardBasicStudyActivity.this, "Lỗi kết nối (due): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * MỤC ĐÍCH:
     * - Tách danh sách thẻ tới hạn (due) từ response.
     * - Chỉ lấy những thẻ thuộc đúng deckId hiện tại.
     *
     * LUỒNG:
     * - Kiểm tra null và response thành công.
     * - Duyệt qua các list: twoSided, multipleChoice (nếu có).
     * - Trả về danh sách card cần ôn.
     */
    private List<Card> extractDueCards(Response<ProgressDueResponse> response) {
        List<Card> dueCards = new ArrayList<>();

        if (response.isSuccessful() && response.body() != null) {
            ProgressDueResponse.DueCards due = response.body().getDue();
            if (due != null) {
                if (due.getTwoSided() != null) {
                    for (Card c : due.getTwoSided()) {
                        if (c.getDeckId() == deckId) dueCards.add(c);
                    }
                }
                if (due.getMultipleChoice() != null) {
                    for (Card c : due.getMultipleChoice()) {
                        if (c.getDeckId() == deckId) dueCards.add(c);
                    }
                }
            }
        }

        return dueCards;
    }

    /**
     * MỤC ĐÍCH:
     * - Xử lý logic tổng hợp các thẻ:
     *    + Thẻ tới hạn (due)
     *    + Thẻ chưa học
     *    + Thẻ đã học (chỉ khi học lại)
     *
     * LUỒNG:
     * 1. Trích xuất các thẻ từ response theo deckId.
     * 2. Nếu có thẻ due hoặc unlearned → học như bình thường.
     * 3. Nếu KHÔNG có due và unlearned, nhưng có learned → học lại toàn bộ.
     * 4. Nếu KHÔNG có gì → thông báo không có dữ liệu.
     */
    private void handleUnlearnedAndLearned(ProgressUnlearnedLearnedResponse body, List<Card> dueCards) {
        List<Card> unlearnedCards = new ArrayList<>();
        List<Card> learnedCards = new ArrayList<>();

        ProgressUnlearnedLearnedResponse.UnlearnedCards unlearned = body.getUnlearned();
        ProgressUnlearnedLearnedResponse.LearnedCards learned = body.getLearned();

        if (unlearned != null) {
            unlearnedCards.addAll(filterCardsByDeck(unlearned.getTwoSided()));
            unlearnedCards.addAll(filterCardsByDeck(unlearned.getMultipleChoice()));
            unlearnedCards.addAll(filterCardsByDeck(unlearned.getFillInBlank()));
        }

        if (learned != null) {
            learnedCards.addAll(filterCardsByDeck(learned.getTwoSided()));
//            learnedCards.addAll(filterCardsByDeck(learned.getMultipleChoice()));
//            learnedCards.addAll(filterCardsByDeck(learned.getFillInBlank()));
        }

        // Gộp các thẻ ưu tiên học: tới hạn + chưa học
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

    /**
     * MỤC ĐÍCH:
     * - Lọc các thẻ chỉ lấy những thẻ có đúng deckId đang học.
     *
     * LUỒNG:
     * - Duyệt từng thẻ, kiểm tra deckId khớp thì thêm vào kết quả.
     */
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
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        tvCardFront = findViewById(R.id.tv_card_front);
        tvCardBack = findViewById(R.id.tv_card_back);
        difficultyButtonsLayout = findViewById(R.id.difficulty_buttons_layout);
        btnHard = findViewById(R.id.btn_hard);
        btnAgain = findViewById(R.id.btn_again);
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

        String front = currentCard.getContent().has("front") ? currentCard.getContent().get("front").getAsString() : "";
        String back = currentCard.getContent().has("back") ? currentCard.getContent().get("back").getAsString() : "";

        tvCardFront.setText(front);
        tvCardBack.setText(back);
        updateProgressBar();
        checkButtonStatus();
        }
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

        btnAgain.setOnClickListener(v->{
            if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
                // Gửi đánh giá là "again" và không đúng
                markCardAsDifficulty("again", false);
            }

            // Sang thẻ tiếp theo hoặc kết thúc
            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession(); // Kết thúc ngay, không học lại
            } else {
                moveToNextCard();
            }
        });
        // Xử lý các nút đánh giá mức độ khó
        btnEasy.setOnClickListener(v -> {
            markCardAsDifficulty("easy", true);

            if (currentPosition >= flashcardList.size() - 1) {
                finishStudySession();
            } else {
                moveToNextCard();
            }
        });

        btnMedium.setOnClickListener(v -> {
            // Xử lý khi người dùng đánh giá "Vừa"
            markCardAsDifficulty("good", true);

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
            submitProgress(currentCard, difficulty, isCorrect);
        }
    }
    /**
     * MỤC ĐÍCH:
     * - Gửi thông tin đánh giá độ khó của thẻ (`POST /api/progress`)
     * - Cập nhật thời gian ôn tiếp theo tùy theo độ khó (`PUT /api/progress`)
     *
     * LUỒNG XỬ LÝ:
     * 1. Tính ngày ôn tiếp theo dựa trên độ khó.
     * 2. Gửi request POST để ghi lại đánh giá.
     * 3. Gửi request PUT để cập nhật next_review_at.
     */
    private void submitProgress(Card card, String difficulty, boolean isCorrect) {
        if (card == null) return;

        int flashcardId = card.getFlashcardId();

        // Lưu local cho thống kê
        cardDifficultyMap.put(String.valueOf(flashcardId), difficulty);
        cardCorrectMap.put(String.valueOf(flashcardId), isCorrect);

        if (difficulty.equals("again") || difficulty.equals("hard")) {
            if (!cardsNeedReviewList.contains(card)) {
                cardsNeedReviewList.add(card);
            }
        }

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

            Log.d("DEBUG", "Tổng số thẻ đã học: " + totalReviewedCards);

             //Đếm số câu trả lời đúng
            for (Card card : studiedCardsList) {
                String id = String.valueOf(card.getFlashcardId());
                Boolean isCorrect = cardCorrectMap.get(id);
                Log.d("DEBUG", "Card ID: " + id + ", isCorrect = " + isCorrect);
                if (isCorrect != null && isCorrect) {
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


}