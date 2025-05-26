package com.example.memorix.ui.deck.card;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import com.example.memorix.data.Card;
import com.example.memorix.data.CardType;

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
        setupListeners();
        loadCardData();
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
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chỉnh sửa thẻ");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void setupListeners() {
        radioGroupCardType.setOnCheckedChangeListener((group, checkedId) -> {
            showLayoutForCardType();
        });

        btnSaveCard.setOnClickListener(v -> saveCard());
        btnCancel.setOnClickListener(v -> finish());
    }
    private void loadCardData() {
        // Lấy card từ Intent
        currentCard = (Card) getIntent().getSerializableExtra("card");
        if (currentCard != null) {
            isEditMode = true;
            populateCardData();
        } else {
            // Mặc định tạo thẻ mới loại Basic
            rbBasic.setChecked(true);
            showLayoutForCardType();
        }
    }

    private void populateCardData() {
        CardType type = currentCard.getType();

        switch (type) {
            case BASIC:
                rbBasic.setChecked(true);
                etBasicQuestion.setText(currentCard.getQuestion());
                etBasicAnswer.setText(currentCard.getAnswer());
                break;

            case MULTIPLE_CHOICE:
                rbMultipleChoice.setChecked(true);
                etMcQuestion.setText(currentCard.getQuestion());
                List<String> options = currentCard.getOptions();
                if (options.size() >= 4) {
                    etMcOption1.setText(options.get(0));
                    etMcOption2.setText(options.get(1));
                    etMcOption3.setText(options.get(2));
                    etMcOption4.setText(options.get(3));
                }

                // Tìm đáp án đúng
                String correctAnswer = currentCard.getCorrectAnswer();
                for (int i = 0; i < options.size(); i++) {
                    if (options.get(i).equals(correctAnswer)) {
                        switch (i) {
                            case 0: rbCorrect1.setChecked(true); break;
                            case 1: rbCorrect2.setChecked(true); break;
                            case 2: rbCorrect3.setChecked(true); break;
                            case 3: rbCorrect4.setChecked(true); break;
                        }
                        break;
                    }
                }
                break;

            case FILL_IN_BLANK:
                rbFillInBlank.setChecked(true);
                etFibQuestion.setText(currentCard.getQuestion());
                etFibAnswer.setText(currentCard.getCorrectAnswer());
                break;
        }

        showLayoutForCardType();
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

    private void updateCardInfo() {
//        if (currentCard != null) {
//            String info = "Loại: " + currentCard.getType().getDisplayName() +
//                    " | Ôn tập: " + currentCard.getReviewCount() + " lần" +
//                    " | Tỷ lệ đúng: " + String.format("%.1f", currentCard.getAccuracyRate()) + "%";
//            tvCardInfo.setText(info);
//            tvCardInfo.setVisibility(View.VISIBLE);
//        } else {
//            tvCardInfo.setVisibility(View.GONE);
//        }
    }

    private void saveCard() {
        if (!validateInput()) {
            return;
        }

        int checkedId = radioGroupCardType.getCheckedRadioButtonId();

        if (currentCard == null) {
            currentCard = new Card();
            currentCard.setId(System.currentTimeMillis() + "");
            currentCard.setDeckId("deck1"); // Demo deck ID
        }

        if (checkedId == R.id.rbBasic) {
            saveBasicCard();
        } else if (checkedId == R.id.rbMultipleChoice) {
            saveMultipleChoiceCard();
        } else if (checkedId == R.id.rbFillInBlank) {
            saveFillInBlankCard();
        }

        Toast.makeText(this, isEditMode ? "Đã cập nhật thẻ!" : "Đã tạo thẻ mới!", Toast.LENGTH_SHORT).show();
        finish();
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
        currentCard.setType(CardType.BASIC);
        currentCard.setQuestion(etBasicQuestion.getText().toString().trim());
        currentCard.setAnswer(etBasicAnswer.getText().toString().trim());
    }

    private void saveMultipleChoiceCard() {
        currentCard.setType(CardType.MULTIPLE_CHOICE);
        currentCard.setQuestion(etMcQuestion.getText().toString().trim());

        List<String> options = new ArrayList<>();
        options.add(etMcOption1.getText().toString().trim());
        options.add(etMcOption2.getText().toString().trim());
        options.add(etMcOption3.getText().toString().trim());
        options.add(etMcOption4.getText().toString().trim());
        currentCard.setOptions(options);

        // Xác định đáp án đúng
        int correctIndex = 0;
        int checkedId = radioGroupCorrectAnswer.getCheckedRadioButtonId();
        if (checkedId == R.id.rbCorrect2) correctIndex = 1;
        else if (checkedId == R.id.rbCorrect3) correctIndex = 2;
        else if (checkedId == R.id.rbCorrect4) correctIndex = 3;

        currentCard.setCorrectAnswer(options.get(correctIndex));
    }

    private void saveFillInBlankCard() {
        currentCard.setType(CardType.FILL_IN_BLANK);
        currentCard.setQuestion(etFibQuestion.getText().toString().trim());
        currentCard.setCorrectAnswer(etFibAnswer.getText().toString().trim());
    }
}