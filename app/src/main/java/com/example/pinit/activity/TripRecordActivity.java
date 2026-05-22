package com.example.pinit.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pinit.R;
import com.example.pinit.database.DatabaseHelper;
import com.example.pinit.model.Record;

import java.util.Calendar;
import java.util.Locale;

// [화면] 여행 기록(일기) 추가·수정 폼
// Intent로 trip_id(필수), record_id(수정 시) 수신
// 제목, 날짜, 본문, 장소명 입력 가능 (이미지 첨부는 현재 미구현)
public class TripRecordActivity extends AppCompatActivity {

    private EditText etTitle, etDate, etContent, etPlaceName;
    private DatabaseHelper dbHelper;
    private int tripId;
    private int recordId = -1;       // -1이면 추가 모드
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_record);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);
        recordId = getIntent().getIntExtra("record_id", -1);
        isEditMode = recordId != -1;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "기록 수정" : "여행 기록");
        }

        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);
        etContent = findViewById(R.id.etContent);
        etPlaceName = findViewById(R.id.etPlaceName);

        // 날짜 필드 클릭 시 DatePickerDialog 표시
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    etDate.setText(String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, day)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (isEditMode) {
            loadExistingRecord(); // 수정 모드: 기존 기록 로드
        } else {
            // 추가 모드: 날짜 기본값=오늘
            Calendar cal = Calendar.getInstance();
            etDate.setText(String.format(Locale.KOREA, "%d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
        }

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setText(isEditMode ? "수정 완료" : "저장");
        btnSave.setOnClickListener(v -> saveRecord());
    }

    // 기존 Record 데이터를 DB에서 읽어 각 필드에 채움
    private void loadExistingRecord() {
        Record r = dbHelper.getRecordById(recordId);
        if (r == null) { finish(); return; }

        etTitle.setText(r.getTitle());
        etDate.setText(r.getDate());
        etContent.setText(r.getContent());
        etPlaceName.setText(r.getPlaceName());
    }

    // 입력값 검증 후 DB 저장/수정 → RESULT_OK 반환 (TripDetailActivity가 수신해 목록 갱신)
    private void saveRecord() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Record r = new Record();
        if (isEditMode) r.setId(recordId);
        r.setTripId(tripId);
        r.setTitle(title);
        r.setDate(etDate.getText().toString());
        r.setContent(etContent.getText().toString());
        r.setPlaceName(etPlaceName.getText().toString());

        if (isEditMode) {
            dbHelper.updateRecord(r);
            Toast.makeText(this, "기록이 수정되었습니다!", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.insertRecord(r);
            Toast.makeText(this, "기록이 저장되었습니다!", Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
