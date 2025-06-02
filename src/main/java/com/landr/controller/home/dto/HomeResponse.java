package com.landr.controller.home.dto;

import com.landr.controller.dday.dto.DDayDto;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.UserProgressDto;
import com.landr.service.dto.WeeklyAchievementDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeResponse {
    private UserProgressDto userProgress;
    private DailyScheduleWithLessonsDto todaySchedule;
    private WeeklyAchievementDto weeklyAchievement;
    private DDayDto dDay;
}
