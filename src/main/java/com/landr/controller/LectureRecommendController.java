package com.landr.controller;

import com.landr.controller.user.dto.LectureRecommendRequest;
import com.landr.controller.user.dto.LectureRecommendResponse;
import com.landr.service.recommend.LectureRecommendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/lectures")
@RequiredArgsConstructor
@Slf4j
public class LectureRecommendController {

    private final LectureRecommendService lectureRecommendService;

    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@Valid @RequestBody LectureRecommendRequest request) {

        // 스타일 개수 체크 (최대 2개)
        if (request.getStyles() != null && request.getStyles().size() > 2) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "학습 스타일은 최대 2개까지 선택 가능합니다."));
        }

        try {
            List<LectureRecommendResponse> recommendations = lectureRecommendService.recommend(request);
            return ResponseEntity.ok(recommendations);

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("강의 추천 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "강의 추천 중 오류가 발생했습니다."));
        }
    }
}