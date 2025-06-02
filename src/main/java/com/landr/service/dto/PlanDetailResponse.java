package com.landr.service.dto;

import com.landr.domain.lecture.Platform;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailResponse {
    private Long planId;
    private String lectureTitle;
    private String teacher;
    private Platform platform;
    private List<DailyScheduleDto> dailySchedules;
}


