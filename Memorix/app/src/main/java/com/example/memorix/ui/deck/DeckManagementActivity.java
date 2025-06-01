package com.example.memorix.ui.deck;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.data.Card;
import com.example.memorix.data.CardType;
import com.example.memorix.ui.deck.adapter.CardAdapter;
import com.example.memorix.ui.deck.card.AddCardActivity;
import com.example.memorix.ui.deck.card.EditCardActivity;
import com.example.memorix.ui.flashcardstudy.FlashcardBasicStudyActivity;
import com.example.memorix.ui.flashcardstudy.FlashcardFillBlankStudyActivity;
import com.example.memorix.ui.flashcardstudy.FlashcardMultipleChoiceStudyActivity;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Typeface;
import java.util.List;
import java.util.Objects;

public class DeckManagementActivity extends AppCompatActivity {
    private TextView tvDeckName, tvDeckDescription, tvTotalCards;
    private Button btnAddCard, btnStudyDeck;
    private Spinner spinnerCardType;
    private RecyclerView recyclerViewCards;
    private CardAdapter cardAdapter;

    private List<Card> allCards;
    private List<Card> filteredCards;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deck_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupRecyclerView();
        setupSpinner();
        setupListeners();
        loadSampleData();
    }

    private void initViews() {
        tvDeckName = findViewById(R.id.tvDeckName);
        tvDeckDescription = findViewById(R.id.tvDeckDescription);
        tvTotalCards = findViewById(R.id.tvTotalCards);
        btnAddCard = findViewById(R.id.btnAddCard);
        btnStudyDeck = findViewById(R.id.btnStudyDeck);
        spinnerCardType = findViewById(R.id.spinnerCardType);
        recyclerViewCards = findViewById(R.id.recyclerViewCards);
    }

    private void setupRecyclerView() {
        filteredCards = new ArrayList<>();
        cardAdapter = new CardAdapter(filteredCards, this::onCardAction);
        recyclerViewCards.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCards.setAdapter(cardAdapter);
    }

    private void setupSpinner() {
        String[] cardTypes = {"Tất cả", "2 Mặt", "Trắc nghiệm", "Điền từ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cardTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCardType.setAdapter(adapter);

        spinnerCardType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCards(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        btnAddCard.setOnClickListener(v -> {
            // Demo: Tạo thẻ mới và thêm vào danh sách
            addSampleCard();
        });

        btnStudyDeck.setOnClickListener(v -> {
            // Demo: Hiển thị toast
            showStudyOptionsDialog();
        });
    }
    private void showStudyOptionsDialog() {
        // Kiểm tra xem có thẻ nào để học không
        if (allCards.isEmpty()) {
            Toast.makeText(this, "Chưa có thẻ nào để học!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_study_options, null);
        builder.setView(dialogView);

        // Tạo dialog
        AlertDialog dialog = builder.create();

        // Set dialog properties
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        // Lấy references đến các view
        LinearLayout layoutTwoSided = dialogView.findViewById(R.id.layoutTwoSided);
        LinearLayout layoutMultipleChoice = dialogView.findViewById(R.id.layoutMultipleChoice);
        LinearLayout layoutFillBlank = dialogView.findViewById(R.id.layoutFillBlank);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Set click listeners
        layoutTwoSided.setOnClickListener(v -> {
            dialog.dismiss();
            startFlashcardBasicStudy();
        });

        layoutMultipleChoice.setOnClickListener(v -> {
            dialog.dismiss();
            startMultipleChoiceStudy();
        });

        layoutFillBlank.setOnClickListener(v -> {
            dialog.dismiss();
            startFillBlankStudy();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Hiển thị dialog
        dialog.show();
    }

    private void startFlashcardBasicStudy() {
        Intent intent = new Intent(this, FlashcardBasicStudyActivity.class);
        // Truyền dữ liệu cần thiết qua intent
        intent.putExtra("card_count", allCards.size());
        // Có thể truyền thêm dữ liệu khác nếu cần
        startActivity(intent);
    }

    private void startMultipleChoiceStudy() {
        Intent intent = new Intent(this, FlashcardMultipleChoiceStudyActivity.class);
        // Truyền dữ liệu cần thiết qua intent
        intent.putExtra("card_count", allCards.size());
        // Có thể truyền thêm dữ liệu khác nếu cần
        startActivity(intent);
    }

    private void startFillBlankStudy() {
        Intent intent = new Intent(this, FlashcardFillBlankStudyActivity.class);
        // Truyền dữ liệu cần thiết qua intent
        intent.putExtra("card_count", allCards.size());
        // Có thể truyền thêm dữ liệu khác nếu cần
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void loadSampleData() {
        // Tạo dữ liệu mẫu cho deck
        tvDeckName.setText("Tiếng Anh Cơ Bản");
        tvDeckDescription.setText("Học từ vựng tiếng Anh cơ bản cho người mới bắt đầu");

        // Tạo danh sách thẻ mẫu
        allCards = new ArrayList<>();

        // Thẻ 2 mặt
        Card card1 = new Card("1", "deck1", "Hello", "Xin chào");
        card1.setReviewCount(5);
        card1.setCorrectCount(4);
        allCards.add(card1);

        Card card2 = new Card("2", "deck1", "Good morning", "Chào buổi sáng");
        card2.setReviewCount(3);
        card2.setCorrectCount(3);
        allCards.add(card2);

        // Thẻ trắc nghiệm
        List<String> options1 = new ArrayList<>();
        options1.add("Tôi là");
        options1.add("Bạn là");
        options1.add("Anh ấy là");
        options1.add("Cô ấy là");
        Card card3 = new Card("3", "deck1", "I am", options1, "Tôi là");
        card3.setReviewCount(2);
        card3.setCorrectCount(1);
        allCards.add(card3);

        List<String> options2 = new ArrayList<>();
        options2.add("Đi");
        options2.add("Đến");
        options2.add("Về");
        options2.add("Tới");
        Card card4 = new Card("4", "deck1", "Go", options2, "Đi");
        card4.setReviewCount(4);
        card4.setCorrectCount(4);
        allCards.add(card4);

        // Thẻ điền từ
        Card card5 = new Card("5", "deck1", "I ___ a student", "am", CardType.FILL_IN_BLANK);
        card5.setReviewCount(6);
        card5.setCorrectCount(5);
        allCards.add(card5);

        Card card6 = new Card("6", "deck1", "She ___ to school", "goes", CardType.FILL_IN_BLANK);
        card6.setReviewCount(1);
        card6.setCorrectCount(0);
        allCards.add(card6);

        // Cập nhật UI
        tvTotalCards.setText(String.valueOf(allCards.size()));
        filterCards(0); // Hiển thị tất cả thẻ
    }

    private void addSampleCard() {
        Intent intent = new Intent(this, AddCardActivity.class);
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterCards(int filterType) {
        filteredCards.clear();

        switch (filterType) {
            case 0: // Tất cả
                filteredCards.addAll(allCards);
                break;
            case 1: // 2 Mặt
                for (Card card : allCards) {
                    if (card.getType() == CardType.BASIC) {
                        filteredCards.add(card);
                    }
                }
                break;
            case 2: // Trắc nghiệm
                for (Card card : allCards) {
                    if (card.getType() == CardType.MULTIPLE_CHOICE) {
                        filteredCards.add(card);
                    }
                }
                break;
            case 3: // Điền từ
                for (Card card : allCards) {
                    if (card.getType() == CardType.FILL_IN_BLANK) {
                        filteredCards.add(card);
                    }
                }
                break;
        }

        cardAdapter.notifyDataSetChanged();
    }

    private void onCardAction(Card card, String action) {
        switch (action) {
            case "edit":
                // Demo: Hiển thị thông tin thẻ
                showCardInfo(card);
                break;

            case "delete":
                // Demo: Xóa thẻ khỏi danh sách
                showDeleteConfirmDialog(card);
                break;

            case "view":
                // Demo: Hiển thị chi tiết thẻ
                showCardDetail(card);
                break;
        }
    }

    private void showCardInfo(Card card) {
//        @SuppressLint("DefaultLocale") String info = "Loại: " + card.getType().getDisplayName() + "\n" +
//                "Câu hỏi: " + card.getQuestion() + "\n" +
//                "Số lần ôn: " + card.getReviewCount() + "\n" +
//                "Tỷ lệ đúng: " + String.format("%.1f", card.getAccuracyRate()) + "%";
//
//        new AlertDialog.Builder(this)
//                .setTitle("Thông tin thẻ")
//                .setMessage(info)
//                .setPositiveButton("OK", null)
//                .show();
        Intent intent = new Intent(this, EditCardActivity.class);
        intent.putExtra("card", card);
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void showCardDetail(Card card) {
//        new AlertDialog.Builder(this)
//                .setTitle("Chi tiết thẻ")
//                .setMessage(card.getDisplayContent())
//                .setPositiveButton("Chỉnh sửa", (dialog, which) -> {
//                    Intent intent = new Intent(this, EditCardActivity.class);
//                    intent.putExtra("card", card);
//                    startActivity(intent);
//                })
//                .setNegativeButton("Đóng", null)
//                .show();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_card_detail, null);

        // Find views
        TextView tvCardType = dialogView.findViewById(R.id.tvCardType);
        TextView tvQuestion = dialogView.findViewById(R.id.tvQuestion);
        TextView tvAnswerLabel = dialogView.findViewById(R.id.tvAnswerLabel);
        TextView tvAnswer = dialogView.findViewById(R.id.tvAnswer);
        LinearLayout layoutOptions = dialogView.findViewById(R.id.layoutOptions);
        LinearLayout layoutOptionsList = dialogView.findViewById(R.id.layoutOptionsList);
        TextView tvReviewCount = dialogView.findViewById(R.id.tvReviewCount);
        TextView tvCorrectCount = dialogView.findViewById(R.id.tvCorrectCount);
        TextView tvAccuracy = dialogView.findViewById(R.id.tvAccuracy);
        AppCompatButton btnClose = dialogView.findViewById(R.id.btnClose);
        AppCompatButton btnEdit = dialogView.findViewById(R.id.btnEdit);

        // Set card type and its color
        String cardTypeText;
        int cardTypeColor;
        switch (card.getType()) {
            case BASIC:
                cardTypeText = "2 Mặt";
                cardTypeColor = getResources().getColor(R.color.primary_color);
                break;
            case MULTIPLE_CHOICE:
                cardTypeText = "Trắc nghiệm";
                cardTypeColor = getResources().getColor(R.color.secondary_color);
                break;
            case FILL_IN_BLANK:
                cardTypeText = "Điền từ";
                cardTypeColor = getResources().getColor(R.color.accent_color);
                break;
            default:
                cardTypeText = "Không xác định";
                cardTypeColor = getResources().getColor(R.color.secondary_text_color);
                break;
        }

        tvCardType.setText(cardTypeText);
        tvCardType.getBackground().setTint(cardTypeColor);

        // Set question
        tvQuestion.setText(card.getQuestion());

        // Handle different card types
        if (card.getType() == CardType.MULTIPLE_CHOICE) {
            // For multiple choice cards
            tvAnswerLabel.setText("Đáp án đúng");
            tvAnswer.setText(card.getCorrectAnswer());

            // Show options
            layoutOptions.setVisibility(View.VISIBLE);
            layoutOptionsList.removeAllViews();

            List<String> options = card.getOptions();
            if (options != null) {
                for (int i = 0; i < options.size(); i++) {
                    String option = options.get(i);

                    // Create option view
                    LinearLayout optionLayout = getLinearLayout();

                    // Option letter (A, B, C, D)
                    TextView tvOptionLetter = new TextView(this);
                    tvOptionLetter.setText(String.valueOf((char)('A' + i)));
                    tvOptionLetter.setTextColor(getResources().getColor(R.color.text_color));
                    tvOptionLetter.setTextSize(14);
                    tvOptionLetter.setTypeface(tvOptionLetter.getTypeface(), Typeface.BOLD);
                    tvOptionLetter.setPadding(0, 0, 16, 0);

                    // Option text
                    TextView tvOptionText = new TextView(this);
                    tvOptionText.setText(option);
                    tvOptionText.setTextColor(getResources().getColor(R.color.text_color));
                    tvOptionText.setTextSize(14);
                    tvOptionText.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    // Highlight correct answer
                    if (option.equals(card.getCorrectAnswer())) {
                        optionLayout.getBackground().setTint(getResources().getColor(R.color.primary_light_color));
                        tvOptionLetter.setTextColor(getResources().getColor(R.color.primary_dark_color));
                        tvOptionText.setTextColor(getResources().getColor(R.color.primary_dark_color));
                        tvOptionText.setTypeface(tvOptionText.getTypeface(), Typeface.BOLD);

                        // Add checkmark
                        TextView tvCheckmark = new TextView(this);
                        tvCheckmark.setText("✓");
                        tvCheckmark.setTextColor(getResources().getColor(R.color.secondary_color));
                        tvCheckmark.setTextSize(16);
                        tvCheckmark.setTypeface(tvCheckmark.getTypeface(), Typeface.BOLD);
                        optionLayout.addView(tvCheckmark);
                    }

                    optionLayout.addView(tvOptionLetter);
                    optionLayout.addView(tvOptionText);
                    layoutOptionsList.addView(optionLayout);
                }
            }
        } else if (card.getType() == CardType.FILL_IN_BLANK) {
            // For fill in blank cards
            tvAnswerLabel.setText("Từ cần điền");
            tvAnswer.setText(card.getCorrectAnswer());
            layoutOptions.setVisibility(View.GONE);
        } else {
            // For basic cards
            tvAnswerLabel.setText("Mặt sau");
            tvAnswer.setText(card.getAnswer());
            layoutOptions.setVisibility(View.GONE);
        }

        // Set statistics
        tvReviewCount.setText(String.valueOf(card.getReviewCount()));
        tvCorrectCount.setText(String.valueOf(card.getCorrectCount()));

        // Calculate and set accuracy
        int accuracy = 0;
        if (card.getReviewCount() > 0) {
            accuracy = (int) Math.round((double) card.getCorrectCount() / card.getReviewCount() * 100);
        }
        tvAccuracy.setText(accuracy + "%");

        // Set accuracy color based on percentage
        int accuracyColor;
        if (accuracy >= 80) {
            accuracyColor = getResources().getColor(R.color.secondary_color);
        } else if (accuracy >= 60) {
            accuracyColor = getResources().getColor(R.color.accent_color);
        } else {
            accuracyColor = getResources().getColor(R.color.secondary_text_color);
        }
        tvAccuracy.setTextColor(accuracyColor);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set button listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, EditCardActivity.class);
            intent.putExtra("card", card);
            startActivity(intent);
        });

        // Show dialog
        dialog.show();

        // Optional: Set dialog window properties for better appearance
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }
    }

    @NonNull
    private LinearLayout getLinearLayout() {
        LinearLayout optionLayout = new LinearLayout(this);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setPadding(16, 12, 16, 12);
        optionLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Set background
        optionLayout.setBackgroundResource(R.drawable.bg_option_item);

        // Add margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 8);
        optionLayout.setLayoutParams(layoutParams);
        return optionLayout;
    }

    @SuppressLint("SetTextI18n")
    private void showDeleteConfirmDialog(Card card) {
//        new AlertDialog.Builder(this)
//                .setTitle("Xóa thẻ")
//                .setMessage("Bạn có chắc chắn muốn xóa thẻ này?\n\n" + card.getQuestion())
//                .setPositiveButton("Xóa", (dialog, which) -> {
//                    // Xóa thẻ khỏi danh sách
//                    allCards.remove(card);
//                    tvTotalCards.setText(String.valueOf(allCards.size()));
//                    filterCards(spinnerCardType.getSelectedItemPosition());
//                    Toast.makeText(this, "Đã xóa thẻ!", Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton("Hủy", null)
//                .show();

        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_card, null);

        // Find views
        TextView tvCardTypeBadge = dialogView.findViewById(R.id.tvCardTypeBadge);
        TextView tvCardQuestion = dialogView.findViewById(R.id.tvCardQuestion);
        TextView tvCardAnswer = dialogView.findViewById(R.id.tvCardAnswer);
        TextView tvReviewCountValue = dialogView.findViewById(R.id.tvReviewCountValue);
        TextView tvCorrectCountValue = dialogView.findViewById(R.id.tvCorrectCountValue);
        TextView tvAccuracyValue = dialogView.findViewById(R.id.tvAccuracyValue);
        AppCompatButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        AppCompatButton btnDelete = dialogView.findViewById(R.id.btnDelete);

        // Set card type and its color
        String cardTypeText;
        int cardTypeColor;
        switch (card.getType()) {
            case BASIC:
                cardTypeText = "2 Mặt";
                cardTypeColor = ContextCompat.getColor(this, R.color.primary_color);
                break;
            case MULTIPLE_CHOICE:
                cardTypeText = "Trắc nghiệm";
                cardTypeColor = ContextCompat.getColor(this, R.color.secondary_color);
                break;
            case FILL_IN_BLANK:
                cardTypeText = "Điền từ";
                cardTypeColor = ContextCompat.getColor(this, R.color.accent_color);
                break;
            default:
                cardTypeText = "Không xác định";
                cardTypeColor = ContextCompat.getColor(this, R.color.secondary_text_color);
                break;
        }

        tvCardTypeBadge.setText(cardTypeText);
        tvCardTypeBadge.getBackground().setTint(cardTypeColor);

        // Set card content
        tvCardQuestion.setText(card.getQuestion());

        // Set answer based on card type
        if (card.getType() == CardType.MULTIPLE_CHOICE) {
            tvCardAnswer.setText(card.getCorrectAnswer());
        } else if (card.getType() == CardType.FILL_IN_BLANK) {
            tvCardAnswer.setText(card.getCorrectAnswer());
        } else {
            tvCardAnswer.setText(card.getAnswer());
        }

        // Set statistics
        tvReviewCountValue.setText(String.valueOf(card.getReviewCount()));
        tvCorrectCountValue.setText(String.valueOf(card.getCorrectCount()));

        // Calculate and set accuracy with color
        int accuracy = 0;
        if (card.getReviewCount() > 0) {
            accuracy = (int) Math.round((double) card.getCorrectCount() / card.getReviewCount() * 100);
        }
        tvAccuracyValue.setText(accuracy + "%");

        // Set accuracy color based on percentage
        int accuracyColor;
        if (accuracy >= 80) {
            accuracyColor = ContextCompat.getColor(this, R.color.secondary_color);
        } else if (accuracy >= 60) {
            accuracyColor = ContextCompat.getColor(this, R.color.accent_color);
        } else {
            accuracyColor = ContextCompat.getColor(this, R.color.secondary_text_color);
        }
        tvAccuracyValue.setTextColor(accuracyColor);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Set button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            // Delete card from list
            allCards.remove(card);
            tvTotalCards.setText(String.valueOf(allCards.size()));
            filterCards(spinnerCardType.getSelectedItemPosition());

            // Show success message
            Toast.makeText(this, "Đã xóa thẻ: " + card.getQuestion(), Toast.LENGTH_SHORT).show();

            // Dismiss dialog
            dialog.dismiss();
        });

        // Show dialog
        dialog.show();

        // Optional: Set dialog window properties for better appearance
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

            // Set dialog width (optional)
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }
}