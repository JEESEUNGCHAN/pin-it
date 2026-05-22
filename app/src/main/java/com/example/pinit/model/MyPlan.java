package com.example.pinit.model;

import java.io.Serializable;
import java.util.List;

// [모델] 마이플랜 데이터 - 여행 제목, 날짜, 여행지, DailySchedule 목록을 하나로 묶는 컨테이너
// Serializable: Intent/Bundle로 화면 간 객체 전달 가능
public class MyPlan implements Serializable {
    private String planId;
    private String title;
    private String date;
    private String country;

    private List<DailySchedule> schedules;

    public MyPlan(String planId, String title, String date, String country, List<DailySchedule> schedules) {
        this.planId = planId;
        this.title = title;
        this.date = date;
        this.country = country;
        this.schedules = schedules;
    }

    public String getPlanId() { return planId; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getCountry() { return country; }
    public List<DailySchedule> getSchedules() { return schedules; } // 일정 목록 꺼내기
}