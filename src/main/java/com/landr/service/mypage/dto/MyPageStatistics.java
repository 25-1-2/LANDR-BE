package com.landr.service.mypage.dto;

import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyPageStatistics {

    private YearMonth date;
    private Long totalStudyMinutes;
    private List<SubjectTimeDto> subjectTimes;
    private List<WeeklyTimeDto> weeklyTimes;

}
