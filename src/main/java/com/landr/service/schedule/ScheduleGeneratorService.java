package com.landr.service.schedule;

import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.plan.PlanType;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleGeneratorService {

    private final LessonRepository lessonRepository;
    private final DailyScheduleRepository dailyScheduleRepository;
    private final LessonScheduleRepository lessonScheduleRepository;

    /**
     * Plan에 대한 스케줄을 생성합니다.
     *
     * @param plan 스케줄을 생성할 Plan
     * @return 생성된 일일 스케줄과 강의 스케줄이 포함된 결과 객체
     */
    @Transactional
    public ScheduleGenerationResult generateSchedules(Plan plan) {
        log.info("Plan {}에 대한 스케줄 생성 시작", plan.getId());

        // 1. 강의 목록 조회
        List<Lesson> lessons = getLessonsBetween(plan);
        if (lessons.isEmpty()) {
            log.warn("Plan {}에 대한 강의가 없습니다.", plan.getId());
            return new ScheduleGenerationResult(Collections.emptyList(), Collections.emptyList());
        }

        // 2. 스케줄 생성
        ScheduleGenerationResult result = buildSchedules(plan, lessons);

        dailyScheduleRepository.saveAll(result.getDailySchedules());
        lessonScheduleRepository.saveAll(result.getLessonSchedules());
        log.info("Plan {}에 대한 스케줄 생성 완료: 일일스케줄 {}개, 강의스케줄 {}개",
            plan.getId(), result.getDailySchedules().size(), result.getLessonSchedules().size());

        return result;
    }

    /**
     * 시작 강의부터 종료 강의까지의 강의 목록을 조회합니다.
     */
    private List<Lesson> getLessonsBetween(Plan plan) {
        Lesson startLesson = plan.getStartLesson();
        Lesson endLesson = plan.getEndLesson();

        String lectureId = startLesson.getLecture().getId();
        int startOrder = startLesson.getOrder();
        int endOrder = endLesson.getOrder();

        log.info("lectureId={}, startOrder={}, endOrder={} 범위의 강의 조회", lectureId, startOrder,
            endOrder);

        return lessonRepository.findByLectureIdAndOrderBetweenOrderByOrder(
            lectureId, startOrder, endOrder);
    }

    /**
     * 강의 목록과 Plan 정보를 바탕으로 스케줄을 생성합니다.
     */
    private ScheduleGenerationResult buildSchedules(Plan plan, List<Lesson> lessons) {
        // 강의별 조정된 시간 계산 (재생 속도 반영)
        List<Integer> adjustedDurations = calculateAdjustedDurations(lessons,
            plan.getPlaybackSpeed());

        // 계획 타입에 따라 강의 배분
        if (plan.getPlanType() == PlanType.PERIOD) {
            // PERIOD 타입인 경우 startDate과 endDate 사이의 studyDays 날짜 계산
            List<LocalDate> studyDates = calculateStudyDates(plan);
            if (studyDates.isEmpty()) {
                log.warn("Plan {}에 대한 공부 가능 날짜가 없습니다.", plan.getId());
                return new ScheduleGenerationResult(Collections.emptyList(),
                    Collections.emptyList());
            }

            return distributeLessonsByPeriod(plan, lessons, adjustedDurations, studyDates);
        } else { // TIME 타입
            // TIME 타입은 studyDays와 dailyTime만 필요
            return distributeLessonsByTime(plan, lessons, adjustedDurations, null);
        }
    }

    /**
     * 시작일부터 종료일까지 studyDays에 해당하는 날짜 목록을 계산합니다.
     */
    private List<LocalDate> calculateStudyDates(Plan plan) {
        List<LocalDate> studyDates = new ArrayList<>();
        LocalDate startDate = plan.getStartDate();
        LocalDate endDate = plan.getEndDate();
        Set<DayOfWeek> studyDays = plan.getStudyDays();

        if (startDate == null || endDate == null || studyDays.isEmpty()) {
            return studyDates;
        }

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = convertToDayOfWeek(currentDate.getDayOfWeek());
            if (studyDays.contains(dayOfWeek)) {
                studyDates.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        return studyDates;
    }

    /**
     * 강의 목록의 각 강의에 대해 playbackSpeed를 반영한 조정된 시간을 계산합니다.
     */
    private List<Integer> calculateAdjustedDurations(List<Lesson> lessons, float playbackSpeed) {
        return lessons.stream()
            .map(lesson -> Math.round(lesson.getDuration() / playbackSpeed))
            .collect(Collectors.toList());
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

    /**
     * PERIOD 타입의 계획에 따라 강의를 배분합니다. 시작일부터 종료일까지 studyDays에 해당하는 날짜에 강의 시간이 최대한 균등하게 배분되도록 합니다.
     */
    private ScheduleGenerationResult distributeLessonsByPeriod(
        Plan plan, List<Lesson> lessons, List<Integer> adjustedDurations,
        List<LocalDate> studyDates) {

        List<DailySchedule> dailySchedules = new ArrayList<>();
        List<LessonSchedule> lessonSchedules = new ArrayList<>();

        int totalLessons = lessons.size();
        int totalDuration = adjustedDurations.stream().mapToInt(Integer::intValue).sum();
        int totalDays = studyDates.size();

        log.info("PERIOD 타입 스케줄 생성: 총 강의 {}개, 총 시간 {}분, 총 공부 날짜 {}일",
            totalLessons, totalDuration, totalDays);

        // 일별 평균 강의 시간 계산 * 1.4배 -> 짧은 강의만 있는 경우를 고려
        double avgDurationPerDay = ((double) totalDuration / totalDays) * 1.4;
        log.info("일별 평균 강의 시간: {}분", avgDurationPerDay);

        // 1. 먼저 각 날짜에 배정할 강의를 결정
        Map<LocalDate, List<LessonAssignment>> dateToLessonsMap = new HashMap<>();
        int lessonIndex = 0;

        for (int dateIndex = 0; dateIndex < totalDays && lessonIndex < totalLessons; dateIndex++) {
            LocalDate currentDate = studyDates.get(dateIndex);
            List<LessonAssignment> assignmentsForDay = new ArrayList<>();

            int currentDayDuration = 0;

            // 현재 날짜에 강의 배정
            while (lessonIndex < totalLessons) {
                int lessonDuration = adjustedDurations.get(lessonIndex);

                // 마지막 날짜가 아니고, 평균 시간을 초과하면 다음 날짜로 넘어감
                // 단, 해당 날짜에 이미 수업이 배정된 경우에만
                if (dateIndex < totalDays - 1
                    && currentDayDuration + lessonDuration > avgDurationPerDay
                    && !assignmentsForDay.isEmpty()) {
                    break;
                }

                assignmentsForDay.add(new LessonAssignment(
                    lessons.get(lessonIndex), lessonDuration, assignmentsForDay.size() + 1));

                currentDayDuration += lessonDuration;
                lessonIndex++;
            }

            dateToLessonsMap.put(currentDate, assignmentsForDay);
        }

        // 2. 각 날짜별로 DailySchedule과 LessonSchedule 생성
        for (Map.Entry<LocalDate, List<LessonAssignment>> entry : dateToLessonsMap.entrySet()) {
            LocalDate date = entry.getKey();
            List<LessonAssignment> assignments = entry.getValue();

            if (assignments.isEmpty()) {
                continue;
            }

            // 총 강의 수와 시간 계산
            int totalLessonsForDay = assignments.size();
            int totalDurationForDay = assignments.stream()
                .mapToInt(LessonAssignment::getAdjustedDuration)
                .sum();

            // DailySchedule 생성
            DailySchedule dailySchedule = createNewDailySchedule(
                plan, date, totalLessonsForDay, totalDurationForDay);
            dailySchedules.add(dailySchedule);

            // LessonSchedule 생성
            for (LessonAssignment assignment : assignments) {
                LessonSchedule lessonSchedule = createNewLessonSchedule(
                    dailySchedule,
                    assignment.getLesson(),
                    assignment.getAdjustedDuration(),
                    assignment.getDisplayOrder());
                lessonSchedules.add(lessonSchedule);
            }
        }

        return new ScheduleGenerationResult(dailySchedules, lessonSchedules);
    }

    /**
     * TIME 타입의 계획에 따라 강의를 배분합니다. studyDays에 지정된 요일마다 dailyTime에 맞게 강의를 배분합니다. TIME 타입의 경우
     * startDate와 endDate를 사용하지 않습니다.
     */
    private ScheduleGenerationResult distributeLessonsByTime(
        Plan plan, List<Lesson> lessons, List<Integer> adjustedDurations,
        List<LocalDate> studyDates) {

        List<DailySchedule> dailySchedules = new ArrayList<>();
        List<LessonSchedule> lessonSchedules = new ArrayList<>();

        int totalLessons = lessons.size();
        int dailyTimeTarget = plan.getDailyTime();

        log.info("TIME 타입 스케줄 생성: 총 강의 {}개, 일일 목표 시간 {}분", totalLessons, dailyTimeTarget);

        // 수강할 요일 가져오기
        Set<DayOfWeek> studyDays = plan.getStudyDays();
        if (studyDays.isEmpty()) {
            log.warn("공부할 요일이 지정되지 않았습니다.");
            return new ScheduleGenerationResult(dailySchedules, lessonSchedules);
        }

        // 요일을 순서대로 정렬 (월화수목금토일)
        List<DayOfWeek> orderedStudyDays = studyDays.stream()
            .sorted(Comparator.comparingInt(this::getDayOfWeekOrder))
            .collect(Collectors.toList());

        // 오늘부터 시작하여 강의 배정
        LocalDate currentDate = LocalDate.now();
        int dayIndex = 0;
        int lessonIndex = 0;

        // 첫 공부 요일로 이동
        while (!studyDays.contains(convertToDayOfWeek(currentDate.getDayOfWeek()))) {
            currentDate = currentDate.plusDays(1);
        }

        // 모든 강의를 배정할 때까지 반복
        while (lessonIndex < totalLessons) {
            // 현재 날짜의 요일
            DayOfWeek currentDayOfWeek = convertToDayOfWeek(currentDate.getDayOfWeek());

            // 공부 요일인 경우에만 강의 배정
            if (studyDays.contains(currentDayOfWeek)) {
                List<LessonAssignment> assignmentsForDay = new ArrayList<>();
                int currentDayDuration = 0;

                // 현재 날짜에 강의 배정 (dailyTime 이내로)
                while (lessonIndex < totalLessons) {
                    int lessonDuration = adjustedDurations.get(lessonIndex);

                    // dailyTime을 초과하고, 이미 최소 1개의 강의가 배정된 경우 다음 날짜로 넘어감
                    if (currentDayDuration + lessonDuration > dailyTimeTarget
                        && !assignmentsForDay.isEmpty()) {
                        break;
                    }

                    assignmentsForDay.add(new LessonAssignment(
                        lessons.get(lessonIndex), lessonDuration, assignmentsForDay.size() + 1));

                    currentDayDuration += lessonDuration;
                    lessonIndex++;
                }

                // 강의가 배정된 경우에만 DailySchedule 생성
                if (!assignmentsForDay.isEmpty()) {
                    // 총 강의 수와 시간 계산
                    int totalLessonsForDay = assignmentsForDay.size();
                    int totalDurationForDay = assignmentsForDay.stream()
                        .mapToInt(LessonAssignment::getAdjustedDuration)
                        .sum();

                    // DailySchedule 생성
                    DailySchedule dailySchedule = createNewDailySchedule(
                        plan, currentDate, totalLessonsForDay, totalDurationForDay);
                    dailySchedules.add(dailySchedule);

                    // LessonSchedule 생성
                    for (LessonAssignment assignment : assignmentsForDay) {
                        LessonSchedule lessonSchedule = createNewLessonSchedule(
                            dailySchedule,
                            assignment.getLesson(),
                            assignment.getAdjustedDuration(),
                            assignment.getDisplayOrder());
                        lessonSchedules.add(lessonSchedule);
                    }
                }
            }

            // 다음 날짜로 이동
            currentDate = currentDate.plusDays(1);
        }

        return new ScheduleGenerationResult(dailySchedules, lessonSchedules);
    }

    /**
     * DayOfWeek의 순서를 숫자로 반환합니다. (MON:1, TUE:2, ...)
     */
    private int getDayOfWeekOrder(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MON:
                return 1;
            case TUE:
                return 2;
            case WED:
                return 3;
            case THU:
                return 4;
            case FRI:
                return 5;
            case SAT:
                return 6;
            case SUN:
                return 7;
            default:
                return 8; // 예상치 못한 값
        }
    }

    /**
     * 새로운 DailySchedule 객체를 생성합니다.
     */
    private DailySchedule createNewDailySchedule(
        Plan plan, LocalDate date, int totalLessons, int totalDuration) {
        return DailySchedule.builder()
            .plan(plan)
            .date(date)
            .dayOfWeek(convertToDayOfWeek(date.getDayOfWeek()))
            .totalLessons(totalLessons)
            .totalDuration(totalDuration)
            .build();
    }

    /**
     * 새로운 LessonSchedule 객체를 생성합니다.
     */
    private LessonSchedule createNewLessonSchedule(
        DailySchedule dailySchedule, Lesson lesson, int adjustedDuration, int displayOrder) {
        return LessonSchedule.builder()
            .dailySchedule(dailySchedule)
            .lesson(lesson)
            .adjustedDuration(adjustedDuration)
            .displayOrder(displayOrder)
            .completed(false)
            .build();
    }

    /**
     * 강의 배정 정보를 담는 내부 클래스
     */
    private static class LessonAssignment {

        private final Lesson lesson;
        private final int adjustedDuration;
        private final int displayOrder;

        public LessonAssignment(Lesson lesson, int adjustedDuration, int displayOrder) {
            this.lesson = lesson;
            this.adjustedDuration = adjustedDuration;
            this.displayOrder = displayOrder;
        }

        public Lesson getLesson() {
            return lesson;
        }

        public int getAdjustedDuration() {
            return adjustedDuration;
        }

        public int getDisplayOrder() {
            return displayOrder;
        }
    }

    /**
     * 스케줄 생성 결과를 담는 클래스
     */
    @Getter
    public static class ScheduleGenerationResult {

        private final List<DailySchedule> dailySchedules;
        private final List<LessonSchedule> lessonSchedules;

        public ScheduleGenerationResult(List<DailySchedule> dailySchedules,
            List<LessonSchedule> lessonSchedules) {
            this.dailySchedules = dailySchedules;
            this.lessonSchedules = lessonSchedules;
        }

    }
}