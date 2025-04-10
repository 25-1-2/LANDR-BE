package com.landr.controller.lessonschedule;

import com.landr.controller.lessonschedule.dto.ToggleCheckResponse;
import com.landr.domain.user.User;
import com.landr.service.lessonschedule.LessonScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Operation(summary = "강의 수강 체크 토글")
    @PatchMapping("/{lessonScheduleId}/check/toggle")
    public ResponseEntity<ToggleCheckResponse> toggleCheck(
        @PathVariable Long lessonScheduleId,
        @AuthenticationPrincipal User user
    ) {
        Long userId = user.getId();
        Boolean curStatus = lessonScheduleService.toggleCheck(lessonScheduleId, userId);

        return ResponseEntity.ok(
            ToggleCheckResponse.builder()
                .lessonScheduleId(lessonScheduleId)
                .checked(curStatus)
                .build()
        );
    }
}
