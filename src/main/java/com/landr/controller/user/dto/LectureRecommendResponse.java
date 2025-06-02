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

    // 🆕 AI 추천 관련 필드 추가
    private double recommendScore;  // 추천 점수 (0-100)
    private String recommendReason; // 추천 이유
    private String difficulty;      // 예상 난이도
    private boolean isPersonalized; // 개인화 추천 여부
}
