package com.landr.controller.lecture;

import com.landr.controller.lecture.dto.LectureSearchRequest;
import com.landr.service.dto.LectureResponseDto;
import com.landr.service.dto.CursorPageResponseDto;
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

    @GetMapping("")
    @Operation(summary = "강의 목록 검색", description = "검색어 및 커서 기반 페이지네이션으로 강의를 조회합니다.")
    public CursorPageResponseDto<LectureResponseDto> searchLectures(
            @ParameterObject LectureSearchRequest request
    ) {
        return lectureService.searchLectures(request);
    }

    @GetMapping("/all")
    @Operation(summary = "전체 강의 목록 조회", description = "정렬 조건: 계획 수가 많은 순서로 강의를 조회합니다.")
    public CursorPageResponseDto<LectureResponseDto> getAllLectures(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer offset
    ) {
        LectureSearchRequest req = new LectureSearchRequest();
        req.setCursor(cursor);
        req.setOffset(offset);
        req.setSortByPlan(true);
        return lectureService.searchLectures(req);
    }
}
