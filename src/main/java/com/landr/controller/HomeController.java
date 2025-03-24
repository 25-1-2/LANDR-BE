package com.landr.controller;

import com.landr.controller.dto.HomeResponseDto;
import com.landr.service.scheduler.ScheduleService;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.UserProgressDto;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/home")
public class HomeController {

    private final ScheduleService scheduleService;

    @GetMapping()
    public ResponseEntity<HomeResponseDto> home() {
        // TODO: JWT에서 추출한 ID 넣기
        DailyScheduleWithLessonsDto dailySchedules = scheduleService.getUserDailySchedules(1L,
            LocalDate.now());

        UserProgressDto userProgress = scheduleService.getUserProgress(1L);

        return ResponseEntity.ok(
            HomeResponseDto.builder()
                .todaySchedule(dailySchedules)
                .userProgress(userProgress)
                .build()
        );
    }
}
