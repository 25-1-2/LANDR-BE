package com.landr.controller.lecture;

import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "강의 검색 요청 파라미터")
public class LectureSearchRequest {

    @Schema(description = "검색어 (강의 제목 또는 선생님 이름)", example = "수학")
    private String search;

    @Schema(description = "커서: 마지막으로 조회한 강의 ID", example = "12345")
    private Long cursorLectureId;

    @Schema(description = "커서: 마지막으로 조회한 강의 생성일시", example = "2024-04-27T15:30:00")
    private LocalDateTime cursorCreatedAt;

    @Schema(description = "한 페이지당 조회할 강의 수", example = "10", defaultValue = "10")
    private Integer offset = 10;

    @Schema(description = "플랫폼 필터링", example = "MEGA")
    private Platform platform;

    @Schema(description = "과목 필터링", example = "KOR")
    private Subject subject;
}