package com.landr.service.scheduler;

import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.LectureProgressDto;
import com.landr.service.dto.LessonScheduleDto;
import com.landr.service.dto.UserProgressDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<DailySchedule> dailySchedules = dailyScheduleRepository.findByUserIdAndDate(userId,
            date);

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
            .lessonSchedules(allLessonSchedules.stream().map(LessonScheduleDto::convert)
                .collect(Collectors.toList()))
            .date(date)
            .totalLessons(allLessonSchedules.size())
            .totalDuration(
                allLessonSchedules.stream().mapToInt(LessonSchedule::getAdjustedDuration).sum())
            .dayOfWeek(dailySchedules.get(0).getDayOfWeek())
            .build();
    }

    /**
     * 특정 유저의 강의별 진행 상황을 조회합니다.
     */
    public UserProgressDto getUserProgress(Long userId) {
        // 사용자의 모든 LessonSchedule 조회
        List<LessonSchedule> allLessonSchedules = lessonScheduleRepository.findAllByUserIdGroupedByLecture(
            userId);

        // plan별로 그룹화
        Map<Plan, List<LessonSchedule>> planMap = allLessonSchedules.stream()
            .collect(Collectors.groupingBy(ls -> ls.getDailySchedule().getPlan()));

        // 강의별 진행 상황 계산
        List<LectureProgressDto> lectureProgressList = new ArrayList<>();
        int totalCompletedLessons = 0;
        int totalLessons = allLessonSchedules.size();

        for (Map.Entry<Plan, List<LessonSchedule>> entry : planMap.entrySet()) {
            Plan plan = entry.getKey();
            List<LessonSchedule> lessonSchedules = entry.getValue();

            // 첫 번째 LessonSchedule에서 Lecture 정보를 가져옴
            String lectureTitle = lessonSchedules.isEmpty() ? "" :
                lessonSchedules.get(0).getLesson().getLecture().getTitle();

            int completedLessons = (int) lessonSchedules.stream()
                .filter(LessonSchedule::isCompleted)
                .count();

            totalCompletedLessons += completedLessons;

            lectureProgressList.add(LectureProgressDto.builder()
                .planId(plan.getId())
                .lectureAlias(plan.getLectureName())
                .lectureName(lectureTitle)
                .completedLessons(completedLessons)
                .totalLessons(lessonSchedules.size())
                .build());
        }

        // 결과 반환
        return UserProgressDto.builder()
            .lectureProgress(lectureProgressList)
            .totalCompletedLessons(totalCompletedLessons)
            .totalLessons(totalLessons)
            .build();
    }
}
