package com.landr.service.schedule;

import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.plan.PlanType;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final PlanRepository planRepository;
    private final EntityManager entityManager;

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
     * 특정 계획(Plan)의 모든 스케줄을 재생성합니다.
     * - completed = true인 강의들은 updatedAt 날짜 기준으로 재배치
     * - completed = false인 강의들은 오늘부터 재스케줄링
     *
     * @param userId 사용자 ID
     * @param planId 계획 ID
     * @return 생성된 스케줄 정보
     */
    @Transactional
    public void rescheduleIncompleteLessons(Long userId, Long planId) {
        // 해당 계획이 사용자의 것인지 확인
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
            .orElseThrow(() -> new ApiException(ExceptionType.PLAN_NOT_FOUND));

        // 계획이 PERIOD 타입이고 종료일이 이미 지난 경우 확인
        LocalDate today = LocalDate.now();
        if (plan.getPlanType() == PlanType.PERIOD &&
            (plan.getEndDate() == null || plan.getEndDate().isBefore(today))) {
            throw new ApiException(ExceptionType.PLAN_END_DATE_PASSED,
                "계획의 종료일이 이미 지났습니다. 종료일을 업데이트한 후 다시 시도해주세요.");
        }

        log.info("Plan {}에 대한 전체 스케줄 재생성 시작", planId);

        // 해당 계획의 모든 LessonSchedule 조회
        List<LessonSchedule> allLessonSchedules = lessonScheduleRepository.findByPlanIdAndUserId(userId, planId);

        if (allLessonSchedules.isEmpty()) {
            log.info("재스케줄링할 강의가 없습니다.");
            return;
        }

        // completed 상태별로 분류
        List<LessonSchedule> completedLessonSchedules = allLessonSchedules.stream()
            .filter(LessonSchedule::isCompleted)
            .collect(Collectors.toList());

        List<LessonSchedule> uncompletedLessonSchedules = allLessonSchedules.stream()
            .filter(ls -> !ls.isCompleted())
            .collect(Collectors.toList());

        log.info("완료된 LessonSchedule 개수: {}", completedLessonSchedules.size());
        log.info("미완료된 LessonSchedule 개수: {}", uncompletedLessonSchedules.size());

        // 기존 모든 스케줄 삭제
        Set<Long> allDailyScheduleIds = allLessonSchedules.stream()
            .map(ls -> ls.getDailySchedule().getId())
            .collect(Collectors.toSet());

        lessonScheduleRepository.deleteAll(allLessonSchedules);

        // 비어있는 DailySchedule 찾아서 삭제
        List<Long> emptyDailyScheduleIds = allDailyScheduleIds.stream()
            .filter(dsId -> lessonScheduleRepository.countByDailyScheduleId(dsId) == 0)
            .collect(Collectors.toList());

        if (!emptyDailyScheduleIds.isEmpty()) {
            log.info("비어있는 DailySchedule {} 개 삭제", emptyDailyScheduleIds.size());
            dailyScheduleRepository.deleteAllByIdInBatch(emptyDailyScheduleIds);
        }

        // 영속성 컨텍스트 클리어
        entityManager.flush();
        entityManager.clear();

        // === 새로운 로직 ===

        // 1. 완료된 강의들을 updatedAt 날짜별로 그룹화
        Map<LocalDate, List<LessonSchedule>> completedByDate = completedLessonSchedules.stream()
            .collect(Collectors.groupingBy(ls -> {
                LocalDateTime updatedAt = ls.getUpdatedAt();
                return updatedAt != null ? updatedAt.toLocalDate() : LocalDate.now();
            }));

        // 2. 미완료 강의들에 대한 Lesson 추출 및 정렬
        List<Lesson> uncompletedLessons = uncompletedLessonSchedules.stream()
            .map(LessonSchedule::getLesson)
            .distinct()
            .sorted(Comparator.comparing(Lesson::getOrder))
            .collect(Collectors.toList());

        // 3. 완료된 강의들의 DailySchedule 먼저 생성
        Map<LocalDate, DailySchedule> existingDailySchedules = new HashMap<>();
        List<LessonSchedule> allNewLessonSchedules = new ArrayList<>();

        for (Map.Entry<LocalDate, List<LessonSchedule>> entry : completedByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<LessonSchedule> lessonsForDate = entry.getValue();

            int totalLessons = lessonsForDate.size();
            int totalDuration = lessonsForDate.stream()
                .mapToInt(LessonSchedule::getAdjustedDuration)
                .sum();

            DailySchedule dailySchedule = createNewDailySchedule(plan, date, totalLessons, totalDuration);
            existingDailySchedules.put(date, dailySchedule);

            // 완료된 LessonSchedule 생성
            lessonsForDate.sort(Comparator.comparing(ls -> ls.getLesson().getOrder()));
            for (int i = 0; i < lessonsForDate.size(); i++) {
                LessonSchedule originalLs = lessonsForDate.get(i);
                LessonSchedule newLessonSchedule = LessonSchedule.builder()
                    .dailySchedule(dailySchedule)
                    .lesson(originalLs.getLesson())
                    .adjustedDuration(originalLs.getAdjustedDuration())
                    .displayOrder(i + 1)
                    .completed(true)
                    .updatedAt(originalLs.getUpdatedAt())
                    .build();
                allNewLessonSchedules.add(newLessonSchedule);
            }
        }

        // 4. 미완료 강의들을 오늘부터 재스케줄링
        List<DailySchedule> newDailySchedules = new ArrayList<>();
        if (!uncompletedLessons.isEmpty()) {
            ScheduleGenerationResult uncompletedResult = buildSchedulesForLessonsFromToday(plan, uncompletedLessons);

            for (DailySchedule uncompletedDs : uncompletedResult.getDailySchedules()) {
                LocalDate date = uncompletedDs.getDate();

                if (existingDailySchedules.containsKey(date)) {
                    // 기존 DailySchedule에 추가
                    DailySchedule existingDs = existingDailySchedules.get(date);
                    existingDs.addLessons(uncompletedDs.getTotalLessons(), uncompletedDs.getTotalDuration());

                    // 해당 날짜의 미완료 LessonSchedule들을 기존 DailySchedule에 연결하고 displayOrder 조정
                    List<LessonSchedule> uncompletedLsForDate = uncompletedResult.getLessonSchedules().stream()
                        .filter(ls -> ls.getDailySchedule().getDate().equals(date))
                        .collect(Collectors.toList());

                    // displayOrder 재조정 (기존 완료된 강의들 다음부터)
                    int nextDisplayOrder = existingDs.getTotalLessons() - uncompletedDs.getTotalLessons() + 1;
                    for (LessonSchedule ls : uncompletedLsForDate) {
                        LessonSchedule newLs = LessonSchedule.builder()
                            .dailySchedule(existingDs)
                            .lesson(ls.getLesson())
                            .adjustedDuration(ls.getAdjustedDuration())
                            .displayOrder(nextDisplayOrder++)
                            .completed(false)
                            .build();
                        allNewLessonSchedules.add(newLs);
                    }
                } else {
                    // 새로운 날짜는 그대로 추가
                    newDailySchedules.add(uncompletedDs);
                    allNewLessonSchedules.addAll(
                        uncompletedResult.getLessonSchedules().stream()
                            .filter(ls -> ls.getDailySchedule().getDate().equals(date))
                            .collect(Collectors.toList())
                    );
                }
            }
        }

        // 5. 모든 DailySchedule 수집
        List<DailySchedule> allDailySchedules = new ArrayList<>();
        allDailySchedules.addAll(existingDailySchedules.values());
        allDailySchedules.addAll(newDailySchedules);

        // 6. 저장
        dailyScheduleRepository.saveAll(allDailySchedules);
        lessonScheduleRepository.saveAll(allNewLessonSchedules);

        log.info("Plan {}에 대한 재스케줄 생성 완료: 일일스케줄 {}개, 강의스케줄 {}개",
            plan.getId(), allDailySchedules.size(), allNewLessonSchedules.size());
    }
    /**
     * 완료된 강의들을 updatedAt 날짜 기준으로 DailySchedule을 생성합니다.
     */
    private List<DailySchedule> createDailySchedulesForCompletedLessons(Plan plan, List<LessonSchedule> completedLessonSchedules) {
        if (completedLessonSchedules.isEmpty()) {
            return new ArrayList<>();
        }

        // updatedAt 날짜별로 그룹화
        Map<LocalDate, List<LessonSchedule>> dateGroupedLessons = completedLessonSchedules.stream()
            .collect(Collectors.groupingBy(ls -> {
                LocalDateTime updatedAt = ls.getUpdatedAt();
                return updatedAt != null ? updatedAt.toLocalDate() : LocalDate.now();
            }));

        List<DailySchedule> dailySchedules = new ArrayList<>();

        for (Map.Entry<LocalDate, List<LessonSchedule>> entry : dateGroupedLessons.entrySet()) {
            LocalDate date = entry.getKey();
            List<LessonSchedule> lessonsForDate = entry.getValue();

            int totalLessons = lessonsForDate.size();
            int totalDuration = lessonsForDate.stream()
                .mapToInt(LessonSchedule::getAdjustedDuration)
                .sum();

            DailySchedule dailySchedule = createNewDailySchedule(plan, date, totalLessons, totalDuration);
            dailySchedules.add(dailySchedule);
        }

        return dailySchedules;
    }

    /**
     * 완료된 강의들에 대한 새로운 LessonSchedule을 생성합니다.
     */
    private List<LessonSchedule> createLessonSchedulesForCompleted(List<DailySchedule> dailySchedules, List<LessonSchedule> completedLessonSchedules) {
        if (completedLessonSchedules.isEmpty() || dailySchedules.isEmpty()) {
            return new ArrayList<>();
        }

        // 날짜별 DailySchedule 맵 생성
        Map<LocalDate, DailySchedule> dateToScheduleMap = dailySchedules.stream()
            .collect(Collectors.toMap(DailySchedule::getDate, ds -> ds));

        List<LessonSchedule> newLessonSchedules = new ArrayList<>();

        // updatedAt 날짜별로 그룹화
        Map<LocalDate, List<LessonSchedule>> dateGroupedLessons = completedLessonSchedules.stream()
            .collect(Collectors.groupingBy(ls -> {
                LocalDateTime updatedAt = ls.getUpdatedAt();
                return updatedAt != null ? updatedAt.toLocalDate() : LocalDate.now();
            }));

        for (Map.Entry<LocalDate, List<LessonSchedule>> entry : dateGroupedLessons.entrySet()) {
            LocalDate date = entry.getKey();
            List<LessonSchedule> lessonsForDate = entry.getValue();
            DailySchedule dailySchedule = dateToScheduleMap.get(date);

            if (dailySchedule == null) {
                log.warn("DailySchedule not found for date: {}", date);
                continue;
            }

            // Lesson order 기준으로 정렬하여 displayOrder 설정
            lessonsForDate.sort(Comparator.comparing(ls -> ls.getLesson().getOrder()));

            for (int i = 0; i < lessonsForDate.size(); i++) {
                LessonSchedule originalLs = lessonsForDate.get(i);

                LessonSchedule newLessonSchedule = LessonSchedule.builder()
                    .dailySchedule(dailySchedule)
                    .lesson(originalLs.getLesson())
                    .adjustedDuration(originalLs.getAdjustedDuration())
                    .displayOrder(i + 1)
                    .completed(true) // 완료된 상태 유지
                    .updatedAt(originalLs.getUpdatedAt()) // 기존 updatedAt 유지
                    .build();

                newLessonSchedules.add(newLessonSchedule);
            }
        }

        return newLessonSchedules;
    }

    /**
     * 시작 강의부터 종료 강의까지의 강의 목록을 조회합니다.
     */
    private List<Lesson> getLessonsBetween(Plan plan) {
        Lesson startLesson = plan.getStartLesson();
        Lesson endLesson = plan.getEndLesson();

        Long lectureId = startLesson.getLecture().getId();
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
     * 특정 날짜부터 종료일까지 studyDays에 해당하는 날짜 목록을 계산합니다.
     */
    private List<LocalDate> calculateStudyDatesFromDate(Plan plan, LocalDate startDate,
        LocalDate endDate) {
        List<LocalDate> studyDates = new ArrayList<>();
        Set<DayOfWeek> studyDays = plan.getStudyDays();

        if (endDate == null || studyDays.isEmpty()) {
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
     * 오늘 날짜부터 강의들에 대한 새로운 일정을 생성합니다.
     */
    private ScheduleGenerationResult buildSchedulesForLessonsFromToday(Plan plan,
        List<Lesson> lessons) {
        // 강의별 조정된 시간 계산 (재생 속도 반영)
        List<Integer> adjustedDurations = calculateAdjustedDurations(lessons,
            plan.getPlaybackSpeed());

        LocalDate today = LocalDate.now();

        // 계획 타입에 따라 강의 배분
        if (plan.getPlanType() == PlanType.PERIOD) {
            // PERIOD 타입인 경우, 오늘부터 endDate까지의 studyDays 날짜 계산
            LocalDate endDate = plan.getEndDate();

            // endDate가 없거나 과거인 경우 재스케줄링하지 않음
            if (endDate == null || endDate.isBefore(today)) {
                log.warn("Plan {}의 종료일({})이 오늘({})보다 이전이거나 설정되지 않았습니다. 재스케줄링을 진행하지 않습니다.",
                    plan.getId(), endDate, today);
                throw new ApiException(ExceptionType.PLAN_END_DATE_PASSED,
                    "계획의 종료일이 이미 지났습니다. 종료일을 업데이트한 후 다시 시도해주세요.");
            }

            List<LocalDate> studyDates = calculateStudyDatesFromDate(plan, today, endDate);

            if (studyDates.isEmpty()) {
                log.warn("계획 {}에 대한 공부 가능 날짜가 없습니다.", plan.getId());
                return new ScheduleGenerationResult(Collections.emptyList(),
                    Collections.emptyList());
            }

            return distributeLessonsByPeriod(plan, lessons, adjustedDurations, studyDates);
        } else {
            // TIME 타입은 오늘부터 studyDays와 dailyTime에 맞춰 배분
            return distributeLessonsByTime(plan, lessons, adjustedDurations, null);
        }
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