package com.example.memorix.ui.deck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorix.R;
import com.example.memorix.model.Deck;
import com.example.memorix.ui.deck.adapter.DeckActionListener;
import com.example.memorix.ui.deck.adapter.DeckAdapter;

import java.util.ArrayList;
import java.util.List;

public class ManageDeckActivity extends AppCompatActivity implements DeckActionListener{
    private DeckAdapter deckAdapter;
    private List<Deck> deckList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_deck);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view_decks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo dữ liệu mẫu
        deckList = createSampleData();

        // Khởi tạo adapter với listener
        deckAdapter = new DeckAdapter(deckList, this);
        recyclerView.setAdapter(deckAdapter);
    }

    // Interface implementation để xử lý các hành động từ menu
    @Override
    public void onEditDeck(int position) {
        Deck deck = deckList.get(position);
        Toast.makeText(this, "Chỉnh sửa bộ thẻ: " + deck.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareDeck(int position) {
        Deck deck = deckList.get(position);
        Toast.makeText(this, "Chia sẻ bộ thẻ: " + deck.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResetProgress(int position) {
    }

    @Override
    public void onDeleteDeck(int position) {
        // Xóa bộ thẻ khỏi danh sách
        Deck removedDeck = deckList.remove(position);
        deckAdapter.notifyItemRemoved(position);
        Toast.makeText(this, "Đã xóa bộ thẻ: " + removedDeck.getName(), Toast.LENGTH_SHORT).show();

        // Cập nhật trạng thái trống
//        updateEmptyState();
    }

    @Override
    public void onDeckClick(Deck deck, int position) {
        Intent intent = new Intent(this, DeckManagementActivity.class);
        intent.putExtra("deck_id", deck.getId());
        startActivity(intent);
    }

    // Tạo dữ liệu mẫu
    private List<Deck> createSampleData() {
        List<Deck> decks = new ArrayList<>();

        Deck deck1 = new Deck(1, "Tiếng Việt", "Vietnamese language flashcards", 25, null, false);
        Deck deck2 = new Deck(2, "Tiếng Anh", "English vocabulary flashcards", 30, null, false);
        Deck deck3 = new Deck(3, "Toán học", "Math formula flashcards", 20, null, false);

        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);

        return decks;
    }
}