package com.landr.service.dto;

import com.landr.domain.plan.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyScheduleWithLessonsDto {
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private int totalLessons;
    private int totalDuration;
    private List<LessonScheduleDto> lessonSchedules;

}
