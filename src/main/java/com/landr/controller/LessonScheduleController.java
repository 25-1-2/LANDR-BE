package com.landr.controller;

import com.landr.service.lessonschedule.LessonScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/lesson-schedules")
@Tag(name = "Home", description = "홈 화면 관련 API")
public class LessonScheduleController {

    private final LessonScheduleService lessonScheduleService;

    // TODO: Swagger에 JWT 인증 추가 & @AuthenticationPrincipal User user 추가
    @PatchMapping("/{lessonScheduleId}/check/toggle")
    public ResponseEntity<String> toggleCheck(
        @PathVariable Long lessonScheduleId
    ) {
        Long userId = 1L;
        Boolean curStatus = lessonScheduleService.toggleCheck(lessonScheduleId, userId);

        return ResponseEntity.ok("현재 체크 상태: " + curStatus);
    }
}
