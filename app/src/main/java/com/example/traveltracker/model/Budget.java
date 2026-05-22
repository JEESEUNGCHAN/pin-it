package com.example.traveltracker.model;

// [모델] 예산/지출 항목 하나를 나타내는 데이터 클래스
// SQLite budgets 테이블의 한 행과 1:1 대응
// BudgetFragment에서 목록으로 표시, AddBudgetActivity에서 추가/수정
public class Budget {
    private int id;           // DB 자동증가 고유번호
    private int tripId;       // 어느 여행의 예산인지 (trips.id 참조)
    private String title;     // 항목명 (예: "라멘 저녁 식사")
    private double amount;    // 금액 (원)
    private String category;  // 카테고리: "식비", "교통", "숙박", "쇼핑", "관광", "기타"
    private String date;      // 날짜 "yyyy-MM-dd"
    private String type;      // "income"(수입) 또는 "expense"(지출)
    private String memo;      // 메모

    public Budget() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}
