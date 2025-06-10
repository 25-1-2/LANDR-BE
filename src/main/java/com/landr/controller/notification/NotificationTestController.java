package com.landr.controller.notification;

import com.landr.controller.CommonResponse;
import com.landr.domain.user.User;
import com.landr.service.notification.NotificationTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/notifications/test")
@RequiredArgsConstructor
@Tag(name = "Notification Test", description = "푸시 알림 테스트 API (시연용)")
public class NotificationTestController {

    private final NotificationTestService notificationTestService;

    @Operation(summary = "미완료 강의 알림 테스트", description = "오늘 미완료 강의 알림을 즉시 전송합니다.")
    @PostMapping("/incomplete-lesson")
    public ResponseEntity<CommonResponse> testIncompleteLessonNotification(
        @AuthenticationPrincipal User user
    ) {
        log.info("사용자 {}의 미완료 강의 알림 테스트 요청", user.getId());

        boolean success = notificationTestService.sendIncompleteLessonNotificationForUser(user.getId());

        String message = success ?
            "미완료 강의 알림이 성공적으로 전송되었습니다." :
            "미완료 강의가 없거나 알림 전송에 실패했습니다.";

        return ResponseEntity.ok(CommonResponse.builder()
            .message(message)
            .build());
    }

    @Operation(summary = "D-Day 알림 테스트", description = "D-Day 알림을 즉시 전송합니다.")
    @PostMapping("/dday")
    public ResponseEntity<CommonResponse> testDDayNotification(
        @AuthenticationPrincipal User user
    ) {
        log.info("사용자 {}의 D-Day 알림 테스트 요청", user.getId());

        boolean success = notificationTestService.sendDDayNotificationForUser(user.getId());

        String message = success ?
            "D-Day 알림이 성공적으로 전송되었습니다." :
            "D-Day가 없거나 알림 전송에 실패했습니다.";

        return ResponseEntity.ok(CommonResponse.builder()
            .message(message)
            .build());
    }
}