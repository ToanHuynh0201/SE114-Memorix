package com.example.memorix.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.memorix.R;
import com.example.memorix.data.Deck;
import com.example.memorix.ui.deck.DeckManagementActivity;
import com.example.memorix.ui.deck.adapter.DeckActionListener;
import com.example.memorix.ui.deck.adapter.DeckAdapter;
import com.example.memorix.ui.deck.bottomsheet.AddOptionsBottomSheet;
import com.example.memorix.ui.deck.bottomsheet.CreateDeckBottomSheet;
import com.example.memorix.ui.deck.bottomsheet.CreateFolderBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements DeckActionListener,
        AddOptionsBottomSheet.OptionClickListener,
        CreateDeckBottomSheet.CreateDeckListener,
        CreateFolderBottomSheet.CreateFolderListener{

    private RecyclerView recyclerFlashcardSets;
    private LinearLayout layoutEmptyState;
    private Button btnCreateFirstSet;
    private DeckAdapter deckAdapter;
    private List<Deck> deckList;

    private FloatingActionButton fabAdd;
    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerFlashcardSets = view.findViewById(R.id.recycler_flashcard_sets);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnCreateFirstSet = view.findViewById(R.id.btn_create_first_set);
        fabAdd = view.findViewById(R.id.fab_add_deck);

        // Khởi tạo RecyclerView, adapter và các thành phần khác
        setupRecyclerView();
        checkEmptyState();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    private void setupRecyclerView() {
        recyclerFlashcardSets.setLayoutManager(new LinearLayoutManager(getContext()));
        deckList = createSampleData();
        deckAdapter = new DeckAdapter(deckList, this);
        recyclerFlashcardSets.setAdapter(deckAdapter);
    }

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

    private void checkEmptyState() {
        // Kiểm tra và hiển thị trạng thái trống nếu không có bộ thẻ nào
        // Ví dụ:
        boolean hasData = !deckList.isEmpty(); // Thay bằng logic kiểm tra dữ liệu thực tế

        if (hasData) {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerFlashcardSets.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerFlashcardSets.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showBottomSheet());

        btnCreateFirstSet.setOnClickListener(v ->  onCreateDeckClicked());
    }

    private void showBottomSheet() {
        AddOptionsBottomSheet bottomSheet = AddOptionsBottomSheet.newInstance();
        bottomSheet.setOptionClickListener(this);
        bottomSheet.show(getParentFragmentManager(), "AddOptionsBottomSheet");
    }

    @Override
    public void onEditDeck(int position) {

    }

    @Override
    public void onShareDeck(int position) {

    }

    @Override
    public void onResetProgress(int position) {

    }

    @Override
    public void onDeleteDeck(int position) {
        // Xóa bộ thẻ khỏi danh sách
//        Deck removedDeck = deckList.remove(position);
        deckAdapter.notifyItemRemoved(position);
        checkEmptyState();
    }

    @Override
    public void onDeckClick(Deck deck, int position) {
        Intent intent = new Intent(getContext(), DeckManagementActivity.class);
        intent.putExtra("deck_id", deck.getId());
        startActivity(intent);
    }

    @Override
    public void onCreateDeckClicked() {
        // Đóng bottom sheet hiện tại và mở CreateDeckBottomSheet
        CreateDeckBottomSheet createDeckBottomSheet = CreateDeckBottomSheet.newInstance();
        createDeckBottomSheet.setCreateDeckListener(this);
        createDeckBottomSheet.show(getParentFragmentManager(), "CreateDeckBottomSheet");
    }

    @Override
    public void onCreateFolderClicked() {
// Đóng bottom sheet hiện tại và mở CreateFolderBottomSheet
        CreateFolderBottomSheet createFolderBottomSheet = CreateFolderBottomSheet.newInstance();
        createFolderBottomSheet.setCreateFolderListener(this);
        createFolderBottomSheet.show(getParentFragmentManager(), "CreateFolderBottomSheet");
    }

    @Override
    public void onDeckCreated(String deckName) {
// Tạo bộ thẻ mới và thêm vào danh sách
        int newId = deckList.size() + 1;
        Deck newDeck = new Deck(newId, deckName, "Created by user", 0, 0);
        deckList.add(newDeck);
        deckAdapter.notifyItemInserted(deckList.size() - 1);

        // Cập nhật trạng thái empty state
        checkEmptyState();

        Toast.makeText(getContext(), "Đã tạo bộ thẻ: " + deckName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFolderCreated(String folderName) {

    }
}