package com.example.memorix.ui.deck;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.memorix.R;
import com.example.memorix.data.Deck;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageDeckFragment extends Fragment implements DeckActionListener {
    private RecyclerView recyclerView;
    private DeckAdapter deckAdapter;
    private List<Deck> deckList;
    private FloatingActionButton fabAddDeck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_deck, container, false);

        // Khởi tạo RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_decks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tạo dữ liệu mẫu
        deckList = createSampleData();

        // Khởi tạo adapter với listener
        deckAdapter = new DeckAdapter(deckList, this);
        recyclerView.setAdapter(deckAdapter);

        // Thiết lập nút thêm bộ thẻ mới
        fabAddDeck = view.findViewById(R.id.fab_add_deck);
        fabAddDeck.setOnClickListener(v -> {
            // Xử lý sự kiện khi bấm nút thêm bộ thẻ mới
            Toast.makeText(getContext(), "Thêm bộ thẻ mới", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình tạo bộ thẻ mới
        });

        return view;
    }

    // Interface implementation để xử lý các hành động từ menu
    @Override
    public void onEditDeck(int position) {
        Deck deck = deckList.get(position);
        Toast.makeText(getContext(), "Chỉnh sửa bộ thẻ: " + deck.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Mở màn hình chỉnh sửa bộ thẻ
    }

    @Override
    public void onShareDeck(int position) {
        Deck deck = deckList.get(position);
        Toast.makeText(getContext(), "Chia sẻ bộ thẻ: " + deck.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Mở dialog chia sẻ
    }

    @Override
    public void onResetProgress(int position) {
        Deck deck = deckList.get(position);
        // Reset số thẻ đã thành thạo về 0
        deck.setMasteredCount(0);
        deckAdapter.notifyItemChanged(position);
        Toast.makeText(getContext(), "Đã đặt lại tiến độ bộ thẻ: " + deck.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteDeck(int position) {
        // Xóa bộ thẻ khỏi danh sách
        Deck removedDeck = deckList.remove(position);
        deckAdapter.notifyItemRemoved(position);
        Toast.makeText(getContext(), "Đã xóa bộ thẻ: " + removedDeck.getName(), Toast.LENGTH_SHORT).show();

        // TODO: Xóa bộ thẻ từ cơ sở dữ liệu
    }

    // Tạo dữ liệu mẫu
    private List<Deck> createSampleData() {
        List<Deck> decks = new ArrayList<>();

        Deck deck1 = new Deck(1, "Tiếng Việt", "Vietnamese language flashcards", 25, 15);
        Deck deck2 = new Deck(2, "Tiếng Anh", "English vocabulary flashcards", 30, 10);
        Deck deck3 = new Deck(3, "Toán học", "Math formula flashcards", 20, 5);

        decks.add(deck1);
        decks.add(deck2);
        decks.add(deck3);

        return decks;
    }
}