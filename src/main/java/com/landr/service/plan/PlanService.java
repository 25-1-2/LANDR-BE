package com.landr.service.plan;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import com.landr.service.dto.DailyScheduleDto;
import com.landr.service.dto.LessonScheduleDto;
import com.landr.service.dto.PlanDetailResponse;
import com.landr.service.dto.PlanSummaryDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final LectureRepository lectureRepository;
    private final LessonRepository lessonRepository;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final DailyScheduleRepository dailyScheduleRepository;

    @Transactional
    public String editLectureName(EditLectureNameRequest req, Long planId, Long memberId) {
        Plan plan = planRepository.findByIdAndUserId(planId, memberId)
            .orElseThrow(() -> new ApiException(
                ExceptionType.PLAN_NOT_FOUND));

        plan.editLectureName(req.getLectureAlias());

        return plan.getLectureName();
    }

    @Transactional
    public Plan createPlan(CreatePlanRequest req, User user) {
        Lecture lecture = lectureRepository.findById(req.getLectureId())
            .orElseThrow(() -> new ApiException(ExceptionType.LECTURE_NOT_FOUND));

        Lesson startLesson = lessonRepository.findById(req.getStartLessonId())
            .orElseThrow(() -> new ApiException(ExceptionType.LESSON_NOT_FOUND));

        Lesson endLesson = lessonRepository.findById(req.getEndLessonId())
            .orElseThrow(() -> new ApiException(ExceptionType.LESSON_NOT_FOUND));

        Plan newPlan = Plan.builder()
            .lecture(lecture)
            .lectureName(lecture.getTitle())
            .user(user)
            .planType(req.getPlanType())
            .startLesson(startLesson)
            .endLesson(endLesson)
            .studyDays(req.getStudyDayOfWeeks())
            .dailyTime(req.getDailyTime())
            .startDate(req.getStartDate())
            .endDate(req.getEndDate())
            .playbackSpeed(req.getPlaybackSpeed())
            .build();

        return planRepository.save(newPlan);
    }

    @Transactional(readOnly = true)
    public List<PlanSummaryDto> getMyPlans(Long userId) {

        // 사용자의 모든 Plan 조회
        List<Plan> plans = planRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAt(userId);
        log.info("plans: {}", plans);

        // 사용자의 모든 Plan이 없으면 빈 리스트 반환
        if (plans.isEmpty()) {
            return List.of();
        }

        return plans.stream()
            .map(plan -> {
                Long completedLessons = lessonScheduleRepository.countCompletedLessonSchedulesByPlanId(
                    plan.getId());
                return PlanSummaryDto.builder()
                    .planId(plan.getId())
                    .lectureTitle(plan.getLecture().getTitle())
                    .teacher(plan.getLecture().getTeacher())
                    .platform(plan.getLecture().getPlatform())
                    .totalLessons(
                        plan.getEndLesson().getOrder() - plan.getStartLesson().getOrder() + 1)
                    .completedLessons(completedLessons)
                    .build();
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public PlanDetailResponse getPlan(Long planId, Long userId) {
        // 해당 계획 조회
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
            .orElseThrow(() -> new ApiException(ExceptionType.PLAN_NOT_FOUND));

        // 일별 일정 목록 조회
        List<DailySchedule> dailySchedules = dailyScheduleRepository.findByUserIdAndPlanId(userId, planId);
        log.info("dailySchedules: {}", dailySchedules);

        if (dailySchedules.isEmpty()) {
            return PlanDetailResponse.builder()
                .planId(planId)
                .lectureTitle(plan.getLecture().getTitle())
                .teacher(plan.getLecture().getTeacher())
                .platform(plan.getLecture().getPlatform())
                .dailySchedules(List.of())
                .build();
        }

        // 일별 일정 ID 목록 추출
        List<Long> dailyScheduleIds = dailySchedules.stream()
            .map(DailySchedule::getId)
            .collect(Collectors.toList());

        // 관련된 모든 레슨 일정 조회
        List<LessonSchedule> lessonSchedules = lessonScheduleRepository
            .findByDailyScheduleIdsWithLessonAndLecture(dailyScheduleIds);

        // 레슨 일정을 일별 일정별로 그룹화
        Map<Long, List<LessonSchedule>> lessonScheduleMap = lessonSchedules.stream()
            .collect(Collectors.groupingBy(ls -> ls.getDailySchedule().getId()));

        // 일별 일정 DTO 생성
        List<DailyScheduleDto> dailyScheduleDtos = dailySchedules.stream()
            .map(ds -> {
                List<LessonScheduleDto> lsDtos = lessonScheduleMap.getOrDefault(ds.getId(), List.of())
                    .stream()
                    .map(LessonScheduleDto::convert)
                    .collect(Collectors.toList());

                return DailyScheduleDto.builder()
                    .date(ds.getDate())
                    .dayOfWeek(ds.getDayOfWeek())
                    .lessonSchedules(lsDtos)
                    .build();
            })
            .collect(Collectors.toList());

        // 최종 응답 생성
        return PlanDetailResponse.builder()
            .planId(planId)
            .lectureTitle(plan.getLecture().getTitle())
            .teacher(plan.getLecture().getTeacher())
            .platform(plan.getLecture().getPlatform())
            .dailySchedules(dailyScheduleDtos)
            .build();
    }
}
