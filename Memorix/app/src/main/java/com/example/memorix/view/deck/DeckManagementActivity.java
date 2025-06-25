package com.example.memorix.view.deck;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.model.Card;
import com.example.memorix.model.CardType;
import com.example.memorix.model.Deck;
import com.example.memorix.view.deck.adapter.CardAdapter;
import com.example.memorix.view.deck.card.AddCardActivity;
import com.example.memorix.view.deck.card.EditCardActivity;
import com.example.memorix.view.flashcardstudy.FlashcardBasicStudyActivity;
import com.example.memorix.view.flashcardstudy.FlashcardFillBlankStudyActivity;
import com.example.memorix.view.flashcardstudy.FlashcardMultipleChoiceStudyActivity;
import com.example.memorix.viewmodel.DeckManagementViewModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

public class DeckManagementActivity extends AppCompatActivity implements CardAdapter.OnCardActionListener{
    private TextView tvDeckName, tvDeckDescription, tvTotalCards;
    private Button btnAddCard, btnStudyDeck;
    private Spinner spinnerCardType;
    private RecyclerView recyclerViewCards;
    private CardAdapter cardAdapter;
    private List<Card> allCards;
    private List<Card> filteredCards;
    private DeckManagementViewModel deckManagementViewModel;
    private long deckId;
    private String cachedAuthToken;
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

        // Lấy deck ID từ Intent
        deckId = getIntent().getLongExtra("deck_id", -1);
        if (deckId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin deck", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cache token một lần duy nhất khi khởi tạo
        cachedAuthToken = getAuthToken();
        if (cachedAuthToken == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        deckManagementViewModel = new ViewModelProvider(this).get(DeckManagementViewModel.class);

        initViews();
        setupRecyclerView();
        setupSpinner();
        setupListeners();
        setupObservers();

        // Load deck data and flashcards from API
        deckManagementViewModel.loadDeckById(deckId, cachedAuthToken);
        deckManagementViewModel.loadFlashcardsByDeck(deckId, cachedAuthToken);
    }

    private String getAuthToken() {
        if (cachedAuthToken != null) {
            return cachedAuthToken;
        }
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        cachedAuthToken = prefs.getString("access_token", null);
        return cachedAuthToken;
    }
    private void setupObservers() {
        // Observe deck detail data
        deckManagementViewModel.getDeckDetail().observe(this, deck -> {
            if (deck != null) {
                displayDeckInfo(deck);
            }
        });

        // Observe loading state
        deckManagementViewModel.getLoadingState().observe(this, isLoading -> {
            // Hiển thị/ẩn loading indicator nếu có
            // Ví dụ: progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        deckManagementViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Observe deck detail data from API
        deckManagementViewModel.getDeckDetail().observe(this, deck -> {
            if (deck != null) {
                displayDeckInfo(deck);
            }
        });

        // Observe error messages
        deckManagementViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                // Clear error message sau khi hiển thị
                deckManagementViewModel.clearErrorMessage();
            }
        });

        // Observe deck detail data
        deckManagementViewModel.getDeckDetail().observe(this, deck -> {
            if (deck != null) {
                displayDeckInfo(deck);
            }
        });

        // Observe flashcards data from API
        deckManagementViewModel.getFlashcards().observe(this, cards -> {
            if (cards != null) {
                allCards = new ArrayList<>(cards);
                updateCardCount();
                filterCards(spinnerCardType.getSelectedItemPosition());
            }
        });

        // Observe loading state
        deckManagementViewModel.getLoadingState().observe(this, isLoading -> {
            // Hiển thị/ẩn loading indicator nếu có
            // Ví dụ: progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        deckManagementViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                // Clear error message sau khi hiển thị
                deckManagementViewModel.clearErrorMessage();
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void displayDeckInfo(Deck deck) {
        tvDeckName.setText(deck.getName());
        tvDeckDescription.setText(deck.getDescription());

        // Update card count - will be updated when flashcards are loaded
        updateCardCount();
    }

    @SuppressLint("SetTextI18n")
    private void updateCardCount() {
        if (allCards != null) {
            tvTotalCards.setText(String.valueOf(allCards.size()));
        }
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
        cardAdapter = new CardAdapter(filteredCards, this);
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
        intent.putExtra("deck_id", deckId);
        intent.putExtra("card_count", allCards.size());
        intent.putExtra("auth_token", cachedAuthToken); // Truyền cached token
        startActivity(intent);
    }
    private void startMultipleChoiceStudy() {
        Intent intent = new Intent(this, FlashcardMultipleChoiceStudyActivity.class);
        intent.putExtra("deck_id", deckId);
        intent.putExtra("card_count", allCards.size());
        intent.putExtra("auth_token", cachedAuthToken); // Truyền cached token
        startActivity(intent);
    }
    private void startFillBlankStudy() {
        Intent intent = new Intent(this, FlashcardFillBlankStudyActivity.class);
        intent.putExtra("deck_id", deckId);
        intent.putExtra("card_count", allCards.size());
        intent.putExtra("auth_token", cachedAuthToken); // Truyền cached token
        startActivity(intent);
    }

    private void addSampleCard() {
        Intent intent = new Intent(this, AddCardActivity.class);
        intent.putExtra("deck_id", deckId);
//        intent.putExtra("deck_id", deckId);
        intent.putExtra("auth_token", cachedAuthToken); // Truyền cached token
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
                    if (card.getCardType() == CardType.BASIC) {
                        filteredCards.add(card);
                    }
                }
                break;
            case 2: // Trắc nghiệm
                for (Card card : allCards) {
                    if (card.getCardType() == CardType.MULTIPLE_CHOICE) {
                        filteredCards.add(card);
                    }
                }
                break;
            case 3: // Điền từ
                for (Card card : allCards) {
                    if (card.getCardType() == CardType.FILL_IN_BLANK) {
                        filteredCards.add(card);
                    }
                }
                break;
        }

        cardAdapter.notifyDataSetChanged();
    }
    @Override
    public void onCardAction(Card card, String action) {
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
        Intent intent = new Intent(this, EditCardActivity.class);
        intent.putExtra("card_id", card.getFlashcardId());
        intent.putExtra("deck_id", deckId);
        startActivity(intent);
    }
    @SuppressLint("SetTextI18n")
    private void showCardDetail(Card card) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_card_detail, null);

