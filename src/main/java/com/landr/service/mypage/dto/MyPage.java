package com.landr.service.mypage.dto;

import com.landr.service.dto.CompletedPlanDto;
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
public class MyPage {
    private String userName;
    private Integer todayTotalLessonCount;
    private Integer todayCompletedLessonCount;
    private Integer completedLectureCount;
    private Integer studyStreak;
    private Integer inProgressLectureCount;
    private List<CompletedPlanDto> completedPlanList;
    private List<SubjectAchievementDto> subjectAchievementList;
}
