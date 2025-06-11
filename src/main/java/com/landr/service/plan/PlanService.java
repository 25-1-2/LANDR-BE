package com.landr.service.plan;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.controller.plan.dto.UpdatePlanRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.Plan;
import com.landr.domain.plan.PlanType;
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
import com.landr.repository.studygroup.StudyGroupMemberRepository;
import com.landr.service.dto.DailyScheduleDto;
import com.landr.service.dto.LessonScheduleDto;
import com.landr.service.dto.PlanDetailResponse;
import com.landr.service.dto.PlanSummaryDto;
import com.landr.service.schedule.ScheduleGeneratorService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ScheduleGeneratorService scheduleGeneratorService;
    private final StudyGroupMemberRepository studyGroupMemberRepository;


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

        Plan savedPlan = planRepository.save(newPlan);

        scheduleGeneratorService.generateSchedules(savedPlan);
        return savedPlan;
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

        Set<Long> studyGroupPlanIds = new HashSet<>(
            studyGroupMemberRepository.findPlanIdsByUserId(userId));
        log.info("studyGroupPlanIds: {}", studyGroupPlanIds);

        return plans.stream()
            .map(plan -> {
                Long completedLessons = lessonScheduleRepository.countCompletedLessonSchedulesByPlanId(
                    plan.getId());

                // 해당 계획이 스터디 그룹의 일부인지 확인
                boolean isStudyGroup = studyGroupPlanIds.contains(plan.getId());
                Long studyGroupId = null;
                if (isStudyGroup) {
                    studyGroupId = studyGroupMemberRepository
                        .findStudyGroupIdByPlanId(plan.getId())
                        .orElse(null);
                }

                return PlanSummaryDto.builder()
                    .planId(plan.getId())
                    .lectureTitle(plan.getLecture().getTitle())
                    .teacher(plan.getLecture().getTeacher())
                    .platform(plan.getLecture().getPlatform())
                    .totalLessons(
                        plan.getEndLesson().getOrder() - plan.getStartLesson().getOrder() + 1)
                    .completedLessons(completedLessons)
                    .isStudyGroup(isStudyGroup)
                    .studyGroupId(studyGroupId)
                    .subject(plan.getLecture().getSubject())
                    .tag(plan.getLecture().getTag())
                    .build();
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public PlanDetailResponse getPlan(Long planId, Long userId) {
        // 해당 계획 조회
        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ApiException(ExceptionType.PLAN_NOT_FOUND));

        // 일별 일정 목록 조회
        List<DailySchedule> dailySchedules = dailyScheduleRepository.findByPlanId(planId);
        log.info("dailySchedules: {}", dailySchedules);

        if (dailySchedules.isEmpty()) {
            return PlanDetailResponse.builder()
                .planId(planId)
                .lectureTitle(plan.getLecture().getTitle())
                .teacher(plan.getLecture().getTeacher())
                .platform(plan.getLecture().getPlatform())
                .planType(plan.getPlanType())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .dailyTime(plan.getDailyTime())
                .playbackSpeed(plan.getPlaybackSpeed())
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
                List<LessonScheduleDto> lsDtos = lessonScheduleMap.getOrDefault(ds.getId(),
                        List.of())
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
            .planType(plan.getPlanType())
            .startDate(plan.getStartDate())
            .endDate(plan.getEndDate())
            .dailyTime(plan.getDailyTime())
            .playbackSpeed(plan.getPlaybackSpeed())
            .dailySchedules(dailyScheduleDtos)
            .build();
    }

    @Transactional
    public void deletePlan(Long planId, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
            .orElseThrow(() -> new ApiException(ExceptionType.PLAN_NOT_FOUND));

        planRepository.delete(plan);
    }

    @Transactional
    public void updatePlan(Long planId, UpdatePlanRequest request, Long userId) {
        Plan plan = planRepository.findByIdAndUserId(planId, userId)
            .orElseThrow(() -> new ApiException(ExceptionType.PLAN_NOT_FOUND));

        if (!request.hasAnyUpdateField()) {
            throw new ApiException(ExceptionType.BAD_REQUEST, "수정할 필드가 없습니다.");
        }

        // 타입별 업데이트 및 검증
        if (plan.getPlanType() == PlanType.PERIOD) {
            if (request.getDailyTime() != null) {
                throw new ApiException(ExceptionType.BAD_REQUEST,
                    "PERIOD 타입 계획은 dailyTime을 수정할 수 없습니다.");
            }
            plan.updateForPeriodType(request.getEndDate(), request.getPlaybackSpeed());
        } else {
            if (request.getEndDate() != null) {
                throw new ApiException(ExceptionType.BAD_REQUEST,
                    "TIME 타입 계획은 endDate를 수정할 수 없습니다.");
            }
            plan.updateForTimeType(request.getDailyTime(), request.getPlaybackSpeed());
        }

        scheduleGeneratorService.rescheduleIncompleteLessons(userId, planId);

        log.info("Plan {} 수정 및 재스케줄링 완료", planId);
    }
}
