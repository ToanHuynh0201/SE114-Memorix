package com.example.memorix.view.deck;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "DeckManagementActivity";

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

    // Track current delete operation to prevent conflicts
    private boolean isDeletingCard = false;
    private AlertDialog currentDeleteDialog = null;

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
        setupObservers(); // SET UP OBSERVERS ONCE

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

        // Observe flashcards data from API
        deckManagementViewModel.getFlashcards().observe(this, cards -> {
            if (cards != null) {
                allCards = new ArrayList<>(cards);
                updateCardCount();
                filterCards(spinnerCardType.getSelectedItemPosition());
            }
        });

        // SINGLE DELETE RESULT OBSERVER - SET UP ONCE
        deckManagementViewModel.getDeleteResult().observe(this, isSuccess -> {
            if (isSuccess != null && isDeletingCard) {
                handleDeleteResult(isSuccess);
                // Clear delete result immediately after handling
                deckManagementViewModel.clearDeleteResult();
            }
        });

        // Observe loading state
        deckManagementViewModel.getLoadingState().observe(this, isLoading -> {
            Log.d(TAG, "Loading state: " + isLoading);
            // Hiển thị/ẩn loading indicator nếu có
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

    private void handleDeleteResult(boolean isSuccess) {
        Log.d(TAG, "Handling delete result: " + isSuccess);

        if (isSuccess) {
            Toast.makeText(this, "Đã xóa thẻ thành công", Toast.LENGTH_SHORT).show();

            // Refresh data from server để đảm bảo sync
            refreshDeckData();
        } else {
            Toast.makeText(this, "Không thể xóa thẻ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }

        // Reset delete state
        isDeletingCard = false;

        // Close dialog and reset UI
        if (currentDeleteDialog != null && currentDeleteDialog.isShowing()) {
            currentDeleteDialog.dismiss();
            currentDeleteDialog = null;
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayDeckInfo(Deck deck) {
        tvDeckName.setText(deck.getName());
        tvDeckDescription.setText(deck.getDescription());
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
        btnAddCard.setOnClickListener(v -> addSampleCard());
        btnStudyDeck.setOnClickListener(v -> showStudyOptionsDialog());
    }

    private void showStudyOptionsDialog() {
        if (allCards.isEmpty()) {
            Toast.makeText(this, "Chưa có thẻ nào để học!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_study_options, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        LinearLayout layoutTwoSided = dialogView.findViewById(R.id.layoutTwoSided);
        LinearLayout layoutMultipleChoice = dialogView.findViewById(R.id.layoutMultipleChoice);
        LinearLayout layoutFillBlank = dialogView.findViewById(R.id.layoutFillBlank);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

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
        dialog.show();
    }

    private void startFlashcardBasicStudy() {
        Intent intent = new Intent(this, FlashcardBasicStudyActivity.class);
        intent.putExtra("deck_id", deckId);
        intent.putExtra("card_count", allCards.size());
        intent.putExtra("auth_token", cachedAuthToken);
        startActivity(intent);
    }

    private void startMultipleChoiceStudy() {
        Intent intent = new Intent(this, FlashcardMultipleChoiceStudyActivity.class);
        intent.putExtra("deck_id", deckId);
        intent.putExtra("card_count", allCards.size());
        intent.putExtra("auth_token", cachedAuthToken);
        startActivity(intent);
    }

    private void startFillBlankStudy() {
        Intent intent = new Intent(this, FlashcardFillBlankStudyActivity.class);
        intent.putExtra("deck_id", deckId);
        intent.putExtra("card_count", allCards.size());
        intent.putExtra("auth_token", cachedAuthToken);
        startActivity(intent);
    }

    private void addSampleCard() {
        Intent intent = new Intent(this, AddCardActivity.class);
        intent.putExtra("deck_id", deckId);
        intent.putExtra("auth_token", cachedAuthToken);
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterCards(int filterType) {
        if (allCards == null) return;

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
        Log.d(TAG, "Card action: " + action + " for card ID: " + card.getFlashcardId());

        switch (action) {
            case "edit":
                showCardInfo(card);
                break;
            case "delete":
                showDeleteConfirmDialog(card);
                break;
            case "view":
                showCardDetail(card);
                break;
        }
    }

    private Card findCardById(long cardId) {
        if (allCards != null) {
            for (Card card : allCards) {
                if (card.getFlashcardId() == cardId) {
                    return card;
                }
            }
        }
        return null;
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
                    String front = content.has("front") ? content.get("front").getAsString() : "";
                    String back = content.has("back") ? content.get("back").getAsString() : "";
                    tvQuestion.setText(front);
                    tvAnswerLabel.setText("Mặt sau");
                    tvAnswer.setText(back);
                    layoutOptions.setVisibility(View.GONE);
                    break;

                case MULTIPLE_CHOICE:
                    String question = content.has("question") ? content.get("question").getAsString() : "";
                    String correctAnswer = content.has("answer") ? content.get("answer").getAsString() : "";
                    tvQuestion.setText(question);
                    tvAnswerLabel.setText("Đáp án đúng");
                    tvAnswer.setText(correctAnswer);

                    layoutOptions.setVisibility(View.VISIBLE);
                    layoutOptionsList.removeAllViews();

                    if (content.has("options")) {
                        JsonArray optionsArray = content.getAsJsonArray("options");
                        for (int i = 0; i < optionsArray.size(); i++) {
                            String option = optionsArray.get(i).getAsString();
                            LinearLayout optionLayout = getLinearLayout();

                            TextView tvOptionLetter = new TextView(this);
                            tvOptionLetter.setText(String.valueOf((char)('A' + i)));
                            tvOptionLetter.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                            tvOptionLetter.setTextSize(14);
                            tvOptionLetter.setTypeface(tvOptionLetter.getTypeface(), android.graphics.Typeface.BOLD);
                            tvOptionLetter.setPadding(0, 0, 16, 0);

                            TextView tvOptionText = new TextView(this);
                            tvOptionText.setText(option);
                            tvOptionText.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                            tvOptionText.setTextSize(14);
                            tvOptionText.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                            if (option.equals(correctAnswer)) {
                                optionLayout.getBackground().setTint(ContextCompat.getColor(this, R.color.primary_light_color));
                                tvOptionLetter.setTextColor(ContextCompat.getColor(this, R.color.primary_dark_color));
                                tvOptionText.setTextColor(ContextCompat.getColor(this, R.color.primary_dark_color));
                                tvOptionText.setTypeface(tvOptionText.getTypeface(), android.graphics.Typeface.BOLD);

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
            tvQuestion.setText("Không có nội dung");
            tvAnswerLabel.setText("Không có đáp án");
            tvAnswer.setText("");
            layoutOptions.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, EditCardActivity.class);
            intent.putExtra("card_id", card.getFlashcardId());
            intent.putExtra("deck_id", deckId);
            startActivity(intent);
        });

        dialog.show();

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
        optionLayout.setBackgroundResource(R.drawable.bg_option_item);

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
        // Prevent multiple delete operations
        if (isDeletingCard) {
            Log.d(TAG, "Delete operation already in progress, ignoring new request");
            return;
        }

        Log.d(TAG, "Showing delete dialog for card ID: " + card.getFlashcardId());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_card, null);

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

        // Set card content
        JsonObject content = card.getContent();
        if (content != null) {
            switch (card.getCardType()) {
                case BASIC:
                    String front = content.has("front") ? content.get("front").getAsString() : "";
                    String back = content.has("back") ? content.get("back").getAsString() : "";
                    tvCardQuestion.setText(front);
                    tvCardAnswer.setText(back);
                    break;

                case MULTIPLE_CHOICE:
                    String question = content.has("question") ? content.get("question").getAsString() : "";
                    String correctAnswer = content.has("answer") ? content.get("answer").getAsString() : "";
                    tvCardQuestion.setText(question);
                    tvCardAnswer.setText(correctAnswer);
                    break;

                case FILL_IN_BLANK:
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
            tvCardQuestion.setText("Không có nội dung");
            tvCardAnswer.setText("");
        }

        currentDeleteDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> {
            currentDeleteDialog.dismiss();
            currentDeleteDialog = null;
        });

        btnDelete.setOnClickListener(v -> {
            // Prevent multiple delete calls
            if (isDeletingCard) {
                Log.d(TAG, "Delete already in progress");
                return;
            }

            // Set delete state
            isDeletingCard = true;

            // Show loading state
            btnDelete.setEnabled(false);
            btnDelete.setText("Đang xóa...");
            btnCancel.setEnabled(false);

            Log.d(TAG, "Starting delete for card ID: " + card.getFlashcardId());

            // Call ViewModel to delete flashcard via API
            deckManagementViewModel.deleteFlashcard(card.getFlashcardId(), cachedAuthToken);
        });

        currentDeleteDialog.show();

        if (currentDeleteDialog.getWindow() != null) {
            currentDeleteDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            currentDeleteDialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            currentDeleteDialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void refreshDeckData() {
        if (cachedAuthToken != null) {
            Log.d(TAG, "Refreshing deck data");
            deckManagementViewModel.loadDeckById(deckId, cachedAuthToken);
            deckManagementViewModel.loadFlashcardsByDeck(deckId, cachedAuthToken);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cachedAuthToken != null && !isDeletingCard) {
            refreshDeckData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up dialog references
        if (currentDeleteDialog != null && currentDeleteDialog.isShowing()) {
            currentDeleteDialog.dismiss();
            currentDeleteDialog = null;
        }
    }
}