package com.landr.controller.home;

import com.landr.controller.dday.dto.DDayDto;
import com.landr.controller.home.dto.HomeResponse;
import com.landr.domain.user.User;
import com.landr.service.dday.DDayService;
import com.landr.service.dto.WeeklyAchievementDto;
import com.landr.service.lessonschedule.LessonScheduleService;
import com.landr.service.schedule.ScheduleService;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.UserProgressDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/home")
@Tag(name = "Home", description = "홈 화면 관련 API")
public class HomeController {

    private final ScheduleService scheduleService;
    private final LessonScheduleService lessonScheduleService;
    private final DDayService dDayService;

    @Operation(summary = "홈 화면 조회")
    @GetMapping()
    public ResponseEntity<HomeResponse> home(@AuthenticationPrincipal User user) {
        Long userId = user.getId();
        DailyScheduleWithLessonsDto dailySchedules = scheduleService.getUserDailySchedules(userId,
            LocalDate.now());

        UserProgressDto userProgress = scheduleService.getUserProgress(userId);

        WeeklyAchievementDto weeklyAchievement = lessonScheduleService.getWeeklyAchievement(userId);

        DDayDto dDay = dDayService.getOneUserDDay(userId);

        return ResponseEntity.ok(
            HomeResponse.builder()
                .todaySchedule(dailySchedules)
                .userProgress(userProgress)
                .weeklyAchievement(weeklyAchievement)
                .dDay(dDay)
                .build()
        );
    }
}
