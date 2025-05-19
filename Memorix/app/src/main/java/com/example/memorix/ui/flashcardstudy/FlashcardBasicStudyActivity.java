package com.example.memorix.ui.flashcardstudy;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memorix.data.Flashcard;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlashcardBasicStudyActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private MaterialCardView flashcardView;
    private CardView cardFront, cardBack;
    private TextView tvCardFront, tvCardBack, tvProgress, tvSetTitle;
    private LinearLayout difficultyButtonsLayout;
    private MaterialButton btnHard, btnMedium, btnEasy, btnPrevious, btnNext;
    AnimatorSet frontAnimation, backAnimation;

    private boolean isShowingFront = true;

    // List of flashcards
    private List<Flashcard> flashcardList;
    // Current position in the list
    private int currentPosition = 0;
    // Animations for slide transition
    private Animation slideOutLeft, slideInRight, slideOutRight, slideInLeft;
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

        // Add sample flashcards
        flashcardList.add(new Flashcard("Present Simple", "Thì hiện tại đơn, diễn tả thói quen hoặc sự thật hiển nhiên"));
        flashcardList.add(new Flashcard("Present Continuous", "Thì hiện tại tiếp diễn, diễn tả hành động đang xảy ra"));
        flashcardList.add(new Flashcard("Present Perfect", "Thì hiện tại hoàn thành, diễn tả hành động đã hoàn thành ở hiện tại"));
        flashcardList.add(new Flashcard("Past Simple", "Thì quá khứ đơn, diễn tả hành động đã xảy ra trong quá khứ"));
        flashcardList.add(new Flashcard("Past Continuous", "Thì quá khứ tiếp diễn, diễn tả hành động đang xảy ra trong quá khứ"));
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

        setupCard();
    }

    private void setupCard(){
        cardFront.setVisibility(View.VISIBLE);
        cardFront.setAlpha(1.0f);
        cardBack.setVisibility(View.GONE);
        cardBack.setAlpha(0.0f);
        isShowingFront = true;
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
                // Chuyển đến thẻ tiếp theo và bắt đầu animation slide in
                currentPosition++;
                if (currentPosition >= flashcardList.size()) {
                    currentPosition = flashcardList.size() - 1;
                }
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
                // Chuyển đến thẻ trước đó và bắt đầu animation slide in
                currentPosition--;
                if (currentPosition < 0) {
                    currentPosition = 0;
                }
                displayCurrentFlashcard();
                flashcardView.startAnimation(slideInLeft);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayCurrentFlashcard() {
        // Đảm bảo thẻ hiển thị mặt trước
        setupCard();

        // Hiển thị flashcard hiện tại
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Flashcard currentCard = flashcardList.get(currentPosition);
            tvCardFront.setText(currentCard.getFront());
            tvCardBack.setText(currentCard.getBack());

            // Đảm bảo layout đánh giá độ khó ẩn khi hiển thị mặt trước
            difficultyButtonsLayout.setVisibility(View.GONE);

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
        flashcardView.setOnClickListener(v -> {
            flipCard();
        });

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
            markCardAsDifficulty("easy");
            if (currentPosition < flashcardList.size() - 1) {
                moveToNextCard();
            } else {
                Toast.makeText(this, "Đã hoàn thành tất cả các thẻ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnMedium.setOnClickListener(v -> {
            // Xử lý khi người dùng đánh giá "Vừa"
            markCardAsDifficulty("medium");
            if (currentPosition < flashcardList.size() - 1) {
                moveToNextCard();
            } else {
                Toast.makeText(this, "Đã hoàn thành tất cả các thẻ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnHard.setOnClickListener(v -> {
            // Xử lý khi người dùng đánh giá "Khó"
            markCardAsDifficulty("hard");
            if (currentPosition < flashcardList.size() - 1) {
                moveToNextCard();
            } else {
                Toast.makeText(this, "Đã hoàn thành tất cả các thẻ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markCardAsDifficulty(String difficulty) {
        // TODO: Thực hiện lưu trữ đánh giá độ khó của thẻ
        if (currentPosition >= 0 && currentPosition < flashcardList.size()) {
            Flashcard currentCard = flashcardList.get(currentPosition);
            // currentCard.setDifficulty(difficulty);
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
}