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
    private RadioGroup radioGroupCardType;
    private RadioButton rbBasic, rbMultipleChoice, rbFillInBlank;
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
        setupViewModel(); // Thêm dòng này
        setupObservers(); // Thêm dòng này
        loadCardData(); // Sửa lại hàm này
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getCardLiveData().observe(this, card -> {
            if (card != null) {
                currentCard = card;
                isEditMode = true;
                populateCardData(card);
                setupListeners(); // Setup lại listeners sau khi có data
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            // Disable/enable các controls khi đang loading
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
        // Khởi tạo repository và viewModel
        FlashcardRepository repository = new FlashcardRepository();
        EditCardViewModelFactory factory = new EditCardViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(EditCardViewModel.class);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        radioGroupCardType = findViewById(R.id.radioGroupCardType);
        rbBasic = findViewById(R.id.rbBasic);
        rbMultipleChoice = findViewById(R.id.rbMultipleChoice);
        rbFillInBlank = findViewById(R.id.rbFillInBlank);

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
        progressBar = findViewById(R.id.progressBar); // Thêm dòng này
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Kiểm tra xem có card được truyền vào không để set title
            Card card = (Card) getIntent().getSerializableExtra("card");
            String title = (card != null) ? "Chỉnh sửa thẻ" : "Thêm thẻ mới";
            getSupportActionBar().setTitle(title);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void setupListeners() {
        radioGroupCardType.setOnCheckedChangeListener((group, checkedId) -> showLayoutForCardType());

        // Disable radio buttons nếu đang edit (không cho đổi loại card)
        if (isEditMode) {
            rbBasic.setEnabled(false);
            rbMultipleChoice.setEnabled(false);
            rbFillInBlank.setEnabled(false);
        }

        btnSaveCard.setOnClickListener(v -> saveCard());
        btnCancel.setOnClickListener(v -> finish());
    }
    private void loadCardData() {
        int cardId = getIntent().getIntExtra("card_id", -1);

        if (cardId == -1) {
            // Nếu không có card_id, nghĩa là đang tạo mới
            isEditMode = false;
            return;
        }

        // Load card từ API thông qua ViewModel
        viewModel.loadCard(cardId);
    }

    private void populateCardData(Card card) {
        CardType cardType = card.getCardType();
        JsonObject content = card.getContent();

        if (content == null) return;

        switch (cardType) {
            case BASIC:
                rbBasic.setChecked(true);
                if (content.has("front")) {
                    etBasicQuestion.setText(content.get("front").getAsString());
                }
                if (content.has("back")) {
                    etBasicAnswer.setText(content.get("back").getAsString());
                }
                break;

            case MULTIPLE_CHOICE:
                rbMultipleChoice.setChecked(true);
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

                    // Tìm đáp án đúng
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
                rbFillInBlank.setChecked(true);
                if (content.has("text")) {
                    etFibQuestion.setText(content.get("text").getAsString());
                }
                if (content.has("answer")) {
                    etFibAnswer.setText(content.get("answer").getAsString());
                }
                break;
        }

        showLayoutForCardType();
    }


    private void setControlsEnabled(boolean enabled) {
        rbBasic.setEnabled(enabled && !isEditMode);
        rbMultipleChoice.setEnabled(enabled && !isEditMode);
        rbFillInBlank.setEnabled(enabled && !isEditMode);

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


    private void showLayoutForCardType() {
        // Ẩn tất cả layouts
        layoutBasic.setVisibility(View.GONE);
        layoutMultipleChoice.setVisibility(View.GONE);
        layoutFillInBlank.setVisibility(View.GONE);

        // Hiển thị layout tương ứng
        int checkedId = radioGroupCardType.getCheckedRadioButtonId();
        if (checkedId == R.id.rbBasic) {
            layoutBasic.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.rbMultipleChoice) {
            layoutMultipleChoice.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.rbFillInBlank) {
            layoutFillInBlank.setVisibility(View.VISIBLE);
        }
    }

    private void saveCard() {
        if (!validateInput()) {
            return;
        }

        if (currentCard == null) {
            Toast.makeText(this, "Không có thẻ để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = radioGroupCardType.getCheckedRadioButtonId();

        if (checkedId == R.id.rbBasic) {
            saveBasicCard();
        } else if (checkedId == R.id.rbMultipleChoice) {
            saveMultipleChoiceCard();
        } else if (checkedId == R.id.rbFillInBlank) {
            saveFillInBlankCard();
        }

        // Chỉ gọi update
        viewModel.updateCard(currentCard);
    }

    private boolean validateInput() {
        int checkedId = radioGroupCardType.getCheckedRadioButtonId();

        if (checkedId == R.id.rbBasic) {
            if (etBasicQuestion.getText().toString().trim().isEmpty()) {
                etBasicQuestion.setError("Vui lòng nhập câu hỏi");
                return false;
            }
            if (etBasicAnswer.getText().toString().trim().isEmpty()) {
                etBasicAnswer.setError("Vui lòng nhập câu trả lời");
                return false;
            }
        } else if (checkedId == R.id.rbMultipleChoice) {
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
        } else if (checkedId == R.id.rbFillInBlank) {
            if (etFibQuestion.getText().toString().trim().isEmpty()) {
                etFibQuestion.setError("Vui lòng nhập câu hỏi");
                return false;
            }
            if (etFibAnswer.getText().toString().trim().isEmpty()) {
                etFibAnswer.setError("Vui lòng nhập đáp án");
                return false;
            }
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

        // Tạo content với field "front" thay vì "text"
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