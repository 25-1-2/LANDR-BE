package com.landr.controller.dto;

import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.UserProgressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeResponseDto {
    private UserProgressDto userProgress;
    private DailyScheduleWithLessonsDto todaySchedule;
}
