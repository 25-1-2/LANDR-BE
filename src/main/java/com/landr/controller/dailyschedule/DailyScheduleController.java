package com.landr.controller.dailyschedule;

import com.landr.domain.user.User;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Operation(summary = "일일 스케줄 조회", description = "날짜 형식은 2025-04-01 형식을 지켜야 합니다.")
    @GetMapping
    public ResponseEntity<DailyScheduleWithLessonsDto> getDailySchedule(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @AuthenticationPrincipal User user
    ) {
        DailyScheduleWithLessonsDto dailySchedules = scheduleService.getUserDailySchedules(
            user.getId(),
            date);

        if (dailySchedules == null) {
            dailySchedules = new DailyScheduleWithLessonsDto();
        }
        return ResponseEntity.ok(dailySchedules);
    }
}
