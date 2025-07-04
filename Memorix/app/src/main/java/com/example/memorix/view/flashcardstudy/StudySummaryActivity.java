package com.example.memorix.view.flashcardstudy;

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
import com.example.memorix.model.Card;
import com.example.memorix.view.MainActivity;

import java.util.ArrayList;
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
    private TextView tvProgressPercent;


    private AppCompatButton btnBackToDeck;

    // Data
    private String deckName;
    private List<Card> studiedCards;
    private long studyStartTime;
    private long studyEndTime;
    private int totalCorrect;
    private int totalReviewed;
    private ArrayList<String> studiedCardsSummary;

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
        tvProgressPercent = findViewById(R.id.tv_progress_percent);


        btnBackToDeck = findViewById(R.id.btn_back_to_deck);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        deckName = intent.getStringExtra("deck_name");
        studiedCardsSummary = intent.getStringArrayListExtra("studied_cards");
        studyStartTime = intent.getLongExtra("study_start_time", 0);
        studyEndTime = intent.getLongExtra("study_end_time", System.currentTimeMillis());
        totalCorrect = intent.getIntExtra("total_correct", 0);
        totalReviewed = intent.getIntExtra("total_reviewed", 0);
    }
    private void displaySummary() {
        // Set deck name
        tvDeckName.setText(deckName != null ? deckName : "Bộ thẻ");

        // Calculate overall statistics
        int totalCards = studiedCardsSummary != null ? studiedCardsSummary.size() : 0;
        double accuracyRate = totalReviewed > 0 ? (double) totalCorrect / totalReviewed * 100 : 0;

        // Display overall statistics
        tvTotalCards.setText(String.valueOf(totalCards));
        tvCorrectCount.setText(String.valueOf(totalCorrect));
        tvAccuracyRate.setText(String.format(Locale.getDefault(), "%.1f%%", accuracyRate));
        progressAccuracy.setProgress((int) accuracyRate);

        int totalStudied = totalReviewed;
        int totalAvailable = studiedCardsSummary != null ? studiedCardsSummary.size() : 0;
        double progressRate = totalAvailable > 0 ? (double) totalStudied / totalAvailable * 100 : 0;

        tvProgressPercent.setText(String.format(Locale.getDefault(),
                "%d/%d thẻ (%.1f%%)", totalStudied, totalAvailable, progressRate));

        // Calculate and display study time
        displayStudyTime();

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

    private void setupClickListeners() {
        btnBackToDeck.setOnClickListener(v -> {
            // Go back to deck
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
//            finish();
        });
    }
    public static Intent createIntent(android.content.Context context, String deckName,
                                      ArrayList<String> studiedCardsSummary,
                                      long studyStartTime,
                                      int totalCorrect, int totalReviewed) {
        Intent intent = new Intent(context, StudySummaryActivity.class);
        intent.putExtra("deck_name", deckName);
        intent.putStringArrayListExtra("studied_cards", studiedCardsSummary);
        intent.putExtra("study_start_time", studyStartTime);
        intent.putExtra("study_end_time", System.currentTimeMillis());
        intent.putExtra("total_correct", totalCorrect);
        intent.putExtra("total_reviewed", totalReviewed);
        return intent;
    }
}