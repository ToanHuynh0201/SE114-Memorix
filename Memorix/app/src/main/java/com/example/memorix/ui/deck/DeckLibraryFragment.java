package com.example.memorix.ui.deck;

import android.annotation.SuppressLint;
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
import com.example.memorix.model.Deck;
import com.example.memorix.ui.deck.adapter.DeckLibraryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeckLibraryFragment extends Fragment implements DeckLibraryAdapter.DeckLibraryListener {
    private EditText etSearch;
    private RecyclerView rvAllDecks;
    private DeckLibraryAdapter deckAdapter;

    private List<Deck> allDecks;
    private List<Deck> originalDecks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deck_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearchFunctionality();
        loadSampleData();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvAllDecks = view.findViewById(R.id.rv_all_decks);
    }

    private void setupRecyclerView() {
        allDecks = new ArrayList<>();
        originalDecks = new ArrayList<>();
        deckAdapter = new DeckLibraryAdapter(allDecks, this);
        rvAllDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllDecks.setAdapter(deckAdapter);
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

    @SuppressLint("NotifyDataSetChanged")
    private void filterDecks(String query) {
        if (query.isEmpty()) {
            allDecks.clear();
            allDecks.addAll(originalDecks);
        } else {
            List<Deck> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase();

            for (Deck deck : originalDecks) {
                if (deck.getName().toLowerCase().contains(lowerCaseQuery) ||
                        (deck.getDescription() != null && deck.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(deck);
                }
            }

            allDecks.clear();
            allDecks.addAll(filteredList);
        }
        deckAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadSampleData() {
        // Clear existing data
        allDecks.clear();
        originalDecks.clear();

        // Mix of different categories for variety
        List<Deck> sampleDecks = new ArrayList<>();

        // Popular/Trending decks
        sampleDecks.add(new Deck(1, "🚀 IELTS Writing Task 2 Master", "Chinh phục IELTS Writing với 200+ bài mẫu band 8.0+ được examiner khuyên dùng", 1250, "Comprehensive IELTS writing guide with examiner-approved templates", false));
        sampleDecks.add(new Deck(2, "💼 Business English Pro", "Tiếng Anh kinh doanh từ cơ bản đến nâng cao cho môi trường công việc chuyên nghiệp", 890, "Professional English for business communication and meetings", false));
        sampleDecks.add(new Deck(3, "🎯 TOEIC 990 Listening Master", "Phương pháp bí mật đạt điểm tối đa TOEIC Listening với 500+ bài tập thực chiến", 756, "Secret techniques used by 990 TOEIC achievers", false));

        // English learning decks
        sampleDecks.add(new Deck(4, "📚 English Idioms & Phrases", "1000+ thành ngữ và cụm từ tiếng Anh thông dụng nhất với ví dụ thực tế", 598, "Master everyday English expressions used by native speakers", false));
        sampleDecks.add(new Deck(5, "🎬 Movie English Quotes", "Học tiếng Anh qua 500+ câu thoại từ các bộ phim Hollywood nổi tiếng", 423, "Learn English through famous movie dialogues and quotes", false));
        sampleDecks.add(new Deck(6, "🌟 Advanced Vocabulary Builder", "Nâng tầm vốn từ vựng lên level chuyên gia với 800+ từ vựng nâng cao", 789, "Elevate your vocabulary to expert level with advanced words", false));

        // Academic subjects
        sampleDecks.add(new Deck(7, "⚖️ Luật Hình sự Việt Nam 2024", "Bộ luật hình sự mới nhất với các điều khoản và án lệ cập nhật", 450, "Latest Vietnamese criminal law with updated articles and precedents", false));
        sampleDecks.add(new Deck(8, "🧮 Toán Cao cấp A1", "Giải tích 1 từ cơ bản đến nâng cao với 300+ công thức và bài tập", 520, "Calculus fundamentals with formulas and practice problems", false));
        sampleDecks.add(new Deck(9, "🔬 Hóa Đại cương", "Hóa học đại cương với các phản ứng và công thức quan trọng", 380, "General chemistry with important reactions and formulas", false));

        // Technology & Programming
        sampleDecks.add(new Deck(10, "💻 Java Programming Basics", "Học lập trình Java từ zero đến hero với 200+ ví dụ code thực tế", 675, "Complete Java programming guide with practical examples", false));
        sampleDecks.add(new Deck(11, "🌐 HTML & CSS Essentials", "Thiết kế web responsive với HTML5 và CSS3 hiện đại", 445, "Modern web design with HTML5 and CSS3 techniques", false));
        sampleDecks.add(new Deck(12, "📱 Android Development", "Phát triển ứng dụng Android với Kotlin và các best practices", 590, "Android app development using Kotlin and modern practices", false));

        // Medical & Health
        sampleDecks.add(new Deck(13, "🏥 Thuật ngữ Y khoa", "500+ thuật ngữ y khoa tiếng Anh-Việt cần thiết cho sinh viên y", 320, "Essential English-Vietnamese medical terminology for students", false));
        sampleDecks.add(new Deck(14, "💊 Dược lý học cơ bản", "Các nhóm thuốc và cơ chế tác dụng trong điều trị", 280, "Basic pharmacology with drug groups and mechanisms", false));

        // Language learning
        sampleDecks.add(new Deck(15, "🇯🇵 Hiragana & Katakana", "Bảng chữ cái tiếng Nhật cơ bản với cách viết và phát âm chuẩn", 210, "Japanese alphabet with proper writing and pronunciation", false));
        sampleDecks.add(new Deck(16, "🇰🇷 Korean Basic Vocabulary", "1000 từ vựng tiếng Hàn cơ bản cho người mới bắt đầu", 350, "Essential Korean vocabulary for beginners with pronunciation", false));

        // Add all sample decks to both lists
        allDecks.addAll(sampleDecks);
        originalDecks.addAll(sampleDecks);

        deckAdapter.notifyDataSetChanged();
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
        tvCardCount.setText(deck.getCardCount() + " thẻ");

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
            cloneDeck(deck);
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
        Toast.makeText(getContext(),
                "🎉 Đã clone \"" + deck.getName() + "\" thành công!\nKiểm tra trong thư viện cá nhân của bạn.",
                Toast.LENGTH_LONG).show();

        // Here you would implement the actual cloning logic:
        // 1. Copy deck data to user's personal library
        // 2. Create new deck instance with user as owner
        // 3. Copy all cards from original deck
        // 4. Update database
        // 5. Navigate to the new deck or refresh UI
    }
}
