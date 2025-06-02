package com.landr.controller;


import com.landr.controller.user.dto.LectureRecommendRequest;
import com.landr.controller.user.dto.LectureRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/lectures")
@RequiredArgsConstructor
public class LectureRecommendController {

    private final com.landr.service.recommend.LectureRecommendService lectureRecommendService;

    @PostMapping("/recommend")
    public List<LectureRecommendResponse> recommendLectures(@RequestBody LectureRecommendRequest request) {
        return lectureRecommendService.recommend(request);
    }
}