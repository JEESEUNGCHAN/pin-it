package com.example.pinit.model;

import java.io.Serializable;
import java.util.List;

// [모델] 날짜별 일정 데이터 - MyPlan 안에서 DAY 1, DAY 2 등 하루 단위 일정을 표현
// Serializable: Intent/Bundle로 화면 간 객체 전달 가능
public class DailySchedule implements Serializable {
    private String dayTitle; // 예: "DAY 1"
    private String date; // 예: "5월 1일"
    private List<String> places; // 예: ["상하이 공항", "호텔", "디즈니랜드"]

    public DailySchedule(String dayTitle, String date, List<String> places) {
        this.dayTitle = dayTitle;
        this.date = date;
        this.places = places;
    }

    public String getDayTitle() { return dayTitle; }
    public String getDate() { return date; }
    public List<String> getPlaces() { return places; }
}