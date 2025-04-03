package com.landr.service.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.plan.PlanType;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.plan.PlanRepository;
import java.time.LocalDate;
import java.util.HashSet;
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

    @InjectMocks
    private PlanService planService;

    private Plan plan;
    private EditLectureNameRequest request;
    private final Long planId = 1L;
    private final Long userId = 1L;

    // createPlan 테스트를 위한 변수 추가
    private User user;
    private Lecture lecture;
    private Lesson startLesson;
    private Lesson endLesson;
    private CreatePlanRequest createPlanRequest;
    private Set<DayOfWeek> studyDays;

    @BeforeEach
    void setUp() {
        plan = new Plan();

        // 테스트 데이터 설정
        request = EditLectureNameRequest.builder()
            .lectureAlias("수학")
            .build();

        // createPlan 테스트를 위한 데이터 설정
        user = new User();
        lecture = new Lecture();
//        lecture.("테스트 강의");
        startLesson = new Lesson();
        endLesson = new Lesson();

        studyDays = new HashSet<>();
        studyDays.add(DayOfWeek.MON);
        studyDays.add(DayOfWeek.WED);
        studyDays.add(DayOfWeek.FRI);
    }

    @Test
    @DisplayName("강의 이름 수정 성공")
    void editLectureName_Success() {
        // Given
        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));

        // When
        planService.editLectureName(request, planId, userId);

        // Then
        verify(planRepository, times(1)).findByIdAndUserId(planId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 계획 ID로 강의 이름 수정 시도")
    void editLectureName_PlanNotFound() {
        // Given
        when(planRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> planService.editLectureName(request, planId, userId));

        assertEquals(ExceptionType.PLAN_NOT_FOUND, exception.getExceptionType());
        verify(planRepository, times(1)).findByIdAndUserId(planId, userId);
    }

    @Test
    @DisplayName("계획 생성 성공 - PERIOD 타입")
    void createPlan_Success_Period() {
        // Given
        createPlanRequest = CreatePlanRequest.builder()
            .lectureId(1L)
            .planType(PlanType.PERIOD)
            .startLessonId(1L)
            .endLessonId(2L)
            .studyDayOfWeeks(studyDays)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .playbackSpeed(1.5f)
            .build();

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(startLesson));
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(endLesson));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> {
            Plan savedPlan = invocation.getArgument(0);
            return savedPlan;
        });

        // When
        Plan result = planService.createPlan(createPlanRequest, user);

        // Then
        assertEquals(lecture, result.getLecture());
        assertEquals(lecture.getTitle(), result.getLectureName());
        assertEquals(user, result.getUser());
        assertEquals(PlanType.PERIOD, result.getPlanType());
        assertEquals(startLesson, result.getStartLesson());
        assertEquals(endLesson, result.getEndLesson());
        assertEquals(studyDays, result.getStudyDays());
        assertEquals(LocalDate.now(), result.getStartDate());
        assertEquals(LocalDate.now().plusDays(30), result.getEndDate());
        assertEquals(1.5f, result.getPlaybackSpeed());
    }

    @Test
    @DisplayName("계획 생성 성공 - TIME 타입")
    void createPlan_Success_Time() {
        // Given
        createPlanRequest = CreatePlanRequest.builder()
            .lectureId(1L)
            .planType(PlanType.TIME)
            .startLessonId(1L)
            .endLessonId(2L)
            .studyDayOfWeeks(studyDays)
            .dailyTime(120)
            .playbackSpeed(1.0f)
            .build();

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture));
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(startLesson));
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(endLesson));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> {
            Plan savedPlan = invocation.getArgument(0);
            return savedPlan;
        });

        // When
        Plan result = planService.createPlan(createPlanRequest, user);

        // Then
        assertEquals(lecture, result.getLecture());
        assertEquals(PlanType.TIME, result.getPlanType());
        assertEquals(120, result.getDailyTime());
        assertEquals(startLesson, result.getStartLesson());
        assertEquals(endLesson, result.getEndLesson());
    }

    @Test
    @DisplayName("계획 생성 실패 - 강의를 찾을 수 없음")
    void createPlan_Failure_LectureNotFound() {
        // Given
        createPlanRequest = CreatePlanRequest.builder()
            .lectureId(1L)
            .planType(PlanType.PERIOD)
            .startLessonId(1L)
            .endLessonId(2L)
            .studyDayOfWeeks(studyDays)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .playbackSpeed(1.5f)
            .build();

        when(lectureRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
            planService.createPlan(createPlanRequest, user));
        assertEquals(ExceptionType.LECTURE_NOT_FOUND, exception.getExceptionType());
    }

    @Test
    @DisplayName("계획 생성 실패 - 시작 레슨을 찾을 수 없음")
    void createPlan_Failure_StartLessonNotFound() {
        // Given
        createPlanRequest = CreatePlanRequest.builder()
            .lectureId(1L)
            .planType(PlanType.PERIOD)
            .startLessonId(1L)
            .endLessonId(2L)
            .studyDayOfWeeks(studyDays)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .playbackSpeed(1.5f)
            .build();

        when(lectureRepository.findById(anyLong())).thenReturn(Optional.of(lecture));
        when(lessonRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () ->
            planService.createPlan(createPlanRequest, user));
        assertEquals(ExceptionType.LESSON_NOT_FOUND, exception.getExceptionType());
    }


}