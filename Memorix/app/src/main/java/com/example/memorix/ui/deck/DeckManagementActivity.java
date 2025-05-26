package com.example.memorix.ui.deck;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

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
            Toast.makeText(this, "Bắt đầu học " + allCards.size() + " thẻ!", Toast.LENGTH_SHORT).show();
        });
    }

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
        String info = "Loại: " + card.getType().getDisplayName() + "\n" +
                "Câu hỏi: " + card.getQuestion() + "\n" +
                "Số lần ôn: " + card.getReviewCount() + "\n" +
                "Tỷ lệ đúng: " + String.format("%.1f", card.getAccuracyRate()) + "%";

        new AlertDialog.Builder(this)
                .setTitle("Thông tin thẻ")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showCardDetail(Card card) {
        new AlertDialog.Builder(this)
                .setTitle("Chi tiết thẻ")
                .setMessage(card.getDisplayContent())
                .setPositiveButton("Chỉnh sửa", (dialog, which) -> {
                    Intent intent = new Intent(this, EditCardActivity.class);
                    intent.putExtra("card", card);
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showDeleteConfirmDialog(Card card) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thẻ")
                .setMessage("Bạn có chắc chắn muốn xóa thẻ này?\n\n" + card.getQuestion())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa thẻ khỏi danh sách
                    allCards.remove(card);
                    tvTotalCards.setText(String.valueOf(allCards.size()));
                    filterCards(spinnerCardType.getSelectedItemPosition());
                    Toast.makeText(this, "Đã xóa thẻ!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void updateCardCount() {
        tvTotalCards.setText(String.valueOf(allCards.size()));
    }
}