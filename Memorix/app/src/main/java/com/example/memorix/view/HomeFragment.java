package com.example.memorix.view;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memorix.R;
import com.example.memorix.model.Deck;
import com.example.memorix.view.deck.DeckManagementActivity;
import com.example.memorix.view.deck.adapter.DeckActionListener;
import com.example.memorix.view.deck.adapter.DeckAdapter;
import com.example.memorix.view.deck.bottomsheet.AddOptionsBottomSheet;
import com.example.memorix.view.deck.bottomsheet.CreateDeckBottomSheet;
import com.example.memorix.viewmodel.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements DeckActionListener,
        AddOptionsBottomSheet.OptionClickListener,
        CreateDeckBottomSheet.CreateDeckListener{

    private static final String TAG = "HomeFragment";
    private static final long SEARCH_DELAY_MS = 500;

    // Views
    private RecyclerView recyclerFlashcardSets;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutNoSearchResults;
    private Button btnCreateFirstSet;
    private DeckAdapter deckAdapter;
    private List<Deck> deckList;
    private FloatingActionButton fabAdd;
    private EditText etSearch;
    private ImageView ivClearSearch;
    private TextView txtMyFsets;
    private TextView txtSearchResults;
    private TextView txtEmptyTitle;
    private TextView txtEmptyDescription;
    private TextView txtSearchQuery;
    private Spinner spinnerCategoryFilter;

    // Search and filter state
    private boolean isSearching = false;
    private String currentSearchQuery = "";
    private String selectedCategory = "";

    // Debounce handling
    private Handler searchHandler;
    private Runnable searchRunnable;

    // MVVM components
    private HomeViewModel homeViewModel;

    // Cache for auth token
    private String cachedAuthToken;

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

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Initialize search handler
        searchHandler = new Handler(Looper.getMainLooper());

        // Cache auth token once
        cachedAuthToken = getAuthToken();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        // Initialize views
        initViews(view);

        // Setup components
        setupRecyclerView();
        setupCategorySpinner();
        setupObservers();
        setupClickListeners();
        setupSearchListener();

        // Load data from API
        loadInitialData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - refreshing decks");
        // Refresh dữ liệu khi quay lại fragment
        refreshDecks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Cancel any pending search operations
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        // Clear references to prevent memory leaks
        searchHandler = null;
        searchRunnable = null;
        deckAdapter = null;
    }

    // ========== INITIALIZATION METHODS ==========

    private void initViews(View view) {
        recyclerFlashcardSets = view.findViewById(R.id.recycler_flashcard_sets);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutNoSearchResults = view.findViewById(R.id.layout_no_search_results);
        btnCreateFirstSet = view.findViewById(R.id.btn_create_first_set);
        fabAdd = view.findViewById(R.id.fab_add_deck);
        etSearch = view.findViewById(R.id.et_search);
        ivClearSearch = view.findViewById(R.id.iv_clear_search);
        txtMyFsets = view.findViewById(R.id.txt_my_sets);
        txtSearchResults = view.findViewById(R.id.txt_search_results);
        txtEmptyTitle = view.findViewById(R.id.txt_empty_title);
        txtEmptyDescription = view.findViewById(R.id.txt_empty_description);
        txtSearchQuery = view.findViewById(R.id.txt_search_query);
        spinnerCategoryFilter = view.findViewById(R.id.spinner_category_filter);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");

        // Enable recycling and optimizations
        recyclerFlashcardSets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerFlashcardSets.setHasFixedSize(true); // Performance optimization
        recyclerFlashcardSets.setItemViewCacheSize(20); // Cache more views

        deckList = new ArrayList<>();
        deckAdapter = new DeckAdapter(deckList, this);
        recyclerFlashcardSets.setAdapter(deckAdapter);
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

                    // Perform filtered search
                    performFilteredSearch();

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

    private void setupSearchListener() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
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
                    searchRunnable = () -> performDebouncedSearch(query);

                    // Schedule new search with delay
                    if (query.isEmpty()) {
                        // If query is empty, search immediately
                        performDebouncedSearch(query);
                    } else {
                        // If query is not empty, wait for delay
                        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                    }

                    // Update UI immediately for better UX
                    updateUIForSearchState();
                }
            });

            // Handle search action from keyboard
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    // Cancel pending search and execute immediately
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    performDebouncedSearch(etSearch.getText().toString());
                    return true;
                }
                return false;
            });
        }
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showBottomSheet());
        btnCreateFirstSet.setOnClickListener(v -> onCreateDeckClicked());

        // Clear search button
        ivClearSearch.setOnClickListener(v -> clearSearch());
    }

    private void setupObservers() {
        Log.d(TAG, "Setting up observers");

        // Observe decks data with DiffUtil for better performance
        homeViewModel.getDecks().observe(getViewLifecycleOwner(), this::updateDeckList);

        homeViewModel.getLoadingState().observe(getViewLifecycleOwner(), isLoading -> {
            // Could show progress bar here if needed
            Log.d(TAG, "Loading state: " + isLoading);
        });

        homeViewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null) {
                if (isSuccess) {
                    Toast.makeText(getContext(), "Đã xóa bộ thẻ thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể xóa bộ thẻ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe error messages
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::handleErrorMessage);

        // Observe create deck success
        homeViewModel.getCreateDeckSuccess().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                Toast.makeText(getContext(), "Đã tạo bộ thẻ: " + response.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Observe update success
        homeViewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(getContext(), "Đã cập nhật bộ thẻ thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========== DATA LOADING METHODS ==========

    private String getAuthToken() {
        if (cachedAuthToken != null) {
            return cachedAuthToken;
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        cachedAuthToken = prefs.getString("access_token", null);
        return cachedAuthToken;
    }

    private void loadInitialData() {
        if (cachedAuthToken != null) {
            Log.d(TAG, "Loading initial data with token");
            homeViewModel.loadDecks(cachedAuthToken);
        } else {
            Log.w(TAG, "No auth token available");
            showAuthError();
        }
    }

    private void showAuthError() {
        Toast.makeText(getContext(), "Vui lòng đăng nhập để xem danh sách bộ thẻ", Toast.LENGTH_LONG).show();
    }

    // ========== SEARCH AND FILTER METHODS ==========

    private void performDebouncedSearch(String query) {
        currentSearchQuery = query;
        performFilteredSearch();
    }

    private void performFilteredSearch() {
        if (cachedAuthToken != null) {
            if (currentSearchQuery.isEmpty() && selectedCategory.isEmpty()) {
                // No filters applied
                isSearching = false;
                homeViewModel.loadDecks(cachedAuthToken);
                Log.d(TAG, "Loading all decks (no filters)");
            } else {
                // Apply search and/or category filter
                isSearching = true;

                Log.d(TAG, "Performing filtered search - Query: '" +
                        currentSearchQuery + "', Category: '" + selectedCategory + "'");

                // Use the new method that supports both search and category
                homeViewModel.searchDecksWithCategory(cachedAuthToken,
                        currentSearchQuery.isEmpty() ? null : currentSearchQuery,
                        selectedCategory.isEmpty() ? null : selectedCategory);
            }
        }
    }

    public void clearSearch() {
        Log.d(TAG, "Clearing search and filters");

        // Cancel any pending search
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        isSearching = false;
        currentSearchQuery = "";
        etSearch.setText("");
        etSearch.clearFocus();

        // Reset category filter to "All"
        spinnerCategoryFilter.setSelection(0);
        selectedCategory = "";

        if (cachedAuthToken != null) {
            homeViewModel.loadDecks(cachedAuthToken);
        }

        updateUIForSearchState();
    }

    public void refreshDecks() {
        if (cachedAuthToken != null) {
            performFilteredSearch(); // This will respect current filters
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tải dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== UI UPDATE METHODS ==========

    @SuppressLint("SetTextI18n")
    private void updateUIForSearchState() {
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
                titleBuilder.append("Lọc theo ").append(selectedCategory);
            }

            txtMyFsets.setText(titleBuilder.toString());
            txtSearchResults.setVisibility(View.VISIBLE);

            // Update empty state for search/filter
            txtEmptyTitle.setText("Không tìm thấy kết quả");
            if (hasSearchQuery && hasCategoryFilter) {
                txtEmptyDescription.setText("Không tìm thấy bộ thẻ nào cho '" +
                        currentSearchQuery + "' trong thể loại " + selectedCategory);
            } else if (hasSearchQuery) {
                txtEmptyDescription.setText("Thử tìm kiếm với từ khóa khác");
            } else {
                txtEmptyDescription.setText("Không có bộ thẻ nào trong thể loại " + selectedCategory);
            }
            btnCreateFirstSet.setVisibility(View.GONE);
        } else {
            txtMyFsets.setText("Bộ thẻ của tôi");
            txtSearchResults.setVisibility(View.GONE);

            // Reset empty state
            txtEmptyTitle.setText("Chưa có bộ thẻ nào");
            txtEmptyDescription.setText("Hãy tạo bộ thẻ đầu tiên để bắt đầu học");
            btnCreateFirstSet.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUIForFilterState() {
        updateUIForSearchState(); // Reuse the same logic
    }

    private void updateSearchResultsCount(int count) {
        if (isSearching) {
            String resultText;
            boolean hasSearchQuery = !currentSearchQuery.isEmpty();
            boolean hasCategoryFilter = !selectedCategory.isEmpty();

            if (count == 0) {
                if (hasSearchQuery && hasCategoryFilter) {
                    resultText = "Không tìm thấy kết quả cho '" + currentSearchQuery +
                            "' trong " + selectedCategory;
                } else if (hasSearchQuery) {
                    resultText = "Không tìm thấy kết quả cho '" + currentSearchQuery + "'";
                } else {
                    resultText = "Không có bộ thẻ nào trong " + selectedCategory;
                }
            } else {
                String countText = count == 1 ? "1 kết quả" : count + " kết quả";
                if (hasSearchQuery && hasCategoryFilter) {
                    resultText = "Tìm thấy " + countText + " cho '" + currentSearchQuery +
                            "' trong " + selectedCategory;
                } else if (hasSearchQuery) {
                    resultText = "Tìm thấy " + countText + " cho '" + currentSearchQuery + "'";
                } else {
                    resultText = "Tìm thấy " + countText + " trong " + selectedCategory;
                }
            }

            txtSearchResults.setText(resultText);
            txtSearchResults.setVisibility(View.VISIBLE);
        } else {
            txtSearchResults.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void checkEmptyState() {
        boolean hasData = !deckList.isEmpty();

        if (hasData) {
            layoutEmptyState.setVisibility(View.GONE);
            layoutNoSearchResults.setVisibility(View.GONE);
            recyclerFlashcardSets.setVisibility(View.VISIBLE);
        } else {
            recyclerFlashcardSets.setVisibility(View.GONE);

            if (isSearching) {
                // Show no search results state
                layoutEmptyState.setVisibility(View.GONE);
                layoutNoSearchResults.setVisibility(View.VISIBLE);

                boolean hasSearchQuery = !currentSearchQuery.isEmpty();
                boolean hasCategoryFilter = !selectedCategory.isEmpty();

                if (hasSearchQuery && hasCategoryFilter) {
                    txtSearchQuery.setText("Không tìm thấy kết quả nào cho '" + currentSearchQuery +
                            "' trong thể loại " + selectedCategory);
                } else if (hasSearchQuery) {
                    txtSearchQuery.setText("Không tìm thấy kết quả nào cho '" + currentSearchQuery + "'");
                } else {
                    txtSearchQuery.setText("Không có bộ thẻ nào trong thể loại " + selectedCategory);
                }
            } else {
                // Show regular empty state
                layoutEmptyState.setVisibility(View.VISIBLE);
                layoutNoSearchResults.setVisibility(View.GONE);
            }
        }
    }

    // ========== DATA HANDLING METHODS ==========

    // Optimized deck list update using DiffUtil
    @SuppressLint("NotifyDataSetChanged")
    private void updateDeckList(List<Deck> newDecks) {
        if (newDecks == null) return;

        debugDeckData(newDecks);

        if (deckList.isEmpty()) {
            // First load - just set the data
            deckList.addAll(newDecks);
            deckAdapter.notifyDataSetChanged();
        } else {
            // Use DiffUtil for efficient updates
            DeckDiffCallback diffCallback = new DeckDiffCallback(deckList, newDecks);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            deckList.clear();
            deckList.addAll(newDecks);
            diffResult.dispatchUpdatesTo(deckAdapter);
        }

        // Update search results count
        if (isSearching) {
            updateSearchResultsCount(newDecks.size());
        }

        checkEmptyState();
    }

    private void debugDeckData(List<Deck> decks) {
        Log.d(TAG, "=== DEBUG DECK DATA ===");
        Log.d(TAG, "Total decks: " + decks.size());
        Log.d(TAG, "Search query: '" + currentSearchQuery + "'");
        Log.d(TAG, "Selected category: '" + selectedCategory + "'");

        for (int i = 0; i < Math.min(decks.size(), 3); i++) { // Show only first 3 for brevity
            Deck deck = decks.get(i);
            Log.d(TAG, "Deck " + i + ": " + deck.getName() +
                    " (" + deck.getCategory() + ") - " + deck.getTotalCards() + " cards");
        }
        Log.d(TAG, "=== END DEBUG ===");
    }

    // Optimized error handling
    private void handleErrorMessage(String errorMsg) {
        if (errorMsg == null || errorMsg.isEmpty()) return;

        String message;
        if (errorMsg.contains("delete")) {
            message = "Không thể xóa bộ thẻ: " + errorMsg;
        } else if (errorMsg.contains("update")) {
            message = "Không thể cập nhật bộ thẻ: " + errorMsg;
        } else if (errorMsg.contains("create")) {
            message = "Không thể tạo bộ thẻ: " + errorMsg;
        } else {
            message = errorMsg;
        }

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private Deck findDeckById(long deckId) {
        if (deckList != null) {
            for (Deck deck : deckList) {
                if (deck.getId() == deckId) {
                    return deck;
                }
            }
        }
        return null;
    }

    // ========== BOTTOM SHEET AND DIALOG METHODS ==========

    private void showBottomSheet() {
        AddOptionsBottomSheet bottomSheet = AddOptionsBottomSheet.newInstance();
        bottomSheet.setOptionClickListener(this);
        bottomSheet.show(getParentFragmentManager(), "AddOptionsBottomSheet");
    }

    // ========== DECK ACTION LISTENER IMPLEMENTATION ==========

    @Override
    public void onDeckClick(Deck deck, int position) {
        Log.d(TAG, "Deck clicked: " + deck.getName());
        Intent intent = new Intent(getContext(), DeckManagementActivity.class);
        intent.putExtra("deck_id", deck.getId());
        intent.putExtra("deck_name", deck.getName());
        startActivity(intent);
    }

    @Override
    public void onEditDeck(long deckId) {
        Deck deckToEdit = findDeckById(deckId);
        if (deckToEdit != null) {
            Log.d(TAG, "Editing deck with ID: " + deckId + ", Name: " + deckToEdit.getName());
            showEditDeckDialog(deckToEdit);
        } else {
            Log.w(TAG, "Cannot find deck with ID: " + deckId);
            Toast.makeText(getContext(), "Không tìm thấy bộ thẻ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShareDeck(long deckId) {
        Deck deckToShare = findDeckById(deckId);
        if (deckToShare != null) {
            Log.d(TAG, "Sharing deck with ID: " + deckId + ", Name: " + deckToShare.getName());
            showShareDeckDialog(deckToShare);
        } else {
            Log.w(TAG, "Cannot find deck with ID: " + deckId);
            Toast.makeText(getContext(), "Không tìm thấy bộ thẻ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResetProgress(long deckId) {
        Log.d(TAG, "Reset progress for deck ID: " + deckId);
        // TODO: Implement reset progress functionality
        Toast.makeText(getContext(), "Tính năng đặt lại tiến độ sẽ được cập nhật sớm", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteDeck(long deckId) {
        Deck deckToDelete = findDeckById(deckId);
        if (deckToDelete == null) {
            Log.w(TAG, "Cannot find deck with ID: " + deckId);
            Toast.makeText(getContext(), "Không tìm thấy bộ thẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Deleting deck with ID: " + deckId + ", Name: " + deckToDelete.getName());

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bộ thẻ \"" + deckToDelete.getName() + "\"?\n\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (cachedAuthToken != null && !cachedAuthToken.isEmpty()) {
                        homeViewModel.deleteDeck(deckId, cachedAuthToken);
                        Toast.makeText(getContext(), "Đang xóa bộ thẻ...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // ========== CREATE DECK LISTENER IMPLEMENTATION ==========

    @Override
    public void onCreateDeckClicked() {
        CreateDeckBottomSheet createDeckBottomSheet = CreateDeckBottomSheet.newInstance();
        createDeckBottomSheet.setCreateDeckListener(this);
        createDeckBottomSheet.show(getParentFragmentManager(), "CreateDeckBottomSheet");
    }

    @Override
    public void onGoToLibrary() {
        // Điều hướng đến Library fragment thay vì tạo folder
        if (getActivity() instanceof MainActivity) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_library);
            }
        }
    }

    @Override
    public void onDeckCreated(String deckName, String deckDescription, int colorId, int categoryId) {
        Log.d(TAG, "Creating new deck: " + deckName);

        if (deckName == null || deckName.trim().isEmpty()) {
            Toast.makeText(getContext(), "Tên bộ thẻ không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (colorId < 1 || colorId > 6) {
            Toast.makeText(getContext(), "Màu được chọn không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryId < 1 || categoryId > 6) {
            Toast.makeText(getContext(), "Thể loại được chọn không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cachedAuthToken != null && !cachedAuthToken.isEmpty()) {
            // Sử dụng description từ input hoặc để trống nếu không có
            String description = deckDescription != null ? deckDescription.trim() : "";
            String imageUrl = String.valueOf(colorId); // Sử dụng colorId làm imageUrl
            boolean isPublic = false;

            // Convert categoryId to category name
            String categoryName = getCategoryName(categoryId);

            Log.d(TAG, "Creating deck with category: " + categoryName + " (ID: " + categoryId + ")");

            // Call API với imageUrl là colorId và category name
            homeViewModel.createDeck(deckName.trim(), description, imageUrl, isPublic, categoryName, cachedAuthToken);
        } else {
            Toast.makeText(getContext(), "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
        }
    }

    private String getCategoryName(int categoryId) {
        String[] categoryNames = {
                "Ngôn ngữ", "Khoa học", "Lịch sử",
                "Toán học", "Nghệ thuật", "Khác"
        };

        if (categoryId >= 1 && categoryId <= categoryNames.length) {
            return categoryNames[categoryId - 1];
        }
        return "Không xác định";
    }

    // ========== DIALOG METHODS ==========

    private void showEditDeckDialog(Deck deck) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_deck, null);

        // Initialize input fields
        TextInputEditText etDeckName = dialogView.findViewById(R.id.et_deck_name);
        TextInputEditText etDeckDescription = dialogView.findViewById(R.id.et_deck_description);
        TextView tvSelectedCategory = dialogView.findViewById(R.id.tv_selected_category);
        TextView tvSelectedColor = dialogView.findViewById(R.id.tv_selected_color);

        // Initialize category options
        LinearLayout categoryOption1 = dialogView.findViewById(R.id.category_option_1);
        LinearLayout categoryOption2 = dialogView.findViewById(R.id.category_option_2);
        LinearLayout categoryOption3 = dialogView.findViewById(R.id.category_option_3);
        LinearLayout categoryOption4 = dialogView.findViewById(R.id.category_option_4);
        LinearLayout categoryOption5 = dialogView.findViewById(R.id.category_option_5);
        LinearLayout categoryOption6 = dialogView.findViewById(R.id.category_option_6);
        LinearLayout[] categoryOptions = {categoryOption1, categoryOption2, categoryOption3, categoryOption4, categoryOption5, categoryOption6};

        // Initialize color options
        View colorOption1 = dialogView.findViewById(R.id.color_option_1);
        View colorOption2 = dialogView.findViewById(R.id.color_option_2);
        View colorOption3 = dialogView.findViewById(R.id.color_option_3);
        View colorOption4 = dialogView.findViewById(R.id.color_option_4);
        View colorOption5 = dialogView.findViewById(R.id.color_option_5);
        View colorOption6 = dialogView.findViewById(R.id.color_option_6);
        View[] colorOptions = {colorOption1, colorOption2, colorOption3, colorOption4, colorOption5, colorOption6};

        // Category and color names
        String[] categoryNames = {"Ngôn ngữ", "Khoa học", "Lịch sử", "Toán học", "Nghệ thuật", "Khác"};
        String[] colorNames = {"Hồng - Xanh lá", "Xanh lá nhạt", "Vàng - Cam", "Tím", "Hồng - Vàng", "Xanh lá đậm"};

        // Set current values
        etDeckName.setText(deck.getName());
        etDeckDescription.setText(deck.getDescription());

        // Get current color ID from imageUrl (default to 1 if not valid)
        int currentColorId = 1;
        try {
            if (deck.getImageUrl() != null && !deck.getImageUrl().isEmpty()) {
                currentColorId = Integer.parseInt(deck.getImageUrl());
                if (currentColorId < 1 || currentColorId > 6) {
                    currentColorId = 1;
                }
            }
        } catch (NumberFormatException e) {
            currentColorId = 1;
        }

        // For now, get category from existing deck data or default to first category
        int currentCategoryId = 1;

        // Try to get category from deck if available
        if (deck.getCategory() != null && !deck.getCategory().isEmpty()) {
            for (int i = 0; i < categoryNames.length; i++) {
                if (categoryNames[i].equals(deck.getCategory())) {
                    currentCategoryId = i + 1;
                    break;
                }
            }
        }

        // Selection state tracking
        final int[] selectedColorId = {currentColorId};
        final int[] selectedCategoryId = {currentCategoryId};

        // Helper method to update category selection
        @SuppressLint("SetTextI18n") Runnable updateCategorySelection = () -> {
            for (int i = 0; i < categoryOptions.length; i++) {
                if (i + 1 == selectedCategoryId[0]) {
                    categoryOptions[i].setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_option_selected));
                    categoryOptions[i].setScaleX(1.05f);
                    categoryOptions[i].setScaleY(1.05f);
                } else {
                    categoryOptions[i].setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_category_option));
                    categoryOptions[i].setScaleX(1.0f);
                    categoryOptions[i].setScaleY(1.0f);
                }
            }
            tvSelectedCategory.setText("Đã chọn: " + categoryNames[selectedCategoryId[0] - 1]);
        };

        // Helper method to update color selection
        @SuppressLint("SetTextI18n") Runnable updateColorSelection = () -> {
            for (int i = 0; i < colorOptions.length; i++) {
                if (i + 1 == selectedColorId[0]) {
                    colorOptions[i].setScaleX(1.2f);
                    colorOptions[i].setScaleY(1.2f);
                    colorOptions[i].setElevation(8f);
                } else {
                    colorOptions[i].setScaleX(1.0f);
                    colorOptions[i].setScaleY(1.0f);
                    colorOptions[i].setElevation(0f);
                }
            }
            tvSelectedColor.setText("Đã chọn: " + colorNames[selectedColorId[0] - 1]);
        };

        // Set initial selections
        updateCategorySelection.run();
        updateColorSelection.run();

        // Set up category click listeners
        for (int i = 0; i < categoryOptions.length; i++) {
            final int categoryId = i + 1;
            categoryOptions[i].setOnClickListener(v -> {
                selectedCategoryId[0] = categoryId;
                updateCategorySelection.run();
            });
        }

        // Set up color click listeners
        for (int i = 0; i < colorOptions.length; i++) {
            final int colorId = i + 1;
            colorOptions[i].setOnClickListener(v -> {
                selectedColorId[0] = colorId;
                updateColorSelection.run();
            });
        }

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Set up button listeners
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String newName = Objects.requireNonNull(etDeckName.getText()).toString().trim();
            String newDescription = Objects.requireNonNull(etDeckDescription.getText()).toString().trim();

            if (newName.isEmpty()) {
                etDeckName.setError("Tên bộ thẻ không được để trống");
                etDeckName.requestFocus();
                return;
            }

            if (cachedAuthToken != null && !cachedAuthToken.isEmpty()) {
                // Log the changes for debugging
                Log.d(TAG, "Updating deck with ID: " + deck.getId());
                Log.d(TAG, "New name: " + newName);
                Log.d(TAG, "New description: " + newDescription);
                Log.d(TAG, "New category: " + getCategoryName(selectedCategoryId[0]) + " (ID: " + selectedCategoryId[0] + ")");
                Log.d(TAG, "New color: " + colorNames[selectedColorId[0] - 1] + " (ID: " + selectedColorId[0] + ")");

                // Create updated deck object with new imageUrl (colorId) and category
                String newImageUrl = String.valueOf(selectedColorId[0]);
                String newCategoryName = getCategoryName(selectedCategoryId[0]);

                // Call API to update deck with category support
                homeViewModel.updateDeck(deck.getId(), newName, newDescription, newImageUrl, false, newCategoryName, cachedAuthToken);

                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();
    }

    private void showShareDeckDialog(Deck deck) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share_deck, null);

        // Initialize views
        TextView txtDeckName = dialogView.findViewById(R.id.txt_deck_name);
        TextView txtDeckDescription = dialogView.findViewById(R.id.txt_deck_description);
        TextInputEditText etReceiverEmail = dialogView.findViewById(R.id.et_receiver_email);
        RadioGroup radioGroupPermission = dialogView.findViewById(R.id.radio_group_permission);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnShare = dialogView.findViewById(R.id.btn_share);
        ProgressBar progressSharing = dialogView.findViewById(R.id.progress_sharing);

        // Set deck info
        txtDeckName.setText(deck.getName());
        txtDeckDescription.setText(deck.getDescription());

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Set up listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnShare.setOnClickListener(v -> {
            String receiverEmail = Objects.requireNonNull(etReceiverEmail.getText()).toString().trim();

            // Validate email
            if (receiverEmail.isEmpty()) {
                etReceiverEmail.setError("Vui lòng nhập email người nhận");
                etReceiverEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(receiverEmail).matches()) {
                etReceiverEmail.setError("Email không hợp lệ");
                etReceiverEmail.requestFocus();
                return;
            }

            // Get permission level
            String permissionLevel = "view"; // Default
            int selectedRadioId = radioGroupPermission.getCheckedRadioButtonId();
            if (selectedRadioId == R.id.radio_view) {
                permissionLevel = "view";
            }

            // Show loading
            progressSharing.setVisibility(View.VISIBLE);
            btnShare.setEnabled(false);
            btnCancel.setEnabled(false);

            // Call share API
            if (cachedAuthToken != null) {
                shareDeck(deck.getId(), receiverEmail, permissionLevel, dialog, progressSharing, btnShare, btnCancel);
            } else {
                hideLoading(progressSharing, btnShare, btnCancel);
                Toast.makeText(getContext(), "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();
    }

    // ========== SHARE DECK METHODS ==========

    private void shareDeck(long deckId, String receiverEmail, String permissionLevel,
                           AlertDialog dialog, ProgressBar progressSharing,
                           Button btnShare, Button btnCancel) {

        Log.d(TAG, "Sharing deck " + deckId + " with " + receiverEmail);

        // Reset previous states
        homeViewModel.resetShareStates();

        // Call share API
        homeViewModel.shareDeck(deckId, receiverEmail, permissionLevel, cachedAuthToken);

        // Observe loading state
        homeViewModel.getShareLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    showLoading(progressSharing, btnShare, btnCancel);
                } else {
                    hideLoading(progressSharing, btnShare, btnCancel);
                }
            }
        });

        // Observe share result
        homeViewModel.getShareSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success) {
                    Toast.makeText(getContext(), "Đã gửi lời mời chia sẻ thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Remove observers after successful share
                    removeShareObservers();
                }
            }
        });

        // Observe share error
        homeViewModel.getShareError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                removeShareObservers();
            }
        });
    }

    private void showLoading(ProgressBar progressSharing, Button btnShare, Button btnCancel) {
        progressSharing.setVisibility(View.VISIBLE);
        btnShare.setEnabled(false);
        btnCancel.setEnabled(false);
    }

    private void hideLoading(ProgressBar progressSharing, Button btnShare, Button btnCancel) {
        progressSharing.setVisibility(View.GONE);
        btnShare.setEnabled(true);
        btnCancel.setEnabled(true);
    }

    private void removeShareObservers() {
        // Remove observers to prevent memory leaks and multiple triggers
        homeViewModel.getShareLoading().removeObservers(getViewLifecycleOwner());
        homeViewModel.getShareSuccess().removeObservers(getViewLifecycleOwner());
        homeViewModel.getShareError().removeObservers(getViewLifecycleOwner());
    }

    // ========== DIFFUTIL CALLBACK ==========

    // DiffUtil callback for efficient RecyclerView updates
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