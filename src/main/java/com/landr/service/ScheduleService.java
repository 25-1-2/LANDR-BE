package com.landr.service;

import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.LessonScheduleDto;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleService {

    private final DailyScheduleRepository dailyScheduleRepository;
    private final LessonScheduleRepository lessonScheduleRepository;

    /**
     * 특정 유저의 특정 날짜 일정과 수업 일정을 함께 조회합니다.
     */
    public DailyScheduleWithLessonsDto getUserDailySchedules(Long userId, LocalDate date) {
        // 1. 유저 ID와 날짜로 DailySchedule 목록 조회
        List<DailySchedule> dailySchedules = dailyScheduleRepository.findByUserIdAndDate(userId, date);

        if (dailySchedules.isEmpty()) {
            return null;
        }

        // 2. DailySchedule ID 목록 추출
        List<Long> dailyScheduleIds = dailySchedules.stream()
            .map(DailySchedule::getId)
            .collect(Collectors.toList());

        // 3. 모든 DailySchedule에 해당하는 LessonSchedule 목록 한 번에 조회
        List<LessonSchedule> allLessonSchedules =
            lessonScheduleRepository.findByDailyScheduleIdsWithLessonAndLecture(dailyScheduleIds);

        return DailyScheduleWithLessonsDto.builder()
            .lessonSchedules(allLessonSchedules.stream().map(LessonScheduleDto::convert).collect(Collectors.toList()))
            .date(date)
            .totalLessons(allLessonSchedules.size())
            .totalDuration(allLessonSchedules.stream().mapToInt(LessonSchedule::getAdjustedDuration).sum())
            .dayOfWeek(dailySchedules.get(0).getDayOfWeek())
            .build();
    }
}
