package com.landr.controller.lecture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LectureSearchRequest {

    @Schema(description = "검색 키워드 (강의명 또는 선생님)", example = "수분감")
    private String search;

    @Schema(description = "커서 기준 마지막 강의 ID", example = "145")
    private Long cursor;

    @Schema(description = "한 페이지에 조회할 강의 수", defaultValue = "10", example = "10")
    private Integer offset = 10;

    @Schema(description = "계획 수 기준 정렬 여부", defaultValue = "false", example = "true")
    private Boolean sortByPlan = false;
}


