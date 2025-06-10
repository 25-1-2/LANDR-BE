package com.landr.service.dto;

import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanSummaryDto {

    private Long planId;
    private String lectureTitle;
    private String teacher;
    private Platform platform;
    private int totalLessons;
    private long completedLessons;
    private boolean isStudyGroup;
    private Long studyGroupId;
    private Subject subject;
    private String tag;
}