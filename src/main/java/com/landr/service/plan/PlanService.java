package com.landr.service.plan;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.Plan;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.plan.PlanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final LectureRepository lectureRepository;
    private final LessonRepository lessonRepository;

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
}
