package com.landr.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAchievementDto {
    private boolean mondayAchieved;
    private boolean tuesdayAchieved;
    private boolean wednesdayAchieved;
    private boolean thursdayAchieved;
    private boolean fridayAchieved;
    private boolean saturdayAchieved;
    private boolean sundayAchieved;
}