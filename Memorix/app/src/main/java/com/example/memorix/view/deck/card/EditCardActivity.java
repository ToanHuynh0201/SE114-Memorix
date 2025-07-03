package com.example.memorix.view.deck.card;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.memorix.R;
import com.example.memorix.data.remote.Repository.FlashcardRepository;
import com.example.memorix.model.Card;
import com.example.memorix.model.CardType;
import com.example.memorix.viewmodel.EditCardViewModel;
import com.example.memorix.viewmodel.EditCardViewModelFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class EditCardActivity extends AppCompatActivity {
    private Toolbar toolbar;

    // Basic card views
    private LinearLayout layoutBasic;
    private EditText etBasicQuestion, etBasicAnswer;

    // Multiple choice views
    private LinearLayout layoutMultipleChoice;
    private EditText etMcQuestion, etMcOption1, etMcOption2, etMcOption3, etMcOption4;
    private RadioGroup radioGroupCorrectAnswer;
    private RadioButton rbCorrect1, rbCorrect2, rbCorrect3, rbCorrect4;

    // Fill in blank views
    private LinearLayout layoutFillInBlank;
    private EditText etFibQuestion, etFibAnswer;
    private AppCompatButton btnSaveCard, btnCancel;

    private Card currentCard;
    private boolean isEditMode = false;
    private EditCardViewModel viewModel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_card);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupToolbar();
        setupViewModel();
        setupObservers();
        loadCardData();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getCardLiveData().observe(this, card -> {
            if (card != null) {
                currentCard = card;
                isEditMode = true;
                populateCardData(card);
                setupListeners();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            setControlsEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Đã cập nhật thẻ!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getSaveErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewModel() {
        FlashcardRepository repository = new FlashcardRepository();
        EditCardViewModelFactory factory = new EditCardViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(EditCardViewModel.class);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Basic layout
        layoutBasic = findViewById(R.id.layoutBasic);
        etBasicQuestion = findViewById(R.id.etBasicQuestion);
        etBasicAnswer = findViewById(R.id.etBasicAnswer);

        // Multiple choice layout
        layoutMultipleChoice = findViewById(R.id.layoutMultipleChoice);
        etMcQuestion = findViewById(R.id.etMcQuestion);
        etMcOption1 = findViewById(R.id.etMcOption1);
        etMcOption2 = findViewById(R.id.etMcOption2);
        etMcOption3 = findViewById(R.id.etMcOption3);
        etMcOption4 = findViewById(R.id.etMcOption4);
        radioGroupCorrectAnswer = findViewById(R.id.radioGroupCorrectAnswer);
        rbCorrect1 = findViewById(R.id.rbCorrect1);
        rbCorrect2 = findViewById(R.id.rbCorrect2);
        rbCorrect3 = findViewById(R.id.rbCorrect3);
        rbCorrect4 = findViewById(R.id.rbCorrect4);

        // Fill in blank layout
        layoutFillInBlank = findViewById(R.id.layoutFillInBlank);
        etFibQuestion = findViewById(R.id.etFibQuestion);
        etFibAnswer = findViewById(R.id.etFibAnswer);

        btnSaveCard = findViewById(R.id.btnSaveCard);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String title = "Chỉnh sửa thẻ";
            getSupportActionBar().setTitle(title);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnSaveCard.setOnClickListener(v -> saveCard());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCardData() {
        int cardId = getIntent().getIntExtra("card_id", -1);

        if (cardId == -1) {
            isEditMode = false;
            return;
        }

        viewModel.loadCard(cardId);
    }

    private void populateCardData(Card card) {
        CardType cardType = card.getCardType();
        JsonObject content = card.getContent();

        if (content == null) return;

        switch (cardType) {
            case BASIC:
                showLayoutForCardType(CardType.BASIC);
                if (content.has("front")) {
                    etBasicQuestion.setText(content.get("front").getAsString());
                }
                if (content.has("back")) {
                    etBasicAnswer.setText(content.get("back").getAsString());
                }
                break;

            case MULTIPLE_CHOICE:
                showLayoutForCardType(CardType.MULTIPLE_CHOICE);
                if (content.has("question")) {
                    etMcQuestion.setText(content.get("question").getAsString());
                }

                if (content.has("options")) {
                    JsonArray optionsArray = content.getAsJsonArray("options");
                    if (optionsArray.size() >= 4) {
                        etMcOption1.setText(optionsArray.get(0).getAsString());
                        etMcOption2.setText(optionsArray.get(1).getAsString());
                        etMcOption3.setText(optionsArray.get(2).getAsString());
                        etMcOption4.setText(optionsArray.get(3).getAsString());
                    }

                    if (content.has("answer")) {
                        String correctAnswer = content.get("answer").getAsString();
                        for (int i = 0; i < optionsArray.size(); i++) {
                            if (optionsArray.get(i).getAsString().equals(correctAnswer)) {
                                switch (i) {
                                    case 0: rbCorrect1.setChecked(true); break;
                                    case 1: rbCorrect2.setChecked(true); break;
                                    case 2: rbCorrect3.setChecked(true); break;
                                    case 3: rbCorrect4.setChecked(true); break;
                                }
                                break;
                            }
                        }
                    }
                }
                break;

            case FILL_IN_BLANK:
                showLayoutForCardType(CardType.FILL_IN_BLANK);
                if (content.has("text")) {
                    etFibQuestion.setText(content.get("text").getAsString());
                }
                if (content.has("answer")) {
                    etFibAnswer.setText(content.get("answer").getAsString());
                }
                break;
        }
    }

    private void setControlsEnabled(boolean enabled) {
        etBasicQuestion.setEnabled(enabled);
        etBasicAnswer.setEnabled(enabled);
        etMcQuestion.setEnabled(enabled);
        etMcOption1.setEnabled(enabled);
        etMcOption2.setEnabled(enabled);
        etMcOption3.setEnabled(enabled);
        etMcOption4.setEnabled(enabled);
        etFibQuestion.setEnabled(enabled);
        etFibAnswer.setEnabled(enabled);

        btnSaveCard.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
    }

    private void showLayoutForCardType(CardType cardType) {
        // Ẩn tất cả layouts
        layoutBasic.setVisibility(View.GONE);
        layoutMultipleChoice.setVisibility(View.GONE);
        layoutFillInBlank.setVisibility(View.GONE);

        // Hiển thị layout tương ứng với cardType
        switch (cardType) {
            case BASIC:
                layoutBasic.setVisibility(View.VISIBLE);
                break;
            case MULTIPLE_CHOICE:
                layoutMultipleChoice.setVisibility(View.VISIBLE);
                break;
            case FILL_IN_BLANK:
                layoutFillInBlank.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void saveCard() {
        if (!validateInput()) {
            return;
        }

        if (currentCard == null) {
            return;
        }

        CardType cardType = currentCard.getCardType();

        switch (cardType) {
            case BASIC:
                saveBasicCard();
                break;
            case MULTIPLE_CHOICE:
                saveMultipleChoiceCard();
                break;
            case FILL_IN_BLANK:
                saveFillInBlankCard();
                break;
        }

        viewModel.updateCard(currentCard);
    }

    private boolean validateInput() {
        if (currentCard == null) {
            return false;
        }

        CardType cardType = currentCard.getCardType();

        switch (cardType) {
            case BASIC:
                if (etBasicQuestion.getText().toString().trim().isEmpty()) {
                    etBasicQuestion.setError("Vui lòng nhập câu hỏi");
                    return false;
                }
                if (etBasicAnswer.getText().toString().trim().isEmpty()) {
                    etBasicAnswer.setError("Vui lòng nhập câu trả lời");
                    return false;
                }
                break;

            case MULTIPLE_CHOICE:
                if (etMcQuestion.getText().toString().trim().isEmpty()) {
                    etMcQuestion.setError("Vui lòng nhập câu hỏi");
                    return false;
                }
                if (etMcOption1.getText().toString().trim().isEmpty() ||
                        etMcOption2.getText().toString().trim().isEmpty() ||
                        etMcOption3.getText().toString().trim().isEmpty() ||
                        etMcOption4.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ 4 lựa chọn", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (radioGroupCorrectAnswer.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(this, "Vui lòng chọn đáp án đúng", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;

            case FILL_IN_BLANK:
                if (etFibQuestion.getText().toString().trim().isEmpty()) {
                    etFibQuestion.setError("Vui lòng nhập câu hỏi");
                    return false;
                }
                if (etFibAnswer.getText().toString().trim().isEmpty()) {
                    etFibAnswer.setError("Vui lòng nhập đáp án");
                    return false;
                }
                break;
        }

        return true;
    }

    private void saveBasicCard() {
        String question = etBasicQuestion.getText().toString().trim();
        String answer = etBasicAnswer.getText().toString().trim();

        Card basicCard = Card.createBasicCard(currentCard.getDeckId(), question, answer);

        // Giữ lại flashcardId nếu đang edit
        if (isEditMode) {
            basicCard.setFlashcardId(currentCard.getFlashcardId());
        }

        currentCard = basicCard;
    }

    private void saveMultipleChoiceCard() {
        String question = etMcQuestion.getText().toString().trim();

        List<String> options = new ArrayList<>();
        options.add(etMcOption1.getText().toString().trim());
        options.add(etMcOption2.getText().toString().trim());
        options.add(etMcOption3.getText().toString().trim());
        options.add(etMcOption4.getText().toString().trim());

        // Xác định đáp án đúng
        int correctIndex = 0;
        int checkedId = radioGroupCorrectAnswer.getCheckedRadioButtonId();
        if (checkedId == R.id.rbCorrect2) correctIndex = 1;
        else if (checkedId == R.id.rbCorrect3) correctIndex = 2;
        else if (checkedId == R.id.rbCorrect4) correctIndex = 3;

        String correctAnswer = options.get(correctIndex);

        Card mcCard = Card.createMultipleChoiceCard(currentCard.getDeckId(), question, options, correctAnswer);

        // Giữ lại flashcardId nếu đang edit
        if (isEditMode) {
            mcCard.setFlashcardId(currentCard.getFlashcardId());
        }

        currentCard = mcCard;
    }

    private void saveFillInBlankCard() {
        String question = etFibQuestion.getText().toString().trim();
        String answer = etFibAnswer.getText().toString().trim();

        // Tạo content với field "text" thay vì "front"
        JsonObject content = new JsonObject();
        content.addProperty("text", question);
        content.addProperty("answer", answer);

        Card fibCard = new Card();
        fibCard.setDeckId(currentCard.getDeckId());
        fibCard.setCardType(CardType.FILL_IN_BLANK);
        fibCard.setContent(content);

        // Giữ lại flashcardId nếu đang edit
        if (isEditMode) {
            fibCard.setFlashcardId(currentCard.getFlashcardId());
        }

        currentCard = fibCard;
    }
}