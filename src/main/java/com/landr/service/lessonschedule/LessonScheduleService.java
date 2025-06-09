package com.landr.service.lessonschedule;

import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.LessonScheduleDto;
import com.landr.service.dto.WeeklyAchievementDto;
import com.landr.service.schedule.ScheduleService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LessonScheduleService {

    private final LessonScheduleRepository lessonScheduleRepository;
    private final ScheduleService scheduleService;

    @Transactional
    public Boolean toggleCheck(Long lessonScheduleId, Long userId) {
        LessonSchedule lessonSchedule = lessonScheduleRepository.findByIdAndUserId(lessonScheduleId,
            userId).orElseThrow(() -> new ApiException(ExceptionType.LESSON_SCHEDULE_NOT_FOUND));

        return lessonSchedule.toggleCheck();
    }

    /**
     * 이번주 학습 성취율 조회 이번주(월~일)에 당일 강의 수강해야되는 강의를 모두 완료한 경우 true, 아니면 false
     *
     * @param userId 사용자 ID
     * @return 요일별 학습 완료 상태를 포함한 DTO
     */
    @Transactional(readOnly = true)
    public WeeklyAchievementDto getWeeklyAchievement(Long userId) {
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        WeeklyAchievementDto.WeeklyAchievementDtoBuilder builder = WeeklyAchievementDto.builder();

        for (int i = 0; i < 7; i++) {
            LocalDate checkDate = startOfWeek.plusDays(i);
            DailyScheduleWithLessonsDto daySchedule = scheduleService.getUserDailySchedules(userId, checkDate);

            boolean isAchieved = false;
            if (daySchedule != null && !daySchedule.getLessonSchedules().isEmpty()) {
                isAchieved = daySchedule.getLessonSchedules().stream()
                    .allMatch(LessonScheduleDto::isCompleted);
            }

            switch (i) {
                case 0 -> builder.mondayAchieved(isAchieved);
                case 1 -> builder.tuesdayAchieved(isAchieved);
                case 2 -> builder.wednesdayAchieved(isAchieved);
                case 3 -> builder.thursdayAchieved(isAchieved);
                case 4 -> builder.fridayAchieved(isAchieved);
                case 5 -> builder.saturdayAchieved(isAchieved);
                case 6 -> builder.sundayAchieved(isAchieved);
            }
        }

        return builder.build();
    }

    /**
     * 요일별 학습 완료 상태를 계산합니다.
     */
    private Map<DayOfWeek, Boolean> calculateDailyAchievements(
        LocalDate startOfWeek,
        LocalDate today,
        Map<LocalDate, List<LessonSchedule>> dailyScheduleMap) {

        return Stream.iterate(startOfWeek, date -> !date.isAfter(today), date -> date.plusDays(1))
            .collect(Collectors.toMap(
                date -> convertToDayOfWeek(date.getDayOfWeek()),
                date -> isDayCompleted(date, dailyScheduleMap)
            ));
    }

    /**
     * 특정 날짜의 학습 완료 여부를 확인합니다.
     */
    private boolean isDayCompleted(LocalDate date,
        Map<LocalDate, List<LessonSchedule>> dailyScheduleMap) {
        List<LessonSchedule> schedulesForDay = dailyScheduleMap.getOrDefault(date,
            Collections.emptyList());

        if (schedulesForDay.isEmpty()) {
            return true; // 일정이 없으면 완료로 간주
        }

        return schedulesForDay.stream().allMatch(LessonSchedule::isCompleted);
    }

    /**
     * 요일별 학습 완료 상태 맵을 DTO로 변환합니다.
     */
    private WeeklyAchievementDto buildWeeklyAchievementDto(Map<DayOfWeek, Boolean> achievementMap) {
        return WeeklyAchievementDto.builder()
            .mondayAchieved(achievementMap.getOrDefault(DayOfWeek.MON, false))
            .tuesdayAchieved(achievementMap.getOrDefault(DayOfWeek.TUE, false))
            .wednesdayAchieved(achievementMap.getOrDefault(DayOfWeek.WED, false))
            .thursdayAchieved(achievementMap.getOrDefault(DayOfWeek.THU, false))
            .fridayAchieved(achievementMap.getOrDefault(DayOfWeek.FRI, false))
            .saturdayAchieved(achievementMap.getOrDefault(DayOfWeek.SAT, false))
            .sundayAchieved(achievementMap.getOrDefault(DayOfWeek.SUN, false))
            .build();
    }

    /**
     * Java의 DayOfWeek을 애플리케이션의 DayOfWeek 열거형으로 변환합니다.
     */
    private DayOfWeek convertToDayOfWeek(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DayOfWeek.MON;
            case TUESDAY -> DayOfWeek.TUE;
            case WEDNESDAY -> DayOfWeek.WED;
            case THURSDAY -> DayOfWeek.THU;
            case FRIDAY -> DayOfWeek.FRI;
            case SATURDAY -> DayOfWeek.SAT;
            case SUNDAY -> DayOfWeek.SUN;
        };
    }
}