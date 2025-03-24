package com.landr.service.dto;

import com.landr.domain.schedule.LessonSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonScheduleDto {
    private Long id;
    private String lessonTitle;     // 레슨 이름
    private String lectureName;     // 강의 이름
    private int adjustedDuration;   // 배속 적용된 시간
    private int displayOrder;
    private boolean completed;      // 체크 여부

    public static LessonScheduleDto convert(LessonSchedule lessonSchedule) {
        return LessonScheduleDto.builder()
            .id(lessonSchedule.getId())
            .lessonTitle(lessonSchedule.getLesson().getTitle())
            .lectureName(lessonSchedule.getLesson().getLecture().getTitle())
            .adjustedDuration(lessonSchedule.getAdjustedDuration())
            .displayOrder(lessonSchedule.getDisplayOrder())
            .completed(lessonSchedule.isCompleted())
            .build();
    }
}
