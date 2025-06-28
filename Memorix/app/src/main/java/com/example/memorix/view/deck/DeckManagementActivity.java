package com.example.memorix.view.deck;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
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

    // Views
    private TextView tvDeckName, tvDeckDescription, tvTotalCards;
    private Button btnAddCard, btnStudyDeck;
    private RecyclerView recyclerViewCards;
    private CardAdapter cardAdapter;
    private DeckManagementViewModel deckManagementViewModel;

    // Data
    private List<Card> allCards;
    private List<Card> filteredCards;
    private long deckId;
    private String cachedAuthToken;

    // Delete operation tracking
    private boolean isDeletingCard = false;
    private AlertDialog currentDeleteDialog = null;

    // Search and filter components
    private EditText etSearchCards;
    private ImageView ivClearSearch;
    private CardView cardFilterDropdown, cardFilterOptions;
    private TextView tvFilterText, tvFilterAll, tvFilterBasic, tvFilterMultiple, tvFilterFill;
    private ImageView ivFilterArrow;

    // Search state management (chỉ local search)
    private boolean isFilterExpanded = false;
    private int currentFilterPosition = 0;
    private String currentSearchQuery = "";
    private boolean isSearching = false; // Track search state for UI updates

    // Debounce handling cho local search
    private Handler searchHandler;
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 300; // Giảm delay cho local search

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

        // Initialize search handler
        searchHandler = new Handler(Looper.getMainLooper());

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
        setupRecyclerViewAnimation();
        setupCustomFilter();
        setupListeners();
        setupObservers();

        // Load deck data and flashcards from API
        deckManagementViewModel.loadDeckById(deckId, cachedAuthToken);
        deckManagementViewModel.loadFlashcardsByDeck(deckId, cachedAuthToken);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending search operations
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        // Clean up references to prevent memory leaks
        searchHandler = null;
        searchRunnable = null;

        // Clean up dialog references
        if (currentDeleteDialog != null && currentDeleteDialog.isShowing()) {
            currentDeleteDialog.dismiss();
            currentDeleteDialog = null;
        }
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
                List<Card> newAllCards = new ArrayList<>(cards);

                if (allCards == null || allCards.isEmpty()) {
                    // First load
                    allCards = newAllCards;
                } else {
                    // Update with new data
                    allCards = newAllCards;
                }

                updateCardCount();
                // Apply current search and filter with DiffUtil
                performLocalSearch();
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
        recyclerViewCards = findViewById(R.id.recyclerViewCards);

        // Search components
        etSearchCards = findViewById(R.id.etSearchCards);
        ivClearSearch = findViewById(R.id.ivClearSearch);

        // Custom filter components
        cardFilterDropdown = findViewById(R.id.cardFilterDropdown);
        cardFilterOptions = findViewById(R.id.cardFilterOptions);
        tvFilterText = findViewById(R.id.tvFilterText);
        tvFilterAll = findViewById(R.id.tvFilterAll);
        tvFilterBasic = findViewById(R.id.tvFilterBasic);
        tvFilterMultiple = findViewById(R.id.tvFilterMultiple);
        tvFilterFill = findViewById(R.id.tvFilterFill);
        ivFilterArrow = findViewById(R.id.ivFilterArrow);
    }

    private void setupRecyclerView() {
        // Enable recycling and optimizations
        recyclerViewCards.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCards.setHasFixedSize(true); // Performance optimization
        recyclerViewCards.setItemViewCacheSize(20); // Cache more views

        filteredCards = new ArrayList<>();
        cardAdapter = new CardAdapter(filteredCards, this);
        recyclerViewCards.setAdapter(cardAdapter);
    }

    private void setupRecyclerViewAnimation() {
        // Tạo layout animation cho RecyclerView
        AnimationSet animationSet = new AnimationSet(true);

        Animation slideIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideIn.setDuration(250);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(250);

        animationSet.addAnimation(slideIn);
        animationSet.addAnimation(fadeIn);

        LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animationSet, 0.1f);
        recyclerViewCards.setLayoutAnimation(layoutAnimationController);
    }

    // Thêm hàm để animate khi filter/search
    private void animateRecyclerView() {
        if (recyclerViewCards.getLayoutAnimation() != null) {
            recyclerViewCards.scheduleLayoutAnimation();
        }
    }

    private void setupCustomFilter() {
        // Set initial filter state
        updateFilterText(0);

        // Filter dropdown click listener
        cardFilterDropdown.setOnClickListener(v -> toggleFilterOptions());

        // Filter option click listeners
        tvFilterAll.setOnClickListener(v -> selectFilter(0, "Tất cả"));
        tvFilterBasic.setOnClickListener(v -> selectFilter(1, "2 Mặt"));
        tvFilterMultiple.setOnClickListener(v -> selectFilter(2, "Trắc nghiệm"));
        tvFilterFill.setOnClickListener(v -> selectFilter(3, "Điền từ"));
    }

    private void toggleFilterOptions() {
        isFilterExpanded = !isFilterExpanded;

        if (isFilterExpanded) {
            cardFilterOptions.setVisibility(View.VISIBLE);
            // Rotate arrow up
            ivFilterArrow.animate().rotation(180f).setDuration(200).start();
            // Add subtle scale animation
            cardFilterOptions.setScaleY(0f);
            cardFilterOptions.animate()
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
        } else {
            // Rotate arrow down
            ivFilterArrow.animate().rotation(0f).setDuration(200).start();
            // Scale down and hide
            cardFilterOptions.animate()
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction(() -> cardFilterOptions.setVisibility(View.GONE))
                    .start();
        }
    }

    private void selectFilter(int position, String filterText) {
        currentFilterPosition = position;
        tvFilterText.setText(filterText);

        // Add ripple effect to selected item
        highlightSelectedFilter(position);

        // Close dropdown
        toggleFilterOptions();

        // Apply filter
        performLocalSearch();
    }

    private void highlightSelectedFilter(int position) {
        // Reset all filter colors
        resetFilterColors();

        // Highlight selected filter
        TextView selectedFilter = null;
        int highlightColor = ContextCompat.getColor(this, R.color.primary_light_color);

        switch (position) {
            case 0:
                selectedFilter = tvFilterAll;
                break;
            case 1:
                selectedFilter = tvFilterBasic;
                break;
            case 2:
                selectedFilter = tvFilterMultiple;
                break;
            case 3:
                selectedFilter = tvFilterFill;
                break;
        }

        if (selectedFilter != null) {
            selectedFilter.setBackgroundColor(highlightColor);
            selectedFilter.setTextColor(ContextCompat.getColor(this, R.color.primary_dark_color));
        }
    }

    private void resetFilterColors() {
        int normalColor = ContextCompat.getColor(this, android.R.color.transparent);
        int normalTextColor = ContextCompat.getColor(this, R.color.text_color);

        tvFilterAll.setBackgroundColor(normalColor);
        tvFilterAll.setTextColor(normalTextColor);
        tvFilterBasic.setBackgroundColor(normalColor);
        tvFilterBasic.setTextColor(normalTextColor);
        tvFilterMultiple.setBackgroundColor(normalColor);
        tvFilterMultiple.setTextColor(normalTextColor);
        tvFilterFill.setBackgroundColor(normalColor);
        tvFilterFill.setTextColor(normalTextColor);
    }

    private void updateFilterText(int position) {
        String[] filterTexts = {"Tất cả", "2 Mặt", "Trắc nghiệm", "Điền từ"};
        tvFilterText.setText(filterTexts[position]);
        highlightSelectedFilter(position);
    }

    private void setupListeners() {
        btnAddCard.setOnClickListener(v -> addSampleCard());
        btnStudyDeck.setOnClickListener(v -> showStudyOptionsDialog());

        // Optimized search functionality với debounce (local search only)
        setupSearchListener();

        // Clear search button
        ivClearSearch.setOnClickListener(v -> clearSearch());

        // Close filter options when clicking outside
        findViewById(R.id.main).setOnClickListener(v -> {
            if (isFilterExpanded) {
                toggleFilterOptions();
            }
        });
    }

    private void setupSearchListener() {
        if (etSearchCards != null) {
            etSearchCards.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Show/hide clear button efficiently
                    int visibility = s.length() > 0 ? View.VISIBLE : View.GONE;
                    if (ivClearSearch.getVisibility() != visibility) {
                        ivClearSearch.setVisibility(visibility);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim();

                    // Avoid unnecessary processing if query hasn't changed
                    if (query.equals(currentSearchQuery)) {
                        return;
                    }

                    currentSearchQuery = query;

                    // Cancel previous search if pending
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    // Create new search runnable
                    searchRunnable = () -> performDebouncedLocalSearch(query);

                    // Schedule new search with delay
                    if (query.isEmpty()) {
                        // If query is empty, search immediately
                        performDebouncedLocalSearch(query);
                    } else {
                        // If query is not empty, wait for delay
                        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                    }
                }
            });

            // Handle search action from keyboard
            etSearchCards.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    // Cancel pending search and execute immediately
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    performDebouncedLocalSearch(etSearchCards.getText().toString());

                    // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSearchCards.getWindowToken(), 0);
                    etSearchCards.clearFocus();
                    return true;
                }
                return false;
            });
        }
    }

    private void performDebouncedLocalSearch(String query) {
        isSearching = !query.isEmpty();
        performLocalSearch();
    }

    private void clearSearch() {
        // Cancel any pending search
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        isSearching = false;
        currentSearchQuery = "";
        etSearchCards.setText("");
        etSearchCards.clearFocus();

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearchCards.getWindowToken(), 0);

        // Refresh with empty search
        performLocalSearch();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performLocalSearch() {
        if (allCards == null) return;

        List<Card> newFilteredCards = new ArrayList<>();

        for (Card card : allCards) {
            // Apply type filter first
            boolean matchesFilter = false;
            switch (currentFilterPosition) {
                case 0: // Tất cả
                    matchesFilter = true;
                    break;
                case 1: // 2 Mặt
                    matchesFilter = card.getCardType() == CardType.BASIC;
                    break;
                case 2: // Trắc nghiệm
                    matchesFilter = card.getCardType() == CardType.MULTIPLE_CHOICE;
                    break;
                case 3: // Điền từ
                    matchesFilter = card.getCardType() == CardType.FILL_IN_BLANK;
                    break;
            }

            // Apply search filter if there's a search query
            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty() && matchesFilter) {
                matchesSearch = cardMatchesSearch(card, currentSearchQuery);
            }

            if (matchesFilter && matchesSearch) {
                newFilteredCards.add(card);
            }
        }

        // Use DiffUtil for efficient updates
        updateCardListWithDiff(newFilteredCards);

        Log.d(TAG, "Local search completed. Query: '" + currentSearchQuery +
                "', Filter: " + currentFilterPosition +
                ", Results: " + filteredCards.size() + "/" + allCards.size());
    }

    // Optimized card list update using DiffUtil
    private void updateCardListWithDiff(List<Card> newCards) {
        if (filteredCards.isEmpty()) {
            // First load - just set the data
            filteredCards.addAll(newCards);
            cardAdapter.notifyDataSetChanged();
            // Add animation for initial load
            animateRecyclerView();
        } else {
            // Use DiffUtil for efficient updates
            CardDiffCallback diffCallback = new CardDiffCallback(filteredCards, newCards);
            androidx.recyclerview.widget.DiffUtil.DiffResult diffResult =
                    androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback);

            filteredCards.clear();
            filteredCards.addAll(newCards);
            diffResult.dispatchUpdatesTo(cardAdapter);
        }
    }

    private boolean cardMatchesSearch(Card card, String query) {
        String lowerQuery = query.toLowerCase();
        JsonObject content = card.getContent();

        if (content == null) return false;

        switch (card.getCardType()) {
            case BASIC:
                String front = content.has("front") ? content.get("front").getAsString().toLowerCase() : "";
                String back = content.has("back") ? content.get("back").getAsString().toLowerCase() : "";
                return front.contains(lowerQuery) || back.contains(lowerQuery);

            case MULTIPLE_CHOICE:
                String question = content.has("question") ? content.get("question").getAsString().toLowerCase() : "";
                String answer = content.has("answer") ? content.get("answer").getAsString().toLowerCase() : "";
                // Also search in options
                if (content.has("options")) {
                    JsonArray optionsArray = content.getAsJsonArray("options");
                    for (int i = 0; i < optionsArray.size(); i++) {
                        String option = optionsArray.get(i).getAsString().toLowerCase();
                        if (option.contains(lowerQuery)) {
                            return true;
                        }
                    }
                }
                return question.contains(lowerQuery) || answer.contains(lowerQuery);

            case FILL_IN_BLANK:
                String text = content.has("text") ? content.get("text").getAsString().toLowerCase() : "";
                String fillAnswer = content.has("answer") ? content.get("answer").getAsString().toLowerCase() : "";
                return text.contains(lowerQuery) || fillAnswer.contains(lowerQuery);

            default:
                return false;
        }
    }

    // Helper method để refresh data
    private void refreshDeckData() {
        if (cachedAuthToken != null) {
            Log.d(TAG, "Refreshing deck data");
            deckManagementViewModel.loadDeckById(deckId, cachedAuthToken);
            deckManagementViewModel.loadFlashcardsByDeck(deckId, cachedAuthToken);
        }
    }

    // Study dialog and navigation methods
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

    // Card action handlers
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
                            LinearLayout optionLayout = createOptionLayout();

                            TextView tvOptionLetter = new TextView(this);
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
    private LinearLayout createOptionLayout() {
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

    @Override
    protected void onResume() {
        super.onResume();
        if (cachedAuthToken != null && !isDeletingCard) {
            refreshDeckData();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFilterExpanded) {
            toggleFilterOptions();
        } else {
            super.onBackPressed();
        }
    }

    // DiffUtil callback for efficient RecyclerView updates
    private static class CardDiffCallback extends androidx.recyclerview.widget.DiffUtil.Callback {
        private final List<Card> oldList;
        private final List<Card> newList;

        public CardDiffCallback(List<Card> oldList, List<Card> newList) {
            this.oldList = new ArrayList<>(oldList);
            this.newList = new ArrayList<>(newList);
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Compare by flashcard ID
            return oldList.get(oldItemPosition).getFlashcardId() ==
                    newList.get(newItemPosition).getFlashcardId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Card oldCard = oldList.get(oldItemPosition);
            Card newCard = newList.get(newItemPosition);

            // Compare all relevant fields that affect UI
            if (oldCard.getCardType() != newCard.getCardType()) {
                return false;
            }

            // Compare content JSON
            JsonObject oldContent = oldCard.getContent();
            JsonObject newContent = newCard.getContent();

            if (oldContent == null && newContent == null) {
                return true;
            }

            if (oldContent == null || newContent == null) {
                return false;
            }

            // Compare content based on card type
            switch (oldCard.getCardType()) {
                case BASIC:
                    String oldFront = oldContent.has("front") ? oldContent.get("front").getAsString() : "";
                    String oldBack = oldContent.has("back") ? oldContent.get("back").getAsString() : "";
                    String newFront = newContent.has("front") ? newContent.get("front").getAsString() : "";
                    String newBack = newContent.has("back") ? newContent.get("back").getAsString() : "";
                    return oldFront.equals(newFront) && oldBack.equals(newBack);

                case MULTIPLE_CHOICE:
                    String oldQuestion = oldContent.has("question") ? oldContent.get("question").getAsString() : "";
                    String oldAnswer = oldContent.has("answer") ? oldContent.get("answer").getAsString() : "";
                    String newQuestion = newContent.has("question") ? newContent.get("question").getAsString() : "";
                    String newAnswer = newContent.has("answer") ? newContent.get("answer").getAsString() : "";

                    if (!oldQuestion.equals(newQuestion) || !oldAnswer.equals(newAnswer)) {
                        return false;
                    }

                    // Compare options array
                    JsonArray oldOptions = oldContent.has("options") ? oldContent.getAsJsonArray("options") : new JsonArray();
                    JsonArray newOptions = newContent.has("options") ? newContent.getAsJsonArray("options") : new JsonArray();
                    return oldOptions.equals(newOptions);

                case FILL_IN_BLANK:
                    String oldText = oldContent.has("text") ? oldContent.get("text").getAsString() : "";
                    String oldFillAnswer = oldContent.has("answer") ? oldContent.get("answer").getAsString() : "";
                    String newText = newContent.has("text") ? newContent.get("text").getAsString() : "";
                    String newFillAnswer = newContent.has("answer") ? newContent.get("answer").getAsString() : "";
                    return oldText.equals(newText) && oldFillAnswer.equals(newFillAnswer);

                default:
                    return oldContent.equals(newContent);
            }
        }

        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            // Return payload for partial updates if needed
            // For now, return null to use default full item update
            return null;
        }
    }
}