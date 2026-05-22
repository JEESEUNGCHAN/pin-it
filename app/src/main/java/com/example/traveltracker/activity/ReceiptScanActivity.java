package com.example.traveltracker.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.traveltracker.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// [화면] 영수증 OCR 스캔 화면
// CameraX로 카메라 미리보기 및 촬영, 또는 갤러리에서 이미지 선택
// ML Kit 한국어 텍스트 인식으로 "합계/총액/결제금액" 키워드 주변의 금액을 추출
// 인식된 금액은 RESULT_OK + Intent("amount")로 BudgetFragment에 전달 → AddBudgetActivity에서 자동 입력
public class ReceiptScanActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY = 200;
    private static final int REQUEST_CAMERA_CAPTURE = 300;

    private PreviewView previewView;    // CameraX 실시간 미리보기
    private ImageView ivPreview;        // 촬영/선택된 이미지 표시
    private TextView tvResult;          // OCR 결과 텍스트 표시
    private ProgressBar progressBar;    // OCR 처리 중 로딩 표시
    private Button btnScan, btnGallery, btnConfirm, btnRetry;
    private ImageCapture imageCapture;  // CameraX 사진 촬영 유스케이스
    private double detectedAmount = 0;  // OCR로 인식된 최종 금액
    private int tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_scan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("영수증 스캔");
        }

        tripId = getIntent().getIntExtra("trip_id", -1);

        previewView = findViewById(R.id.previewView);
        ivPreview = findViewById(R.id.ivPreview);
        tvResult = findViewById(R.id.tvResult);
        progressBar = findViewById(R.id.progressBar);
        btnScan = findViewById(R.id.btnScan);
        btnGallery = findViewById(R.id.btnGallery);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnRetry = findViewById(R.id.btnRetry);

        btnGallery.setOnClickListener(v -> openGallery());
        btnScan.setOnClickListener(v -> {
            // 카메라 권한 확인 후 촬영
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        // 인식된 금액 확정 → BudgetFragment로 결과 반환
        btnConfirm.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("amount", detectedAmount);
            setResult(Activity.RESULT_OK, result);
            finish();
        });

        btnRetry.setOnClickListener(v -> resetUI()); // 다시 찍기 → 초기 상태로 복귀

        // 권한이 있으면 앱 진입 시 바로 카메라 미리보기 시작
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    // CameraX 바인딩: Preview(미리보기) + ImageCapture(촬영) 유스케이스 등록
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // 빠른 촬영 우선
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this,
                        CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "카메라 초기화 실패", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // 사진 촬영 후 캐시에 파일 저장 → InputImage 생성 → OCR 실행
    private void takePicture() {
        if (imageCapture == null) return;
        File photoFile = new File(getCacheDir(), "receipt_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        try {
                            InputImage image = InputImage.fromFilePath(
                                    ReceiptScanActivity.this, Uri.fromFile(photoFile));
                            ivPreview.setImageURI(Uri.fromFile(photoFile));
                            previewView.setVisibility(View.GONE); // 미리보기 숨기고
                            ivPreview.setVisibility(View.VISIBLE); // 촬영 이미지 표시
                            runOcr(image);
                        } catch (IOException e) {
                            Toast.makeText(ReceiptScanActivity.this,
                                    "이미지 처리 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Toast.makeText(ReceiptScanActivity.this,
                                "촬영 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 갤러리에서 이미지 선택
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 갤러리 선택 완료: Bitmap으로 변환 후 OCR 실행
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivPreview.setImageBitmap(bitmap);
                previewView.setVisibility(View.GONE);
                ivPreview.setVisibility(View.VISIBLE);
                InputImage image = InputImage.fromBitmap(bitmap, 0);
                runOcr(image);
            } catch (IOException e) {
                Toast.makeText(this, "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ML Kit 한국어 OCR 실행 → 금액 추출 → 결과 표시
    private void runOcr(InputImage image) {
        progressBar.setVisibility(View.VISIBLE);
        tvResult.setText("인식 중...");
        btnScan.setEnabled(false);
        btnGallery.setEnabled(false);

        TextRecognizer recognizer = TextRecognition.getClient(
                new KoreanTextRecognizerOptions.Builder().build());
        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    progressBar.setVisibility(View.GONE);
                    String fullText = text.getText();
                    detectedAmount = extractAmount(fullText);
                    if (detectedAmount > 0) {
                        tvResult.setText("✅ 인식된 금액: " + (int) detectedAmount + "원\n\n" +
                                "--- 전체 인식 텍스트 ---\n" + fullText);
                        btnConfirm.setVisibility(View.VISIBLE); // 금액 확정 버튼 표시
                    } else {
                        tvResult.setText("금액을 찾지 못했습니다.\n\n--- 인식된 텍스트 ---\n" + fullText);
                    }
                    btnRetry.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvResult.setText("인식 실패: " + e.getMessage());
                    btnRetry.setVisibility(View.VISIBLE);
                    btnScan.setEnabled(true);
                    btnGallery.setEnabled(true);
                });
    }

    // 금액 추출: 합계/총액/결제금액 키워드 주변 숫자 우선, 없으면 가장 큰 숫자 반환
    private double extractAmount(String text) {
        String[] keywords = {"합계", "총액", "결제금액", "총합계", "받을금액", "청구금액", "total", "TOTAL"};
        String[] lines = text.split("\n");

        for (String keyword : keywords) {
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(keyword)) {
                    // 해당 줄과 다음 줄에서 숫자 추출
                    String target = lines[i];
                    if (i + 1 < lines.length) target += " " + lines[i + 1];
                    double amount = extractNumber(target);
                    if (amount > 0) return amount;
                }
            }
        }

        // 키워드 없으면 전체 텍스트에서 가장 큰 숫자 (1000만원 미만)
        double maxAmount = 0;
        Pattern pattern = Pattern.compile("[0-9,]+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String numStr = matcher.group().replace(",", "");
            try {
                double num = Double.parseDouble(numStr);
                if (num > maxAmount && num < 10000000) maxAmount = num;
            } catch (NumberFormatException ignored) {}
        }
        return maxAmount;
    }

    // 주어진 텍스트에서 가장 큰 숫자 추출
    private double extractNumber(String text) {
        Pattern pattern = Pattern.compile("[0-9,]+");
        Matcher matcher = pattern.matcher(text);
        double max = 0;
        while (matcher.find()) {
            String numStr = matcher.group().replace(",", "");
            try {
                double num = Double.parseDouble(numStr);
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return max;
    }

    // 화면을 초기 카메라 미리보기 상태로 되돌림
    private void resetUI() {
        previewView.setVisibility(View.VISIBLE);
        ivPreview.setVisibility(View.GONE);
        tvResult.setText("영수증을 카메라에 비추면 자동으로 금액을 인식합니다");
        btnConfirm.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnScan.setEnabled(true);
        btnGallery.setEnabled(true);
        detectedAmount = 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
