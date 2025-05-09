package com.landr.service.mypage;

import com.landr.domain.lecture.Subject;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.domain.user.User;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import com.landr.service.dto.PlanSummaryDto;
import com.landr.service.mypage.dto.MyPage;
import com.landr.service.mypage.dto.SubjectAchievementDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        // 사용자가 완료한 계획 리스트
        List<PlanSummaryDto> completedPlans = userplanList.stream()
            .filter(plan -> {
                // 해당 Plan의 모든 LessonSchedule 조회
                List<LessonSchedule> lessonSchedules = lessonScheduleRepository.findByPlanIdAndUserId(
                    user.getId(), plan.getId());

                // LessonSchedule이 없거나, 모든 LessonSchedule이 completed인 경우만 필터링
                return !lessonSchedules.isEmpty() &&
                    lessonSchedules.stream().allMatch(LessonSchedule::isCompleted);
            })
            .map(plan -> PlanSummaryDto.builder()
                .planId(plan.getId())
                .lectureTitle(plan.getLecture().getTitle())
                .teacher(plan.getLecture().getTeacher())
                .platform(plan.getLecture().getPlatform())
                .build())
            .toList();

        // TODO: 목표 날짜 조회

        return MyPage.builder()
            .userName(user.getName())
            .completedLectureCount(completedPlans.size())
            .studyStreak(calculateStudyStreak(user.getId()))
            .goalDate(null)
            .completedPlanList(completedPlans)
            .subjectAchievementList(calculateSubjectAchievements(user.getId(), userplanList))
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
