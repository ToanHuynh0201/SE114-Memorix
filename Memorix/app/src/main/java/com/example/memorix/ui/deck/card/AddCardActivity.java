package com.example.memorix.ui.deck.card;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.memorix.R;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.memorix.data.Card;
import com.example.memorix.data.CardType;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddCardActivity extends AppCompatActivity {
    private Spinner spinnerCardType;
    private EditText etQuestion;
    private TextView tvQuestionLabel;

    // Basic card views
    private MaterialCardView cardBasicContent;
    private EditText etAnswer;

    // Multiple choice views
    private MaterialCardView cardMultipleChoiceContent;
    private EditText etOptionA, etOptionB, etOptionC, etOptionD;
    private RadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private RadioGroup radioGroup;

    // Fill in blank views
    private MaterialCardView cardFillBlankContent;
    private EditText etCorrectAnswer;

    // Action buttons
    private AppCompatButton btnSave, btnCancel;

    // Data
    private String deckId;
    private CardType currentCardType = CardType.BASIC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_card);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar();

        // Get deck ID from intent
//        deckId = getIntent().getStringExtra("deck_id");
//        if (deckId == null) {
//            Toast.makeText(this, "Lỗi: Không tìm thấy deck", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        initViews();
        setupSpinner();
        setupListeners();
        showContentForCardType(CardType.BASIC);
    }

    private void setupToolbar() {
        // If you have a toolbar in your layout, uncomment and modify this:
        /*
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm Flashcard");
        }
        */
    }

    private void initViews() {
        spinnerCardType = findViewById(R.id.spinner_card_type);
        etQuestion = findViewById(R.id.et_question);
        tvQuestionLabel = findViewById(R.id.tv_question_label);

        // Basic card views
        cardBasicContent = findViewById(R.id.card_basic_content);
        etAnswer = findViewById(R.id.et_answer);

        // Multiple choice views
        cardMultipleChoiceContent = findViewById(R.id.card_multiple_choice_content);
        etOptionA = findViewById(R.id.et_option_a);
        etOptionB = findViewById(R.id.et_option_b);
        etOptionC = findViewById(R.id.et_option_c);
        etOptionD = findViewById(R.id.et_option_d);
        rbOptionA = findViewById(R.id.rb_option_a);
        rbOptionB = findViewById(R.id.rb_option_b);
        rbOptionC = findViewById(R.id.rb_option_c);
        rbOptionD = findViewById(R.id.rb_option_d);

        // Setup RadioGroup for multiple choice
        setupRadioGroup();

        // Fill in blank views
        cardFillBlankContent = findViewById(R.id.card_fill_blank_content);
        etCorrectAnswer = findViewById(R.id.et_correct_answer);

        // Action buttons
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupRadioGroup() {
        // Find the parent layout for radio buttons
        View parent = findViewById(R.id.layout_options);
        radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        // Set up radio button click listeners instead of using RadioGroup
        // This approach works better with our layout structure
        View.OnClickListener radioClickListener = v -> {
            // Uncheck all radio buttons first
            rbOptionA.setChecked(false);
            rbOptionB.setChecked(false);
            rbOptionC.setChecked(false);
            rbOptionD.setChecked(false);

            // Check the clicked one
            ((RadioButton) v).setChecked(true);
        };

        rbOptionA.setOnClickListener(radioClickListener);
        rbOptionB.setOnClickListener(radioClickListener);
        rbOptionC.setOnClickListener(radioClickListener);
        rbOptionD.setOnClickListener(radioClickListener);
    }

    private void setupSpinner() {
        CardType[] cardTypes = CardType.values();
        String[] displayNames = new String[cardTypes.length];

        for (int i = 0; i < cardTypes.length; i++) {
            displayNames[i] = cardTypes[i].getDisplayName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                displayNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCardType.setAdapter(adapter);

        // Set default selection
        spinnerCardType.setSelection(0);
    }

    private void setupListeners() {
        spinnerCardType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CardType[] cardTypes = CardType.values();
                if (position < cardTypes.length) {
                    currentCardType = cardTypes[position];
                    showContentForCardType(currentCardType);
                    updateQuestionLabel(currentCardType);
                    clearInputs();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        btnSave.setOnClickListener(v -> saveCard());
        btnCancel.setOnClickListener(v -> {
            // Show confirmation dialog if there's unsaved content
            if (hasUnsavedContent()) {
                showExitConfirmationDialog();
            } else {
                finish();
            }
        });
    }

    private void clearInputs() {
        etQuestion.setText("");
        etAnswer.setText("");
        etOptionA.setText("");
        etOptionB.setText("");
        etOptionC.setText("");
        etOptionD.setText("");
        etCorrectAnswer.setText("");

        // Uncheck all radio buttons
        rbOptionA.setChecked(false);
        rbOptionB.setChecked(false);
        rbOptionC.setChecked(false);
        rbOptionD.setChecked(false);
    }

    private boolean hasUnsavedContent() {
        return !TextUtils.isEmpty(etQuestion.getText().toString().trim()) ||
                !TextUtils.isEmpty(etAnswer.getText().toString().trim()) ||
                !TextUtils.isEmpty(etOptionA.getText().toString().trim()) ||
                !TextUtils.isEmpty(etOptionB.getText().toString().trim()) ||
                !TextUtils.isEmpty(etOptionC.getText().toString().trim()) ||
                !TextUtils.isEmpty(etOptionD.getText().toString().trim()) ||
                !TextUtils.isEmpty(etCorrectAnswer.getText().toString().trim());
    }

    private void showExitConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn thoát? Dữ liệu chưa lưu sẽ bị mất.")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showContentForCardType(CardType cardType) {
        // Hide all content views first
        cardBasicContent.setVisibility(View.GONE);
        cardMultipleChoiceContent.setVisibility(View.GONE);
        cardFillBlankContent.setVisibility(View.GONE);

        // Show appropriate content view with animation
        switch (cardType) {
            case BASIC:
                cardBasicContent.setVisibility(View.VISIBLE);
                break;
            case MULTIPLE_CHOICE:
                cardMultipleChoiceContent.setVisibility(View.VISIBLE);
                break;
            case FILL_IN_BLANK:
                cardFillBlankContent.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateQuestionLabel(CardType cardType) {
        switch (cardType) {
            case BASIC:
                tvQuestionLabel.setText("Câu hỏi");
                etQuestion.setHint("Nhập câu hỏi...");
                break;
            case MULTIPLE_CHOICE:
                tvQuestionLabel.setText("Câu hỏi trắc nghiệm");
                etQuestion.setHint("Nhập câu hỏi trắc nghiệm...");
                break;
            case FILL_IN_BLANK:
                tvQuestionLabel.setText("Câu hỏi điền từ");
                etQuestion.setHint("Nhập câu với dấu _ để đánh dấu chỗ trống...");
                break;
        }
    }

    private void saveCard() {
        String question = etQuestion.getText().toString().trim();

        if (TextUtils.isEmpty(question)) {
            Toast.makeText(this, "Vui lòng nhập câu hỏi", Toast.LENGTH_SHORT).show();
            etQuestion.requestFocus();
            return;
        }

        Card card = null;
        String cardId = UUID.randomUUID().toString();

        try {
            switch (currentCardType) {
                case BASIC:
                    card = createBasicCard(cardId, question);
                    break;
                case MULTIPLE_CHOICE:
                    card = createMultipleChoiceCard(cardId, question);
                    break;
                case FILL_IN_BLANK:
                    card = createFillInBlankCard(cardId, question);
                    break;
            }

            if (card != null) {
                // Save card to database
                saveCardToDatabase(card);

                Toast.makeText(this, "Đã lưu flashcard thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Card createBasicCard(String cardId, String question) {
        String answer = etAnswer.getText().toString().trim();

        if (TextUtils.isEmpty(answer)) {
            etAnswer.requestFocus();
            throw new IllegalArgumentException("Vui lòng nhập câu trả lời");
        }

        return new Card(cardId, deckId, question, answer);
    }

    private Card createMultipleChoiceCard(String cardId, String question) {
        String optionA = etOptionA.getText().toString().trim();
        String optionB = etOptionB.getText().toString().trim();
        String optionC = etOptionC.getText().toString().trim();
        String optionD = etOptionD.getText().toString().trim();

        if (TextUtils.isEmpty(optionA) || TextUtils.isEmpty(optionB)) {
            throw new IllegalArgumentException("Vui lòng nhập ít nhất 2 lựa chọn (A và B)");
        }

        List<String> options = new ArrayList<>();
        options.add(optionA);
        options.add(optionB);
        if (!TextUtils.isEmpty(optionC)) {
            options.add(optionC);
        }
        if (!TextUtils.isEmpty(optionD)) {
            options.add(optionD);
        }

        // Get selected correct answer
        String correctAnswer = getSelectedCorrectAnswer();
        if (TextUtils.isEmpty(correctAnswer)) {
            throw new IllegalArgumentException("Vui lòng chọn đáp án đúng");
        }

        return new Card(cardId, deckId, question, options, correctAnswer);
    }

    private Card createFillInBlankCard(String cardId, String question) {
        String correctAnswer = etCorrectAnswer.getText().toString().trim();

        if (TextUtils.isEmpty(correctAnswer)) {
            etCorrectAnswer.requestFocus();
            throw new IllegalArgumentException("Vui lòng nhập đáp án đúng");
        }

        if (!question.contains("_")) {
            etQuestion.requestFocus();
            throw new IllegalArgumentException("Câu hỏi điền từ phải có ít nhất một dấu gạch dưới (_)");
        }

        return new Card(cardId, deckId, question, correctAnswer, CardType.FILL_IN_BLANK);
    }

    private String getSelectedCorrectAnswer() {
        if (rbOptionA.isChecked()) {
            return etOptionA.getText().toString().trim();
        } else if (rbOptionB.isChecked()) {
            return etOptionB.getText().toString().trim();
        } else if (rbOptionC.isChecked()) {
            return etOptionC.getText().toString().trim();
        } else if (rbOptionD.isChecked()) {
            return etOptionD.getText().toString().trim();
        }

        return null;
    }

    private void saveCardToDatabase(Card card) {
        System.out.println("Saving card: " + card.toString());
        System.out.println("Card type: " + card.getType().getDisplayName());
        System.out.println("Card content: " + card.getDisplayContent());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (hasUnsavedContent()) {
            showExitConfirmationDialog();
        } else {
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedContent()) {
            showExitConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }
}