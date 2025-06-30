package com.example.memorix.view.deck;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.ProgressBar;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.model.Deck;
import com.example.memorix.view.deck.adapter.DeckLibraryAdapter;
import com.example.memorix.viewmodel.DeckLibraryViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class DeckLibraryFragment extends Fragment implements DeckLibraryAdapter.DeckLibraryListener {

    private static final String TAG = "DeckLibraryFragment";

    // Views
    private EditText etSearch;
    private RecyclerView rvAllDecks;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private NestedScrollView scrollView;
    private Spinner spinnerCategoryFilter;
    private TextView tvDeckTitle;
    private TextView tvSearchResults;
    private TextView tvEmptyDescription;

    // Components
    private DeckLibraryAdapter deckAdapter;
    private DeckLibraryViewModel viewModel;

    // State
    private String cachedAuthToken;
    private String currentSearchQuery = "";
    private String selectedCategory = "";

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

        // Load data
        viewModel.loadPublicDecks();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvAllDecks = view.findViewById(R.id.rv_all_decks);
        progressBar = view.findViewById(R.id.progress_bar);
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
        deckAdapter = new DeckLibraryAdapter(new ArrayList<>(), this);
        rvAllDecks.setLayoutManager(new LinearLayoutManager(getContext()));
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

                // Only perform filter if category actually changed
                if (!newCategory.equals(selectedCategory)) {
                    selectedCategory = newCategory;
                    Log.d(TAG, "Category filter changed to: " +
                            (selectedCategory.isEmpty() ? "All" : selectedCategory));

                    // Perform local filtering
                    performLocalFiltering();

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
                performLocalFiltering();
                updateUIForFilterState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performLocalFiltering() {
        if (currentSearchQuery.isEmpty() && selectedCategory.isEmpty()) {
            // No filters - show all decks
            viewModel.filterDecks("");
        } else {
            // Apply local filtering with both search and category
            viewModel.filterDecksWithCategory(currentSearchQuery, selectedCategory);
        }
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
        // Observe public decks from API
        viewModel.getPublicDecks().observe(getViewLifecycleOwner(), decks -> {
            if (decks != null) {
                viewModel.setOriginalDecks(decks);
                Log.d(TAG, "Loaded " + decks.size() + " public decks from API");
            }
        });

        // Observe filtered decks
        viewModel.getFilteredDecks().observe(getViewLifecycleOwner(), decks -> {
            if (decks != null) {
                deckAdapter.updateData(decks);
                updateEmptyState(decks.isEmpty());
                updateSearchResultsCount(decks.size());
                Log.d(TAG, "Displaying " + decks.size() + " filtered decks");
            }
        });

        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            updateLoadingState(isLoading);
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe clone loading state
        viewModel.getCloneLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Optional: Show loading dialog or disable buttons during clone
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

    private void updateLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
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

    public void clearFilters() {
        Log.d(TAG, "Clearing all filters");

        currentSearchQuery = "";
        selectedCategory = "";
        etSearch.setText("");
        etSearch.clearFocus();

        // Reset category filter to "All"
        spinnerCategoryFilter.setSelection(0);

        // Reset filtering
        performLocalFiltering();
        updateUIForFilterState();
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
        builder.setTitle("🚀 Clone Deck");
        builder.setMessage("Bạn có muốn clone deck \"" + deck.getName() + "\" vào thư viện cá nhân không?\n\nDeck sẽ được thêm vào bộ sưu tập của bạn và bạn có thể tùy chỉnh nội dung.");

        builder.setPositiveButton("✨ Clone ngay", (dialog, which) -> {
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
        Log.d(TAG, "Cloning deck: " + deck.getName());
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
}