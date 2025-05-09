package com.landr.service.mypage.dto;

import com.landr.service.dto.PlanSummaryDto;
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
    private Integer completedLectureCount;
    private Integer studyStreak;
    private LocalDate goalDate;
    private List<PlanSummaryDto> completedPlanList;
    private List<SubjectAchievementDto> subjectAchievementList;
}
