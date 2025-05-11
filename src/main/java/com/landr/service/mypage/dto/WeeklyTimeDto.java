package com.landr.service.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyTimeDto {
    private int weekNumber;
    private long totalMinutes;
    private String weekLabel; // "1주차", "2주차" 등
}