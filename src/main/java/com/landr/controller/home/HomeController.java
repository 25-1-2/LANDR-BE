package com.landr.controller.home;

import com.landr.controller.home.dto.HomeResponse;
import com.landr.service.scheduler.ScheduleService;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.UserProgressDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/home")
@Tag(name = "Home", description = "홈 화면 관련 API")
public class HomeController {

    private final ScheduleService scheduleService;

    // TODO: Swagger에 JWT 인증 추가
    @Operation(summary = "홈 화면 조회", security = {})
    @GetMapping()
    public ResponseEntity<HomeResponse> home() {
        // TODO: JWT에서 추출한 ID 넣기
        DailyScheduleWithLessonsDto dailySchedules = scheduleService.getUserDailySchedules(1L,
            LocalDate.now());

        UserProgressDto userProgress = scheduleService.getUserProgress(1L);

        return ResponseEntity.ok(
            HomeResponse.builder()
                .todaySchedule(dailySchedules)
                .userProgress(userProgress)
                .build()
        );
    }
}
