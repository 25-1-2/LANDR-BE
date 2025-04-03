package com.landr.controller.dailyschedule;

import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.scheduler.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/daily-schedules")
@AllArgsConstructor
@Tag(name = "DailySchedule", description = "DailySchedule 관련 API")
public class DailyScheduleController {

    private final ScheduleService scheduleService;

    // TODO: Swagger에 JWT 인증 추가
    @Operation(summary = "일일 스케줄 조회", security = {})
    @GetMapping
    public ResponseEntity<DailyScheduleWithLessonsDto> getDailySchedule(
        @RequestParam LocalDate date) {
        // TODO: SecurityContext에서 User 추출
        Long userId = 1L;
        DailyScheduleWithLessonsDto dailySchedules = scheduleService.getUserDailySchedules(userId,
            date);

        return ResponseEntity.ok(dailySchedules);
    }
}
