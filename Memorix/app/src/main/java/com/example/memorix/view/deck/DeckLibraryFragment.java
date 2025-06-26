package com.example.memorix.view.deck;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private EditText etSearch;
    private RecyclerView rvAllDecks;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private NestedScrollView scrollView;

    private DeckLibraryAdapter deckAdapter;
    private DeckLibraryViewModel viewModel;

    private String cachedAuthToken;

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
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DeckLibraryViewModel.class);
    }

    private void setupRecyclerView() {
        deckAdapter = new DeckLibraryAdapter(new ArrayList<>(), this);
        rvAllDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllDecks.setAdapter(deckAdapter);
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterDecks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeData() {
        // Observe public decks from API
        viewModel.getPublicDecks().observe(getViewLifecycleOwner(), decks -> {
            if (decks != null) {
                viewModel.setOriginalDecks(decks);
            }
        });

        // Observe filtered decks
        viewModel.getFilteredDecks().observe(getViewLifecycleOwner(), decks -> {
            if (decks != null) {
                deckAdapter.updateData(decks);
                updateEmptyState(decks.isEmpty());
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
            // You can show a loading indicator during clone operation
            if (isLoading) {
                // Optional: Show loading dialog or disable buttons
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

    // Implement DeckLibraryListener methods
    @Override
    public void onDeckClick(Deck deck, int position) {
        showDeckPreviewDialog(deck);
    }

    @Override
    public void onCloneDeck(Deck deck, int position) {
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
        viewModel.cloneDeck(deck, cachedAuthToken);
    }

    // Add this method to get token - implement according to your auth system
    private String getAuthToken() {
        if (cachedAuthToken != null) {
            return cachedAuthToken;
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        cachedAuthToken = prefs.getString("access_token", null);
        return cachedAuthToken;
    }
}