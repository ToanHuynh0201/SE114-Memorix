package com.example.memorix.view.deck;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.model.Deck;
import com.example.memorix.view.deck.adapter.DeckLibraryAdapter;
import com.example.memorix.viewmodel.DeckLibraryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeckLibraryFragment extends Fragment implements DeckLibraryAdapter.DeckLibraryListener {

    private static final String TAG = "DeckLibraryFragment";
    private static final int SEARCH_DELAY_MS = 500; // Debounce delay for search

    // Views
    private EditText etSearch;
    private RecyclerView rvAllDecks;
    private LinearLayout layoutEmptyState;
    private NestedScrollView scrollView;
    private Spinner spinnerCategoryFilter;
    private TextView tvDeckTitle;
    private TextView tvSearchResults;
    private TextView tvEmptyDescription;

    // Components
    private DeckLibraryAdapter deckAdapter;
    private DeckLibraryViewModel viewModel;
    private List<Deck> deckList; // Add this for DiffUtil

    // State
    private String cachedAuthToken;
    private String currentSearchQuery = "";
    private String selectedCategory = "";

    // Search debouncing
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Category options
    private final String[] categoryOptions = {
            "Tất cả thể loại",
            "Ngôn ngữ",
            "Khoa học",
            "Lịch sử",
            "Toán học",
            "Nghệ thuật",
            "Khác"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deck_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cachedAuthToken = getAuthToken();

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupCategorySpinner();
        setupSearchFunctionality();
        observeData();

        // Load initial data
        viewModel.loadPublicDecks();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvAllDecks = view.findViewById(R.id.rv_all_decks);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        scrollView = view.findViewById(R.id.scroll_view);
        spinnerCategoryFilter = view.findViewById(R.id.spinner_category_filter);
        tvDeckTitle = view.findViewById(R.id.tv_deck_title);
        tvSearchResults = view.findViewById(R.id.tv_search_results);
        tvEmptyDescription = view.findViewById(R.id.tv_empty_description);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DeckLibraryViewModel.class);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");

        // Enable recycling and optimizations
        rvAllDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllDecks.setHasFixedSize(true); // Performance optimization
        rvAllDecks.setItemViewCacheSize(20); // Cache more views

        deckList = new ArrayList<>();
        deckAdapter = new DeckLibraryAdapter(deckList, this);
        rvAllDecks.setAdapter(deckAdapter);
    }

    private void setupCategorySpinner() {
        Log.d(TAG, "Setting up category spinner");

        // Create adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryOptions
        );

        // Set dropdown layout
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(adapter);

        // Set listener for category selection
        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newCategory = position == 0 ? "" : categoryOptions[position];

                // Only perform search if category actually changed
                if (!newCategory.equals(selectedCategory)) {
                    selectedCategory = newCategory;
                    Log.d(TAG, "Category filter changed to: " +
                            (selectedCategory.isEmpty() ? "All" : selectedCategory));

                    // Perform API search with new category
                    performSearch();

                    // Update UI
                    updateUIForFilterState();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();

                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search with debounce
                searchRunnable = () -> {
                    performSearch();
                    updateUIForFilterState();
                };

                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        Log.d(TAG, "Performing API search - Query: '" + currentSearchQuery +
                "', Category: '" + selectedCategory + "'");

        // Use API-based search
        viewModel.searchDecks(currentSearchQuery, selectedCategory);
    }

    @SuppressLint("SetTextI18n")
    private void updateUIForFilterState() {
        boolean hasSearchQuery = !currentSearchQuery.isEmpty();
        boolean hasCategoryFilter = !selectedCategory.isEmpty();

        if (hasSearchQuery || hasCategoryFilter) {
            // Build dynamic title
            StringBuilder titleBuilder = new StringBuilder();
            if (hasSearchQuery && hasCategoryFilter) {
                titleBuilder.append("Kết quả cho '").append(currentSearchQuery)
                        .append("' trong ").append(selectedCategory);
            } else if (hasSearchQuery) {
                titleBuilder.append("Kết quả tìm kiếm");
            } else {
                titleBuilder.append("Thư viện ").append(selectedCategory);
            }

            tvDeckTitle.setText(titleBuilder.toString());
            tvSearchResults.setVisibility(View.VISIBLE);

            // Update empty state for search/filter
            if (hasSearchQuery && hasCategoryFilter) {
                tvEmptyDescription.setText("Không tìm thấy deck nào cho '" +
                        currentSearchQuery + "' trong thể loại " + selectedCategory);
            } else if (hasSearchQuery) {
                tvEmptyDescription.setText("Thử tìm kiếm với từ khóa khác");
            } else {
                tvEmptyDescription.setText("Không có deck nào trong thể loại " + selectedCategory);
            }
        } else {
            tvDeckTitle.setText("✨ Khám phá các bộ deck tuyệt vời");
            tvSearchResults.setVisibility(View.GONE);

            // Reset empty state
            tvEmptyDescription.setText("Thử tìm kiếm với từ khóa khác hoặc kiểm tra lại kết nối mạng");
        }
    }

    private void updateSearchResultsCount(int count) {
        boolean hasSearchQuery = !currentSearchQuery.isEmpty();
        boolean hasCategoryFilter = !selectedCategory.isEmpty();

        if (hasSearchQuery || hasCategoryFilter) {
            String resultText;

            if (count == 0) {
                if (hasSearchQuery && hasCategoryFilter) {
                    resultText = "Không tìm thấy kết quả cho '" + currentSearchQuery +
                            "' trong " + selectedCategory;
                } else if (hasSearchQuery) {
                    resultText = "Không tìm thấy kết quả cho '" + currentSearchQuery + "'";
                } else {
                    resultText = "Không có deck nào trong " + selectedCategory;
                }
            } else {
                String countText = count == 1 ? "1 deck" : count + " deck";
                if (hasSearchQuery && hasCategoryFilter) {
                    resultText = "Tìm thấy " + countText + " cho '" + currentSearchQuery +
                            "' trong " + selectedCategory;
                } else if (hasSearchQuery) {
                    resultText = "Tìm thấy " + countText + " cho '" + currentSearchQuery + "'";
                } else {
                    resultText = "Tìm thấy " + countText + " trong " + selectedCategory;
                }
            }

            tvSearchResults.setText(resultText);
            tvSearchResults.setVisibility(View.VISIBLE);
        } else {
            tvSearchResults.setVisibility(View.GONE);
        }
    }

    private void observeData() {
        // Observe public decks from API with DiffUtil
        viewModel.getPublicDecks().observe(getViewLifecycleOwner(), this::updateDeckList);

        // Observe loading state (no UI changes needed since ProgressBar is removed)
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Loading state: " + isLoading);
            // Loading state is tracked but no UI update needed
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe clone loading state
        viewModel.getCloneLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                Log.d(TAG, "Clone operation in progress...");
            }
        });

        // Observe clone success
        viewModel.getCloneSuccess().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(getContext(),
                        "🎉 Đã clone deck thành công!\nKiểm tra trong thư viện cá nhân của bạn.",
                        Toast.LENGTH_LONG).show();
                viewModel.clearCloneMessages();
            }
        });

        // Observe clone errors
        viewModel.getCloneError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "❌ " + error, Toast.LENGTH_LONG).show();
                viewModel.clearCloneMessages();
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
    }

    // Implement DeckLibraryListener methods
    @Override
    public void onDeckClick(Deck deck, int position) {
        Log.d(TAG, "Deck clicked: " + deck.getName());
        showDeckPreviewDialog(deck);
    }

    @Override
    public void onCloneDeck(Deck deck, int position) {
        Log.d(TAG, "Clone deck clicked: " + deck.getName());
        showCloneConfirmationDialog(deck);
    }

    @SuppressLint("SetTextI18n")
    private void showDeckPreviewDialog(Deck deck) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_preview_deck, null);
        builder.setView(dialogView);

        // Create dialog
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Get views
        TextView tvDeckName = dialogView.findViewById(R.id.tv_deck_name);
        TextView tvCardCount = dialogView.findViewById(R.id.tv_card_count);
        TextView tvDeckDescription = dialogView.findViewById(R.id.tv_deck_description);
        AppCompatButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        AppCompatButton btnClone = dialogView.findViewById(R.id.btn_start_learning);

        // Set data
        tvDeckName.setText(deck.getName());
        tvCardCount.setText(deck.getTotalCards() + " thẻ");

        // Set description
        String description = deck.getDescription();
        if (description == null || description.trim().isEmpty()) {
            tvDeckDescription.setText("Một bộ flashcard thú vị đang chờ bạn khám phá! 🚀");
        } else {
            tvDeckDescription.setText(description);
        }

        // Update button text for cloning
        btnClone.setText("Clone Deck");

        // Set click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnClone.setOnClickListener(v -> {
            dialog.dismiss();
            showCloneConfirmationDialog(deck);
        });

        dialog.show();
    }

    private void showCloneConfirmationDialog(Deck deck) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Clone Deck");
        builder.setMessage("Bạn có muốn clone deck \"" + deck.getName() + "\" vào thư viện cá nhân không?\n\nDeck sẽ được thêm vào bộ sưu tập của bạn và bạn có thể tùy chỉnh nội dung.");

        builder.setPositiveButton("Clone ngay", (dialog, which) -> {
            cloneDeck(deck);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Customize button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.secondary_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.secondary_text_color));
    }

    private void cloneDeck(Deck deck) {
        viewModel.cloneDeck(deck, cachedAuthToken);
    }

    private String getAuthToken() {
        if (cachedAuthToken != null) {
            return cachedAuthToken;
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        cachedAuthToken = prefs.getString("access_token", null);
        return cachedAuthToken;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up search handler
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Clear references to prevent memory leaks
        searchHandler = null;
        searchRunnable = null;
        deckAdapter = null;
        deckList = null;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateDeckList(List<Deck> newDecks) {
        if (newDecks == null) return;

        debugDeckData(newDecks);

        if (deckList.isEmpty()) {
            // First load - just set the data
            deckList.addAll(newDecks);
            deckAdapter.notifyDataSetChanged();
        } else {
            // Use DiffUtil for efficient updates with smooth animations
            DeckDiffCallback diffCallback = new DeckDiffCallback(deckList, newDecks);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            deckList.clear();
            deckList.addAll(newDecks);
            diffResult.dispatchUpdatesTo(deckAdapter);
        }

        // Update UI states
        updateEmptyState(newDecks.isEmpty());
        updateSearchResultsCount(newDecks.size());
    }

    private void debugDeckData(List<Deck> decks) {
        for (int i = 0; i < Math.min(decks.size(), 3); i++) { // Show only first 3 for brevity
            Deck deck = decks.get(i);
            Log.d(TAG, "Deck " + i + ": " + deck.getName() +
                    " (" + deck.getCategory() + ") - " + deck.getTotalCards() + " cards");
        }
        Log.d(TAG, "=== END DEBUG ===");
    }
    private static class DeckDiffCallback extends DiffUtil.Callback {
        private final List<Deck> oldList;
        private final List<Deck> newList;

        public DeckDiffCallback(List<Deck> oldList, List<Deck> newList) {
            this.oldList = oldList;
            this.newList = newList;
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
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Deck oldDeck = oldList.get(oldItemPosition);
            Deck newDeck = newList.get(newItemPosition);

            return Objects.equals(oldDeck.getName(), newDeck.getName()) &&
                    Objects.equals(oldDeck.getDescription(), newDeck.getDescription()) &&
                    oldDeck.getTotalCards() == newDeck.getTotalCards() &&
                    oldDeck.getLearnedCards() == newDeck.getLearnedCards() &&
                    oldDeck.getDueCards() == newDeck.getDueCards() &&
                    oldDeck.getUnlearnedCards() == newDeck.getUnlearnedCards() &&
                    oldDeck.isPublic() == newDeck.isPublic() &&
                    Objects.equals(oldDeck.getImageUrl(), newDeck.getImageUrl()) &&
                    Objects.equals(oldDeck.getCategory(), newDeck.getCategory());
        }
    }
}