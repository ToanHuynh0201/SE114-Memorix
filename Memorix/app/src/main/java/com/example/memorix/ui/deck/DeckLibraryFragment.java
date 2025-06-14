package com.example.memorix.ui.deck;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.data.Deck;
import com.example.memorix.ui.deck.adapter.DeckActionListener;
import com.example.memorix.ui.deck.adapter.DeckAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeckLibraryFragment extends Fragment implements DeckActionListener {
    private EditText etSearch;
    private RecyclerView rvEnglishDecks;
    private RecyclerView rvPopularDecks;
    private RecyclerView rvLawDecks;
    private DeckAdapter englishAdapter;
    private DeckAdapter popularAdapter;
    private DeckAdapter lawAdapter;
    private TextView tvSeeAllEnglish;

    private List<Deck> englishDecks;
    private List<Deck> popularDecks;
    private List<Deck> lawDecks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deck_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupSearchFunctionality();
        loadSampleData();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvEnglishDecks = view.findViewById(R.id.rv_english_decks);
        rvPopularDecks = view.findViewById(R.id.rv_popular_decks);
        rvLawDecks = view.findViewById(R.id.rv_law_decks);
        tvSeeAllEnglish = view.findViewById(R.id.tvSeeAllEnglish);

        tvSeeAllEnglish.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ManageDeckActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        // Setup English decks RecyclerView
        englishDecks = new ArrayList<>();
        englishAdapter = new DeckAdapter(englishDecks, this);
        rvEnglishDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEnglishDecks.setAdapter(englishAdapter);

        // Setup Popular decks RecyclerView
        popularDecks = new ArrayList<>();
        popularAdapter = new DeckAdapter(popularDecks, this);
        rvPopularDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPopularDecks.setAdapter(popularAdapter);

        // Setup Law decks RecyclerView
        lawDecks = new ArrayList<>();
        lawAdapter = new DeckAdapter(lawDecks, this);
        rvLawDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLawDecks.setAdapter(lawAdapter);
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDecks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterDecks(String query) {
        // Implement search functionality here
        // This is a basic example - you can enhance it based on your needs
        if (query.isEmpty()) {
            loadSampleData();
        } else {
            // Filter decks based on query
            // For now, just show a toast
            Toast.makeText(getContext(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadSampleData() {
        // Load sample English decks
        englishDecks.clear();
        englishDecks.add(new Deck(1, "English Regular Expressions", "Learn common English expressions and idioms", 598, 358));
        englishDecks.add(new Deck(2, "Common English Phrases", "Essential phrases for daily conversation", 2196, 1318));
        englishAdapter.notifyDataSetChanged();

        // Load sample Popular decks
        popularDecks.clear();
        popularDecks.add(new Deck(3, "English Regular Expressions", "Learn common English expressions and idioms", 598, 358));
        popularDecks.add(new Deck(4, "MCAT Preparation", "Medical College Admission Test preparation", 2597, 1558));
        popularAdapter.notifyDataSetChanged();

        // Load sample Law decks
        lawDecks.clear();
        lawDecks.add(new Deck(5, "Criminal Law", "Fundamental concepts of criminal law", 450, 270));
        lawDecks.add(new Deck(6, "Family Law", "Family law principles and cases", 320, 192));
        lawAdapter.notifyDataSetChanged();
    }



    // Implement DeckActionListener methods
    @Override
    public void onEditDeck(int position) {
        Toast.makeText(getContext(), "Edit deck at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareDeck(int position) {
        Toast.makeText(getContext(), "Share deck at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResetProgress(int position) {
        Toast.makeText(getContext(), "Reset progress for deck at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteDeck(int position) {
        Toast.makeText(getContext(), "Delete deck at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeckClick(Deck deck, int position) {
        showDeckInfoDialog(deck);
    }

    @SuppressLint("SetTextI18n")
    private void showDeckInfoDialog(Deck deck) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_preview_deck, null);
        builder.setView(dialogView);

        // Create dialog
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Get views
        TextView tvDeckName = dialogView.findViewById(R.id.tv_deck_name);
        TextView tvCardCount = dialogView.findViewById(R.id.tv_card_count);
        TextView tvDeckDescription = dialogView.findViewById(R.id.tv_deck_description);
        AppCompatButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        AppCompatButton btnStartLearning = dialogView.findViewById(R.id.btn_start_learning);

        // Set data
        tvDeckName.setText(deck.getName());
        tvCardCount.setText(deck.getCardCount() + " thẻ");

        // Set description, nếu không có mô tả thì hiển thị text mặc định
        String description = deck.getDescription();
        if (description == null || description.trim().isEmpty()) {
            tvDeckDescription.setText("Không có mô tả cho bộ flashcard này.");
        } else {
            tvDeckDescription.setText(description);
        }

        // Set click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnStartLearning.setOnClickListener(v -> {
            dialog.dismiss();
            // Chuyển đến activity học
            Intent intent = new Intent(getContext(), DeckManagementActivity.class);
            intent.putExtra("deck_id", deck.getId());
            startActivity(intent);
        });

        dialog.show();
    }
}