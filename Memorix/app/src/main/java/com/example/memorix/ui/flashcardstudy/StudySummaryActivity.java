package com.example.memorix.ui.flashcardstudy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.example.memorix.data.Card;
import com.example.memorix.ui.MainActivity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StudySummaryActivity extends AppCompatActivity {
    private TextView tvDeckName;
    private TextView tvTotalCards;
    private TextView tvCorrectCount;
    private TextView tvAccuracyRate;
    private ProgressBar progressAccuracy;
    private TextView tvStudyTime;
    private TextView tvAverageTime;

    // Card type layouts
    private LinearLayout layoutBasicCards;
    private LinearLayout layoutMultipleChoiceCards;
    private LinearLayout layoutFillBlankCards;

    // Card type statistics
    private TextView tvBasicStats;
    private TextView tvBasicAccuracy;
    private TextView tvMultipleChoiceStats;
    private TextView tvMultipleChoiceAccuracy;
    private TextView tvFillBlankStats;
    private TextView tvFillBlankAccuracy;

    // Buttons
    private AppCompatButton btnStudyAgain;
    private AppCompatButton btnBackToDeck;

    // Data
    private String deckName;
    private List<Card> studiedCards;
    private long studyStartTime;
    private long studyEndTime;
    private int totalCorrect;
    private int totalReviewed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        getDataFromIntent();
        displaySummary();
        setupClickListeners();
    }
    private void initViews() {
        tvDeckName = findViewById(R.id.tv_deck_name);
        tvTotalCards = findViewById(R.id.tv_total_cards);
        tvCorrectCount = findViewById(R.id.tv_correct_count);
        tvAccuracyRate = findViewById(R.id.tv_accuracy_rate);
        progressAccuracy = findViewById(R.id.progress_accuracy);
        tvStudyTime = findViewById(R.id.tv_study_time);
        tvAverageTime = findViewById(R.id.tv_average_time);

        layoutBasicCards = findViewById(R.id.layout_basic_cards);
        layoutMultipleChoiceCards = findViewById(R.id.layout_multiple_choice_cards);
        layoutFillBlankCards = findViewById(R.id.layout_fill_blank_cards);

        tvBasicStats = findViewById(R.id.tv_basic_stats);
        tvBasicAccuracy = findViewById(R.id.tv_basic_accuracy);
        tvMultipleChoiceStats = findViewById(R.id.tv_multiple_choice_stats);
        tvMultipleChoiceAccuracy = findViewById(R.id.tv_multiple_choice_accuracy);
        tvFillBlankStats = findViewById(R.id.tv_fill_blank_stats);
        tvFillBlankAccuracy = findViewById(R.id.tv_fill_blank_accuracy);

        btnStudyAgain = findViewById(R.id.btn_study_again);
        btnBackToDeck = findViewById(R.id.btn_back_to_deck);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        deckName = intent.getStringExtra("deck_name");
        studiedCards = (List<Card>) intent.getSerializableExtra("studied_cards");
        studyStartTime = intent.getLongExtra("study_start_time", 0);
        studyEndTime = intent.getLongExtra("study_end_time", System.currentTimeMillis());
        totalCorrect = intent.getIntExtra("total_correct", 0);
        totalReviewed = intent.getIntExtra("total_reviewed", 0);
    }
    private void displaySummary() {
        // Set deck name
        tvDeckName.setText(deckName != null ? deckName : "Bộ thẻ");

        // Calculate overall statistics
        int totalCards = studiedCards != null ? studiedCards.size() : 0;
        double accuracyRate = totalReviewed > 0 ? (double) totalCorrect / totalReviewed * 100 : 0;

        // Display overall statistics
        tvTotalCards.setText(String.valueOf(totalCards));
        tvCorrectCount.setText(String.valueOf(totalCorrect));
        tvAccuracyRate.setText(String.format(Locale.getDefault(), "%.1f%%", accuracyRate));
        progressAccuracy.setProgress((int) accuracyRate);

        // Calculate and display study time
        displayStudyTime();

        // Display card type breakdown
        displayCardTypeBreakdown();
    }

    @SuppressLint("SetTextI18n")
    private void displayStudyTime() {
        long studyDuration = studyEndTime - studyStartTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(studyDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(studyDuration) % 60;

        tvStudyTime.setText(String.format(Locale.getDefault(), "%d phút %d giây", minutes, seconds));

        // Calculate average time per card
        if (totalReviewed > 0) {
            double averageSeconds = (double) studyDuration / 1000 / totalReviewed;
            tvAverageTime.setText(String.format(Locale.getDefault(), "~%.1fs/thẻ", averageSeconds));
        } else {
            tvAverageTime.setText("~0s/thẻ");
        }
    }

    private void displayCardTypeBreakdown() {
        if (studiedCards == null || studiedCards.isEmpty()) {
            return;
        }

        // Count cards by type
        int basicCount = 0, basicCorrect = 0, basicReviewed = 0;
        int multipleChoiceCount = 0, multipleChoiceCorrect = 0, multipleChoiceReviewed = 0;
        int fillBlankCount = 0, fillBlankCorrect = 0, fillBlankReviewed = 0;

        for (Card card : studiedCards) {
            switch (card.getType()) {
                case BASIC:
                    basicCount++;
                    basicCorrect += card.getCorrectCount();
                    basicReviewed += card.getReviewCount();
                    break;
                case MULTIPLE_CHOICE:
                    multipleChoiceCount++;
                    multipleChoiceCorrect += card.getCorrectCount();
                    multipleChoiceReviewed += card.getReviewCount();
                    break;
                case FILL_IN_BLANK:
                    fillBlankCount++;
                    fillBlankCorrect += card.getCorrectCount();
                    fillBlankReviewed += card.getReviewCount();
                    break;
            }
        }

        // Display basic cards statistics
        if (basicCount > 0) {
            layoutBasicCards.setVisibility(View.VISIBLE);
            double basicAccuracy = basicReviewed > 0 ? (double) basicCorrect / basicReviewed * 100 : 0;
            tvBasicStats.setText(String.format(Locale.getDefault(),
                    "%d thẻ • %.1f%% chính xác", basicCount, basicAccuracy));
            tvBasicAccuracy.setText(String.format(Locale.getDefault(), "%.1f%%", basicAccuracy));
        }

        // Display multiple choice cards statistics
        if (multipleChoiceCount > 0) {
            layoutMultipleChoiceCards.setVisibility(View.VISIBLE);
            double multipleChoiceAccuracy = multipleChoiceReviewed > 0 ?
                    (double) multipleChoiceCorrect / multipleChoiceReviewed * 100 : 0;
            tvMultipleChoiceStats.setText(String.format(Locale.getDefault(),
                    "%d thẻ • %.1f%% chính xác", multipleChoiceCount, multipleChoiceAccuracy));
            tvMultipleChoiceAccuracy.setText(String.format(Locale.getDefault(), "%.1f%%", multipleChoiceAccuracy));
        }

        // Display fill in blank cards statistics
        if (fillBlankCount > 0) {
            layoutFillBlankCards.setVisibility(View.VISIBLE);
            double fillBlankAccuracy = fillBlankReviewed > 0 ?
                    (double) fillBlankCorrect / fillBlankReviewed * 100 : 0;
            tvFillBlankStats.setText(String.format(Locale.getDefault(),
                    "%d thẻ • %.1f%% chính xác", fillBlankCount, fillBlankAccuracy));
            tvFillBlankAccuracy.setText(String.format(Locale.getDefault(), "%.1f%%", fillBlankAccuracy));
        }
    }

    private void setupClickListeners() {
        btnStudyAgain.setOnClickListener(v -> {
            // Restart study session
//            Intent intent = new Intent();
//            intent.putExtra("action", "study_again");
//            finish();
        });
        btnBackToDeck.setOnClickListener(v -> {
            // Go back to deck
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
//            finish();
        });
    }
    public static Intent createIntent(android.content.Context context, String deckName,
                                      List<Card> studiedCards, long studyStartTime,
                                      int totalCorrect, int totalReviewed) {
        Intent intent = new Intent(context, StudySummaryActivity.class);
        intent.putExtra("deck_name", deckName);
        intent.putExtra("studied_cards", (java.io.Serializable) studiedCards);
        intent.putExtra("study_start_time", studyStartTime);
        intent.putExtra("study_end_time", System.currentTimeMillis());
        intent.putExtra("total_correct", totalCorrect);
        intent.putExtra("total_reviewed", totalReviewed);
        return intent;
    }
}