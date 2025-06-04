package com.landr.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Schema(description = "강의 추천 요청 DTO")
public class LectureRecommendRequest {

    @Schema(description = "현재 학년 (예: 고1, 고2, 고3, N수생)", example = "고1")
    @NotBlank(message = "학년은 필수입니다")
    private String grade;

    @Schema(description = "내신 등급", example = "2")
    @Min(value = 1, message = "내신 등급은 1등급 이상이어야 합니다")
    @Max(value = 9, message = "내신 등급은 9등급 이하여야 합니다")
    private int schoolRank;

    @Schema(description = "모의고사 등급", example = "3")
    @Min(value = 1, message = "모의고사 등급은 1등급 이상이어야 합니다")
    @Max(value = 9, message = "모의고사 등급은 9등급 이하여야 합니다")
    private int mockRank;

    @Schema(description = "학습 방향 (수능 중심 / 내신 중심)", example = "수능 중심")
    @NotBlank(message = "학습 방향은 필수입니다")
    private String focus;

    @Schema(description = "학습 목표 (개념 정리 / 기출 분석 / 실전 문제풀이 / 빠른 요약 정리)", example = "기출 분석")
    @NotBlank(message = "학습 목표는 필수입니다")
    private String goal;

    @Schema(description = "선호 학습 스타일 (최대 2개)", example = "[\"문풀 위주\", \"차분한 설명\"]")
    @Size(max = 2, message = "학습 스타일은 최대 2개까지 선택 가능합니다")
    private List<String> styles;

    @Schema(description = "선택 과목 (단일 과목)", example = "수학Ⅰ")
    @NotBlank(message = "과목은 필수입니다")
    private String subject;
}