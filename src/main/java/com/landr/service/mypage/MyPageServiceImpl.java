package com.landr.service.mypage;

import com.landr.domain.lecture.Subject;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.domain.user.User;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import com.landr.service.dto.CompletedPlanDto;
import com.landr.service.mypage.dto.MyPage;
import com.landr.service.mypage.dto.MyPageStatistics;
import com.landr.service.mypage.dto.SubjectAchievementDto;
import com.landr.service.mypage.dto.SubjectTimeDto;
import com.landr.service.mypage.dto.WeeklyTimeDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    private final PlanRepository planRepository;
    private final LessonScheduleRepository lessonScheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public MyPage getMyPageInfo(User user) {
        // 사용자가 가진 모든 계획 조회
        List<Plan> userplanList = planRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAt(
            user.getId());

        // 오늘의 레슨 스케줄 정보 조회
        LocalDate today = LocalDate.now();
        List<LessonSchedule> todayLessonSchedules = lessonScheduleRepository.findTodayLessonSchedules(
            user.getId(), today);

        int todayTotalLessonCount = todayLessonSchedules.size();
        int todayCompletedLessonCount = (int) todayLessonSchedules.stream()
            .filter(LessonSchedule::isCompleted)
            .count();

        // 사용자가 완료한 계획 리스트
        List<CompletedPlanDto> completedPlans = userplanList.stream()
            .filter(plan -> {
                // 해당 Plan의 모든 LessonSchedule 조회
                List<LessonSchedule> lessonSchedules = lessonScheduleRepository.findByPlanIdAndUserId(
                    user.getId(), plan.getId());

                // LessonSchedule이 없거나, 모든 LessonSchedule이 completed인 경우만 필터링
                return !lessonSchedules.isEmpty() &&
                    lessonSchedules.stream().allMatch(LessonSchedule::isCompleted);
            })
            .map(plan -> CompletedPlanDto.builder()
                .planId(plan.getId())
                .lectureTitle(plan.getLecture().getTitle())
                .teacher(plan.getLecture().getTeacher())
                .platform(plan.getLecture().getPlatform())
                .build())
            .toList();

        // 수강 중인 강의 수 = 전체 계획 수 - 완료한 계획 수
        int inProgressLectureCount = userplanList.size() - completedPlans.size();

        return MyPage.builder()
            .userName(user.getName())
            .todayTotalLessonCount(todayTotalLessonCount)
            .todayCompletedLessonCount(todayCompletedLessonCount)
            .completedLectureCount(completedPlans.size())
            .studyStreak(calculateStudyStreak(user.getId()))
            .inProgressLectureCount(inProgressLectureCount)
            .completedPlanList(completedPlans)
            .subjectAchievementList(calculateSubjectAchievements(user.getId(), userplanList))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MyPageStatistics getMonthlyStatistics(Long userId, YearMonth date) {
        LocalDate startDate = date.atDay(1);
        LocalDate endDate = date.atEndOfMonth();

        // 해당 월의 완료된 lessonSchedule 조회
        List<LessonSchedule> completedLessonScheduleList = lessonScheduleRepository.findCompletedLessonSchedulesInMonth(
            userId, startDate, endDate);

        // 과목별 공부 시간 계산
        Map<Subject, Long> subjectTimeMap = completedLessonScheduleList.stream()
            .collect(Collectors.groupingBy(
                lessonSchedule -> lessonSchedule.getLesson().getLecture().getSubject(),
                Collectors.summingLong(LessonSchedule::getAdjustedDuration)
            ));

        // 총 공부 시간 계산
        long totalMinutes = subjectTimeMap.values().stream().mapToLong(Long::longValue).sum();

        // 과목별 시간 DTO 생성
        List<SubjectTimeDto> subjectTimes = subjectTimeMap.entrySet().stream()
            .map(entry -> SubjectTimeDto.builder()
                .subject(entry.getKey())
                .totalMinutes(entry.getValue())
                .percentage(totalMinutes > 0 ? Math.round((double) entry.getValue() / totalMinutes * 100 * 100) / 100.0 : 0.0)
                .build())
            .sorted((a, b) -> Long.compare(b.getTotalMinutes(), a.getTotalMinutes()))
            .toList();

        // 주차별 공부 시간 계산 (월요일이 한 주의 시작)
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
        Map<Integer, Long> weeklyTimeMap = completedLessonScheduleList.stream()
            .collect(Collectors.groupingBy(
                ls -> ls.getDailySchedule().getDate().get(weekFields.weekOfMonth()),
                Collectors.summingLong(LessonSchedule::getAdjustedDuration)
            ));

        // 해당 월의 모든 주차에 대한 통계 생성 (0분인 주차도 포함)
        int totalWeeksInMonth = date.atEndOfMonth().get(weekFields.weekOfMonth());
        List<WeeklyTimeDto> weeklyTimes = IntStream.rangeClosed(1, totalWeeksInMonth)
            .mapToObj(weekNumber -> WeeklyTimeDto.builder()
                .weekNumber(weekNumber)
                .totalMinutes(weeklyTimeMap.getOrDefault(weekNumber, 0L))
                .build())
            .toList();

        return MyPageStatistics.builder()
            .date(date)
            .totalStudyMinutes(totalMinutes)
            .subjectTimes(subjectTimes)
            .weeklyTimes(weeklyTimes)
            .build();
    }

    /**
     * 연속 학습일 계산
     */
    private int calculateStudyStreak(Long userId) {
        LocalDate today = LocalDate.now();

        // 오늘로부터 최대 365일 전까지만 확인 (1년)
        return (int) IntStream.rangeClosed(0, 365)
            .takeWhile(day -> {
                // 체크할 날짜 계산
                LocalDate checkDate = today.minusDays(day);

                // 해당 날짜의 시작과 끝 시간 계산
                LocalDateTime startOfDay = checkDate.atStartOfDay();
                LocalDateTime endOfDay = checkDate.plusDays(1).atStartOfDay().minusNanos(1);

                // 해당 날짜에 completed=true로 변경된 LessonSchedule이 있는지 확인
                // 여기서는 Repository에 새로운 메서드를 추가해야 함
                return lessonScheduleRepository.existsCompletedLessonOnDate(userId, startOfDay, endOfDay);
            })
            .count();  // takeWhile 조건을 만족하는 날의 수를 계산
    }

    /**
     * 과목별 성취율 계산
     */
    private List<SubjectAchievementDto> calculateSubjectAchievements(Long userId, List<Plan> plans) {
        // 1. Plan을 과목별로 그룹화
        return plans.stream()
            .collect(Collectors.groupingBy(plan -> plan.getLecture().getSubject()))
            .entrySet().stream()
            .map(entry -> {
                Subject subject = entry.getKey();
                List<Plan> subjectPlans = entry.getValue();

                // 2. 과목별 시작일, 종료일 계산
                LocalDate startDate = subjectPlans.stream()
                    .map(Plan::getStartDate)
                    .filter(Objects::nonNull)
                    .min(LocalDate::compareTo)
                    .orElse(null);

                LocalDate endDate = subjectPlans.stream()
                    .map(Plan::getEndDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(null);

                // 3. 해당 과목의 모든 Plan ID 목록 추출
                List<Long> planIds = subjectPlans.stream()
                    .map(Plan::getId)
                    .toList();

                // 4. 해당 과목의 모든 LessonSchedule 조회
                List<LessonSchedule> allLessonSchedules = planIds.stream()
                    .flatMap(planId -> lessonScheduleRepository.findByPlanIdAndUserId(userId, planId).stream())
                    .toList();

                // 5. 총 강의 수와 완료한 강의 수 계산
                int totalLessons = allLessonSchedules.size();
                int completedLessons = (int) allLessonSchedules.stream()
                    .filter(LessonSchedule::isCompleted)
                    .count();

                // 6. SubjectAchievementDto 생성
                return SubjectAchievementDto.builder()
                    .subject(subject)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalLessons(totalLessons)
                    .completedLessons(completedLessons)
                    .build();
            })
            .collect(Collectors.toList());
    }
}
