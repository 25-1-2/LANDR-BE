package com.landr.service.dto;

import com.landr.domain.lecture.Platform;
import com.landr.domain.plan.PlanType;
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
public class PlanDetailResponse {
    private Long planId;
    private String lectureTitle;
    private String teacher;
    private Platform platform;
    private PlanType planType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer dailyTime;
    private Float playbackSpeed;
    private Long lectureId;
    private List<DailyScheduleDto> dailySchedules;
}


