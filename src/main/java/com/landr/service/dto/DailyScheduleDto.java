package com.landr.service.dto;

import com.landr.domain.plan.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyScheduleDto {
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private List<LessonScheduleDto> lessonSchedules;
}
