package com.example.pinit.model;

// [모델] 여행 하나를 나타내는 데이터 클래스
// SQLite trips 테이블의 한 행과 1:1 대응
// HomeFragment에서 목록으로 보이고, TripDetailActivity에서 상세보기에 사용
public class Trip {
    private int id;           // DB 자동증가 고유번호
    private String title;     // 여행 이름 (예: "도쿄 여행")
    private String destination; // 여행지 (예: "일본 도쿄")
    private String startDate; // 시작일 "yyyy-MM-dd"
    private String endDate;   // 종료일 "yyyy-MM-dd"
    private double budget;    // 총 예산 (원)
    private String memo;      // 메모
    private String coverImage; // 커버 이미지 파일 경로 (미구현, 필드만 있음)

    public Trip() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
}
