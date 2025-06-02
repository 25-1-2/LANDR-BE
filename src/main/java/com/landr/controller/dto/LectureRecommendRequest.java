package com.landr.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "강의 추천 요청 DTO")
public class LectureRecommendRequest {

    @Schema(description = "현재 학년 (예: 고1, 고2, 고3, N수생)", example = "고1")
    private String grade;

    @Schema(description = "내신 등급", example = "2")
    private int schoolRank;

    @Schema(description = "모의고사 등급", example = "3")
    private int mockRank;

    @Schema(description = "학습 방향 (수능 중심 / 내신 중심)", example = "수능 중심")
    private String focus;

    @Schema(description = "학습 목표 (개념 정리 / 기출 분석 / 실전 문제풀이 / 빠른 요약 정리)", example = "기출 분석")
    private String goal;

    @Schema(description = "선호 학습 스타일", example = "[\"문풀 위주\", \"차분한 설명\"]")
    private List<String> styles;
}
