package com.landr.service.schedule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.plan.PlanType;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import com.landr.service.schedule.ScheduleGeneratorService.ScheduleGenerationResult;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Arrays;
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
class ScheduleGeneratorServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private DailyScheduleRepository dailyScheduleRepository;
    @Mock
    private LessonScheduleRepository lessonScheduleRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ScheduleGeneratorService scheduleGeneratorService;

    private User user;
    private Plan periodPlan, timePlan;
    private Lecture lecture;
    private List<Lesson> lessons;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").build();

        lecture = Lecture.builder()
            .id(1L)
            .title("Test Lecture")
            .build();

        lessons = Arrays.asList(
            Lesson.builder().id(1L).order(1).duration(60).lecture(lecture).build(),
            Lesson.builder().id(2L).order(2).duration(90).lecture(lecture).build(),
            Lesson.builder().id(3L).order(3).duration(45).lecture(lecture).build()
        );

        Set<DayOfWeek> studyDays = new HashSet<>(Arrays.asList(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI));

        periodPlan = Plan.builder()
            .id(1L)
            .user(user)
            .lecture(lecture)
            .startLesson(lessons.get(0))
            .endLesson(lessons.get(2))
            .planType(PlanType.PERIOD)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .studyDays(studyDays)
            .playbackSpeed(1.0f)
            .build();

        timePlan = Plan.builder()
            .id(2L)
            .user(user)
            .lecture(lecture)
            .startLesson(lessons.get(0))
            .endLesson(lessons.get(2))
            .planType(PlanType.TIME)
            .dailyTime(120)
            .studyDays(studyDays)
            .playbackSpeed(1.5f)
            .build();
    }

    @Test
    @DisplayName("PERIOD 타입 스케줄 생성 성공")
    void generateSchedules_Period_Success() {
        // Given
        when(lessonRepository.findByLectureIdAndOrderBetweenOrderByOrder(
            lecture.getId(), 1, 3))
            .thenReturn(lessons);
        when(dailyScheduleRepository.saveAll(anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(lessonScheduleRepository.saveAll(anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ScheduleGenerationResult result = scheduleGeneratorService.generateSchedules(periodPlan);

        // Then
        assertNotNull(result);
        assertFalse(result.getDailySchedules().isEmpty());
        assertFalse(result.getLessonSchedules().isEmpty());
        assertEquals(lessons.size(), result.getLessonSchedules().size());
    }

    @Test
    @DisplayName("강의가 없는 경우 빈 결과 반환")
    void generateSchedules_NoLessons() {
        // Given
        when(lessonRepository.findByLectureIdAndOrderBetweenOrderByOrder(
            anyLong(), anyInt(), anyInt()))
            .thenReturn(Arrays.asList());

        // When
        ScheduleGenerationResult result = scheduleGeneratorService.generateSchedules(periodPlan);

        // Then
        assertNotNull(result);
        assertTrue(result.getDailySchedules().isEmpty());
        assertTrue(result.getLessonSchedules().isEmpty());
    }


    @Test
    @DisplayName("재스케줄링 실패 - 종료일이 지난 경우")
    void rescheduleIncompleteLessons_EndDatePassed() {
        // Given
        Plan expiredPlan = Plan.builder()
            .id(3L)
            .user(user)
            .planType(PlanType.PERIOD)
            .endDate(LocalDate.now().minusDays(1))
            .studyDays(new HashSet<>())
            .build();

        when(planRepository.findByIdAndUserId(expiredPlan.getId(), user.getId()))
            .thenReturn(Optional.of(expiredPlan));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> scheduleGeneratorService.rescheduleIncompleteLessons(user.getId(), expiredPlan.getId()));

        assertEquals(ExceptionType.PLAN_END_DATE_PASSED, exception.getExceptionType());
    }
}