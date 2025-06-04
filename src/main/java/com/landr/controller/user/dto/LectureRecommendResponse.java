package com.landr.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LectureRecommendResponse {
    private Long id;
    private String platform;
    private String title;
    private String teacher;
    private String url;
    private String description;
    private String tag;
    private Integer totalLessons;
    private Double recommendScore;
    private String recommendReason;
    private String difficulty;
    private Boolean isPersonalized;
    private String subject;
}
