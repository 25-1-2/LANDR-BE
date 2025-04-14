package com.landr.service.plan;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import com.landr.service.dto.PlanSummaryDto;
import java.util.List;
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
    public List<LessonSchedule> getPlan(Long planId, Long userId) {
        List<LessonSchedule> lessonScheduleList = lessonScheduleRepository.findByPlanIdAndUserId(
            userId, planId);

        return lessonScheduleList;
    }
}
