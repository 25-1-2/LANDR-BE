package com.landr.service.dto;

import com.landr.domain.lecture.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedPlanDto {

    private Long planId;
    private String lectureTitle;
    private String teacher;
    private Platform platform;
}