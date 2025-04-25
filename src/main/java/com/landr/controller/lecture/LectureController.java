package com.landr.controller.lecture;

import com.landr.service.dto.lecture.CursorPageResponseDto;
import com.landr.service.dto.lecture.LectureResponseDto;
import com.landr.service.lecture.LectureService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture API", description = "강의 조회 API")
public class LectureController {

    private final LectureService lectureService;

    /**
     * 전체 강의 목록 최신순 조회
     */
    @GetMapping("/all")
    @Operation(summary = "전체 강의 목록 조회", description = "생성일 기준 최신순으로 강의 목록을 커서 기반으로 10개 조회합니다.")
    public CursorPageResponseDto<LectureResponseDto> getAllLectures(
            @ParameterObject LectureSearchRequest req
    ) {
        return lectureService.getLatestLectures(req);
    }

    /**
     * 검색된 강의 목록 최신순 조회
     */
    @GetMapping("")
    @Operation(summary = "강의 검색 (title, teacher)", description = "강의명 또는 선생님 검색어 기반으로 최신순 정렬된 강의 목록을 커서 기반으로 조회합니다.")
    public CursorPageResponseDto<LectureResponseDto> searchLectures(
            @ParameterObject LectureSearchRequest req
    ) {
        return lectureService.searchLatestLectures(req);
    }
}
