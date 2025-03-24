package com.landr.controller;

import com.landr.service.ScheduleService;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/home")
public class HomeController {
    private final ScheduleService scheduleService;

    @GetMapping()
    public DailyScheduleWithLessonsDto home() {
        // TODO: JWT에서 추출한 ID 넣기
        return scheduleService.getUserDailySchedules(1L, LocalDate.now());
    }
}
