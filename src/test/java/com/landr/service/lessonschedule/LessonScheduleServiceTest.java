package com.landr.service.lessonschedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonScheduleServiceTest {

    @Mock
    private LessonScheduleRepository lessonScheduleRepository;

    @InjectMocks
    private LessonScheduleService lessonScheduleService;

    private LessonSchedule lessonSchedule;
    private final Long lessonScheduleId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // Create test LessonSchedule
        lessonSchedule = new LessonSchedule();

        // Setup necessary related entities
        DailySchedule dailySchedule = new DailySchedule();
        Plan plan = new Plan();
        Lesson lesson = new Lesson();

        // Set initial state (not completed)
        // This depends on reflection or setter methods for testing purposes
        try {
            java.lang.reflect.Field completedField = LessonSchedule.class.getDeclaredField("completed");
            completedField.setAccessible(true);
            completedField.set(lessonSchedule, false);

            java.lang.reflect.Field dailyScheduleField = LessonSchedule.class.getDeclaredField("dailySchedule");
            dailyScheduleField.setAccessible(true);
            dailyScheduleField.set(lessonSchedule, dailySchedule);

            java.lang.reflect.Field lessonField = LessonSchedule.class.getDeclaredField("lesson");
            lessonField.setAccessible(true);
            lessonField.set(lessonSchedule, lesson);

            java.lang.reflect.Field planField = DailySchedule.class.getDeclaredField("plan");
            planField.setAccessible(true);
            planField.set(dailySchedule, plan);
        } catch (Exception e) {
            // Handle reflection exceptions
        }
    }

    @Test
    @DisplayName("수업 일정 체크 토글 성공 - 미완료 → 완료")
    void toggleCheck_FromFalseToTrue_Success() {
        // Given
        when(lessonScheduleRepository.findByIdAndUserId(lessonScheduleId, userId))
            .thenReturn(Optional.of(lessonSchedule));

        // When
        Boolean result = lessonScheduleService.toggleCheck(lessonScheduleId, userId);

        // Then
        assertTrue(result);
        assertTrue(lessonSchedule.isCompleted());
        verify(lessonScheduleRepository, times(1)).findByIdAndUserId(lessonScheduleId, userId);
    }

    @Test
    @DisplayName("수업 일정 체크 토글 성공 - 완료 → 미완료")
    void toggleCheck_FromTrueToFalse_Success() {
        // Given
        try {
            java.lang.reflect.Field completedField = LessonSchedule.class.getDeclaredField("completed");
            completedField.setAccessible(true);
            completedField.set(lessonSchedule, true);
        } catch (Exception e) {
            // Handle reflection exceptions
        }

        when(lessonScheduleRepository.findByIdAndUserId(lessonScheduleId, userId))
            .thenReturn(Optional.of(lessonSchedule));

        // When
        Boolean result = lessonScheduleService.toggleCheck(lessonScheduleId, userId);

        // Then
        assertFalse(result);
        assertFalse(lessonSchedule.isCompleted());
        verify(lessonScheduleRepository, times(1)).findByIdAndUserId(lessonScheduleId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 수업 일정 ID로 토글 시도")
    void toggleCheck_LessonScheduleNotFound() {
        // Given
        when(lessonScheduleRepository.findByIdAndUserId(anyLong(), anyLong()))
            .thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> lessonScheduleService.toggleCheck(lessonScheduleId, userId));

        assertEquals(ExceptionType.LESSON_SCHEDULE_NOT_FOUND, exception.getExceptionType());
        verify(lessonScheduleRepository, times(1)).findByIdAndUserId(lessonScheduleId, userId);
    }
}