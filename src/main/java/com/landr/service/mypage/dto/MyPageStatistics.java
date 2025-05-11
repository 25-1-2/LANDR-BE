package com.landr.service.mypage.dto;

import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyPageStatistics {

    private YearMonth date;
    private List<SubjectTimeDto> subjectTimes;
    private List<WeeklyTimeDto> weeklyTimes;

}
