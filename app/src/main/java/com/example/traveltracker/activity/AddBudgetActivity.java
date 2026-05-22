package com.example.traveltracker.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.traveltracker.R;
import com.example.traveltracker.database.DatabaseHelper;
import com.example.traveltracker.model.Budget;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

// [화면] 예산/지출 항목 추가·수정 폼
// Intent로 trip_id, budget_id(수정 시), auto_amount(영수증 OCR 자동입력 금액) 수신
// 카테고리 Spinner(식비/교통/숙박/쇼핑/관광/기타) + 수입/지출 라디오 버튼 포함
public class AddBudgetActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etDate, etMemo;
    private RadioGroup rgType;           // 수입/지출 선택 라디오 그룹
    private Spinner spinnerCategory;     // 카테고리 선택 스피너
    private DatabaseHelper dbHelper;
    private int tripId;
    private int budgetId = -1;           // -1이면 추가 모드
    private boolean isEditMode = false;

    private static final String[] CATEGORIES = {"식비", "교통", "숙박", "쇼핑", "관광", "기타"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);
        budgetId = getIntent().getIntExtra("budget_id", -1);
        isEditMode = budgetId != -1;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "지출 수정" : "지출 추가");
        }

        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etMemo = findViewById(R.id.etMemo);
        rgType = findViewById(R.id.rgType);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // 카테고리 Spinner 초기화
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    etDate.setText(String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, day)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (isEditMode) {
            loadExistingBudget(); // 수정 모드: DB에서 기존 데이터 로드
        } else {
            // 추가 모드: 날짜 기본값=오늘, 영수증 OCR 금액이 있으면 자동 입력
            Calendar cal = Calendar.getInstance();
            etDate.setText(String.format(Locale.KOREA, "%d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));

            double autoAmount = getIntent().getDoubleExtra("auto_amount", 0);
            if (autoAmount > 0) {
                etAmount.setText(String.valueOf((int) autoAmount));
                etTitle.requestFocus();
                Toast.makeText(this, "영수증에서 인식된 금액이 자동 입력되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setText(isEditMode ? "수정 완료" : "저장");
        btnSave.setOnClickListener(v -> saveBudget());
    }

    // 기존 Budget 데이터를 DB에서 읽어 각 필드에 채움
    private void loadExistingBudget() {
        Budget b = dbHelper.getBudgetById(budgetId);
        if (b == null) { finish(); return; }

        etTitle.setText(b.getTitle());
        etAmount.setText(String.valueOf((long) b.getAmount()));
        etDate.setText(b.getDate());
        etMemo.setText(b.getMemo());

        // type에 따라 수입/지출 라디오 버튼 선택
        if ("income".equals(b.getType())) {
            ((RadioButton) findViewById(R.id.rbIncome)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rbExpense)).setChecked(true);
        }

        int idx = Arrays.asList(CATEGORIES).indexOf(b.getCategory());
        if (idx >= 0) spinnerCategory.setSelection(idx);
    }

    // 입력값 검증 후 DB 저장/수정 → RESULT_OK 반환 (BudgetFragment에서 목록 갱신)
    private void saveBudget() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "제목과 금액을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Budget b = new Budget();
        if (isEditMode) b.setId(budgetId);
        b.setTripId(tripId);
        b.setTitle(title);
        b.setAmount(Double.parseDouble(amountStr));
        b.setCategory(spinnerCategory.getSelectedItem().toString());
        b.setDate(etDate.getText().toString());
        b.setType(rgType.getCheckedRadioButtonId() == R.id.rbIncome ? "income" : "expense");
        b.setMemo(etMemo.getText().toString());

        if (isEditMode) {
            dbHelper.updateBudget(b);
            Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.insertBudget(b);
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