        // Find views
        TextView tvCardType = dialogView.findViewById(R.id.tvCardType);
        TextView tvQuestion = dialogView.findViewById(R.id.tvQuestion);
        TextView tvAnswerLabel = dialogView.findViewById(R.id.tvAnswerLabel);
        TextView tvAnswer = dialogView.findViewById(R.id.tvAnswer);
        LinearLayout layoutOptions = dialogView.findViewById(R.id.layoutOptions);
        LinearLayout layoutOptionsList = dialogView.findViewById(R.id.layoutOptionsList);
        AppCompatButton btnClose = dialogView.findViewById(R.id.btnClose);
        AppCompatButton btnEdit = dialogView.findViewById(R.id.btnEdit);

        // Set card type and its color
        String cardTypeText;
        int cardTypeColor;
        switch (card.getCardType()) {
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

        tvCardType.setText(cardTypeText);
        tvCardType.getBackground().setTint(cardTypeColor);

        // Handle different card types based on JsonObject content
        JsonObject content = card.getContent();
        if (content != null) {
            switch (card.getCardType()) {
                case BASIC:
                    // For basic cards: {"front": "question", "back": "answer"}
                    String front = content.has("front") ? content.get("front").getAsString() : "";
                    String back = content.has("back") ? content.get("back").getAsString() : "";

                    tvQuestion.setText(front);
                    tvAnswerLabel.setText("Mặt sau");
                    tvAnswer.setText(back);
                    layoutOptions.setVisibility(View.GONE);
                    break;

                case MULTIPLE_CHOICE:
                    // For multiple choice cards: {"question": "...", "options": [...], "answer": "..."}
                    String question = content.has("question") ? content.get("question").getAsString() : "";
                    String correctAnswer = content.has("answer") ? content.get("answer").getAsString() : "";

                    tvQuestion.setText(question);
                    tvAnswerLabel.setText("Đáp án đúng");
                    tvAnswer.setText(correctAnswer);

                    // Show options
                    layoutOptions.setVisibility(View.VISIBLE);
                    layoutOptionsList.removeAllViews();

                    if (content.has("options")) {
                        JsonArray optionsArray = content.getAsJsonArray("options");
                        for (int i = 0; i < optionsArray.size(); i++) {
                            String option = optionsArray.get(i).getAsString();

                            // Create option view
                            LinearLayout optionLayout = getLinearLayout();

                            // Option letter (A, B, C, D)
                            TextView tvOptionLetter = new TextView(this);
                            tvOptionLetter.setText(String.valueOf((char)('A' + i)));
                            tvOptionLetter.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                            tvOptionLetter.setTextSize(14);
                            tvOptionLetter.setTypeface(tvOptionLetter.getTypeface(), android.graphics.Typeface.BOLD);
                            tvOptionLetter.setPadding(0, 0, 16, 0);

                            // Option text
                            TextView tvOptionText = new TextView(this);
                            tvOptionText.setText(option);
                            tvOptionText.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                            tvOptionText.setTextSize(14);
                            tvOptionText.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                            // Highlight correct answer
                            if (option.equals(correctAnswer)) {
                                optionLayout.getBackground().setTint(ContextCompat.getColor(this, R.color.primary_light_color));
                                tvOptionLetter.setTextColor(ContextCompat.getColor(this, R.color.primary_dark_color));
                                tvOptionText.setTextColor(ContextCompat.getColor(this, R.color.primary_dark_color));
                                tvOptionText.setTypeface(tvOptionText.getTypeface(), android.graphics.Typeface.BOLD);

                                // Add checkmark
                                TextView tvCheckmark = new TextView(this);
                                tvCheckmark.setText("✓");
                                tvCheckmark.setTextColor(ContextCompat.getColor(this, R.color.secondary_color));
                                tvCheckmark.setTextSize(16);
                                tvCheckmark.setTypeface(tvCheckmark.getTypeface(), android.graphics.Typeface.BOLD);
                                optionLayout.addView(tvCheckmark);
                            }

                            optionLayout.addView(tvOptionLetter);
                            optionLayout.addView(tvOptionText);
                            layoutOptionsList.addView(optionLayout);
                        }
                    }
                    break;

                case FILL_IN_BLANK:
                    // For fill in blank cards: {"text": "question", "answer": "correct_answer"}
                    String text = content.has("text") ? content.get("text").getAsString() : "";
                    String fillAnswer = content.has("answer") ? content.get("answer").getAsString() : "";

                    tvQuestion.setText(text);
                    tvAnswerLabel.setText("Từ cần điền");
                    tvAnswer.setText(fillAnswer);
                    layoutOptions.setVisibility(View.GONE);
                    break;

                default:
                    tvQuestion.setText("Không có nội dung");
                    tvAnswerLabel.setText("Không có đáp án");
                    tvAnswer.setText("");
                    layoutOptions.setVisibility(View.GONE);
                    break;
            }
        } else {
            // Handle case when content is null
            tvQuestion.setText("Không có nội dung");
            tvAnswerLabel.setText("Không có đáp án");
            tvAnswer.setText("");
            layoutOptions.setVisibility(View.GONE);
        }

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set button listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, EditCardActivity.class);
            intent.putExtra("card_id", card.getFlashcardId());
            intent.putExtra("deck_id", deckId);
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
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_card, null);

        // Find views
        TextView tvCardTypeBadge = dialogView.findViewById(R.id.tvCardTypeBadge);
        TextView tvCardQuestion = dialogView.findViewById(R.id.tvCardQuestion);
        TextView tvCardAnswer = dialogView.findViewById(R.id.tvCardAnswer);
        AppCompatButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        AppCompatButton btnDelete = dialogView.findViewById(R.id.btnDelete);

        // Set card type and its color
        String cardTypeText;
        int cardTypeColor;
        switch (card.getCardType()) {
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

        // Set card content based on JsonObject content
        JsonObject content = card.getContent();
        if (content != null) {
            switch (card.getCardType()) {
                case BASIC:
                    // For basic cards: {"front": "question", "back": "answer"}
                    String front = content.has("front") ? content.get("front").getAsString() : "";
                    String back = content.has("back") ? content.get("back").getAsString() : "";

                    tvCardQuestion.setText(front);
                    tvCardAnswer.setText(back);
                    break;

                case MULTIPLE_CHOICE:
                    // For multiple choice cards: {"question": "...", "options": [...], "answer": "..."}
                    String question = content.has("question") ? content.get("question").getAsString() : "";
                    String correctAnswer = content.has("answer") ? content.get("answer").getAsString() : "";

                    tvCardQuestion.setText(question);
                    tvCardAnswer.setText(correctAnswer);
                    break;

                case FILL_IN_BLANK:
                    // For fill in blank cards: {"text": "question", "answer": "correct_answer"}
                    String text = content.has("text") ? content.get("text").getAsString() : "";
                    String fillAnswer = content.has("answer") ? content.get("answer").getAsString() : "";

                    tvCardQuestion.setText(text);
                    tvCardAnswer.setText(fillAnswer);
                    break;

                default:
                    tvCardQuestion.setText("Không có nội dung");
                    tvCardAnswer.setText("");
                    break;
            }
        } else {
            // Handle case when content is null
            tvCardQuestion.setText("Không có nội dung");
            tvCardAnswer.setText("");
        }

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Set button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            // Show loading state (optional)
            btnDelete.setEnabled(false);
            btnDelete.setText("Đang xóa...");

            // Call ViewModel to delete flashcard via API
            deckManagementViewModel.deleteFlashcard(card.getFlashcardId(), cachedAuthToken);

            // Observe delete result
            deckManagementViewModel.getDeleteResult().observe(this, isSuccess -> {
                if (isSuccess != null) {
                    if (isSuccess) {
                        // Success: Remove card from local list and update UI
                        allCards.remove(card);
                        updateCardCount();
                        filterCards(spinnerCardType.getSelectedItemPosition());

                        // Show success message
                        String questionText = "";
                        if (content != null) {
                            switch (card.getCardType()) {
                                case BASIC:
                                    questionText = content.has("front") ? content.get("front").getAsString() : "";
                                    break;
                                case MULTIPLE_CHOICE:
                                    questionText = content.has("question") ? content.get("question").getAsString() : "";
                                    break;
                                case FILL_IN_BLANK:
                                    questionText = content.has("text") ? content.get("text").getAsString() : "";
                                    break;
                            }
                        }

                        Toast.makeText(this, "Đã xóa thẻ: " + questionText, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        // Error: Re-enable button and show error
                        btnDelete.setEnabled(true);
                        btnDelete.setText("Xóa");
                        Toast.makeText(this, "Không thể xóa thẻ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }

                    // Clear delete result to avoid triggering observer again
                    deckManagementViewModel.clearDeleteResult();
                }
            });
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
    private void refreshDeckData() {
        if (cachedAuthToken != null) {
            deckManagementViewModel.loadDeckById(deckId, cachedAuthToken);
            deckManagementViewModel.loadFlashcardsByDeck(deckId, cachedAuthToken);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh deck data khi quay lại Activity
        if (cachedAuthToken != null) {
            refreshDeckData();
        }
    }
}