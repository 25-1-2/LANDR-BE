package com.landr.service.plan;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.lecture.Platform;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.plan.PlanType;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import com.landr.repository.studygroup.StudyGroupMemberRepository;
import com.landr.service.dto.PlanDetailResponse;
import com.landr.service.dto.PlanSummaryDto;
import com.landr.service.schedule.ScheduleGeneratorService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonScheduleRepository lessonScheduleRepository;
    @Mock
    private DailyScheduleRepository dailyScheduleRepository;
    @Mock
    private ScheduleGeneratorService scheduleGeneratorService;
    @Mock
    private StudyGroupMemberRepository studyGroupMemberRepository;

    @InjectMocks
    private PlanService planService;

    private User user;
    private Plan plan;
    private Lecture lecture;
    private Lesson startLesson, endLesson;
    private CreatePlanRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").build();

        lecture = Lecture.builder()
            .id(1L)
            .title("Test Lecture")
            .teacher("Test Teacher")
            .platform(Platform.MEGA)
            .build();

        startLesson = Lesson.builder().id(1L).order(1).build();
        endLesson = Lesson.builder().id(10L).order(10).build();

        Set<DayOfWeek> studyDays = new HashSet<>(Arrays.asList(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI));

        plan = Plan.builder()
            .id(1L)
            .user(user)
            .lecture(lecture)
            .lectureName("Test Lecture")
            .startLesson(startLesson)
            .endLesson(endLesson)
            .studyDays(studyDays)
            .isDeleted(false)
            .build();
    }

    @Test
    @DisplayName("강의 이름 수정 성공")
    void editLectureName_Success() {
        // Given
        EditLectureNameRequest request = EditLectureNameRequest.builder()
            .lectureAlias("수학 기초")
            .build();

        when(planRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));

        // When
        String result = planService.editLectureName(request, 1L, 1L);

        // Then
        assertEquals("수학 기초", result);
        assertEquals("수학 기초", plan.getLectureName());
    }

    @Test
    @DisplayName("계획 생성 성공")
    void createPlan_Success() {
        // Given
        Set<DayOfWeek> studyDays = new HashSet<>(Arrays.asList(DayOfWeek.MON, DayOfWeek.WED));
        createRequest = CreatePlanRequest.builder()
            .lectureId(1L)
            .planType(PlanType.PERIOD)
            .startLessonId(1L)
            .endLessonId(10L)
            .studyDayOfWeeks(studyDays)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .playbackSpeed(1.5f)
            .build();

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(startLesson));
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(endLesson));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Plan result = planService.createPlan(createRequest, user);

        // Then
        assertNotNull(result);
        assertEquals(lecture, result.getLecture());
        assertEquals(user, result.getUser());
        verify(scheduleGeneratorService, times(1)).generateSchedules(any(Plan.class));
    }

    @Test
    @DisplayName("나의 계획 목록 조회 성공")
    void getMyPlans_Success() {
        // Given
        when(planRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAt(user.getId()))
            .thenReturn(Arrays.asList(plan));
        when(lessonScheduleRepository.countCompletedLessonSchedulesByPlanId(plan.getId()))
            .thenReturn(5L);
        when(studyGroupMemberRepository.findPlanIdsByUserId(user.getId()))
            .thenReturn(Collections.emptyList());

        // When
        List<PlanSummaryDto> result = planService.getMyPlans(user.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals(plan.getId(), result.get(0).getPlanId());
        assertEquals(5L, result.get(0).getCompletedLessons());
        assertFalse(result.get(0).isStudyGroup());
    }

    @Test
    @DisplayName("계획 상세 조회 성공")
    void getPlan_Success() {
        // Given
        DailySchedule dailySchedule = DailySchedule.builder()
            .id(1L)
            .plan(plan)
            .date(LocalDate.now())
            .totalLessons(2)
            .totalDuration(120)
            .build();

        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(dailyScheduleRepository.findByPlanId(plan.getId()))
            .thenReturn(Arrays.asList(dailySchedule));
        when(lessonScheduleRepository.findByDailyScheduleIdsWithLessonAndLecture(anyList()))
            .thenReturn(Collections.emptyList());

        // When
        PlanDetailResponse result = planService.getPlan(plan.getId(), user.getId());

        // Then
        assertNotNull(result);
        assertEquals(plan.getId(), result.getPlanId());
        assertEquals(lecture.getTitle(), result.getLectureTitle());
        assertEquals(1, result.getDailySchedules().size());
    }

    @Test
    @DisplayName("계획 삭제 성공")
    void deletePlan_Success() {
        // Given
        when(planRepository.findByIdAndUserId(plan.getId(), user.getId()))
            .thenReturn(Optional.of(plan));

        // When
        assertDoesNotThrow(() -> planService.deletePlan(plan.getId(), user.getId()));

        // Then
        verify(planRepository, times(1)).delete(plan);
    }

    @Test
    @DisplayName("계획 삭제 실패 - 계획을 찾을 수 없음")
    void deletePlan_NotFound() {
        // Given
        when(planRepository.findByIdAndUserId(anyLong(), anyLong()))
            .thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> planService.deletePlan(999L, user.getId()));

        assertEquals(ExceptionType.PLAN_NOT_FOUND, exception.getExceptionType());
    }
}