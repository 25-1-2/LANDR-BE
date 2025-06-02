package com.landr.service.mypage.dto;

import com.landr.domain.lecture.Subject;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectAchievementDto {
    private Subject subject;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalLessons;
    private int completedLessons;
}
