
package com.landr.service.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.domain.user.User;
import com.landr.repository.dailyschedule.DailyScheduleRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.dto.UserProgressDto;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private DailyScheduleRepository dailyScheduleRepository;

    @Mock
    private LessonScheduleRepository lessonScheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private User user;
    private Plan plan;
    private Lecture lecture;
    private Lesson lesson1, lesson2;
    private DailySchedule dailySchedule;
    private LessonSchedule lessonSchedule1, lessonSchedule2;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").build();

        lecture = Lecture.builder()
            .id(1L)
            .title("Test Lecture")
            .teacher("Test Teacher")
            .build();

        lesson1 = Lesson.builder()
            .id(1L)
            .lecture(lecture)
            .title("Lesson 1")
            .duration(60)
            .order(1)
            .build();

        lesson2 = Lesson.builder()
            .id(2L)
            .lecture(lecture)
            .title("Lesson 2")
            .duration(90)
            .order(2)
            .build();

        plan = Plan.builder()
            .id(1L)
            .user(user)
            .lecture(lecture)
            .lectureName("Test Lecture")
            .build();

        dailySchedule = DailySchedule.builder()
            .id(1L)
            .plan(plan)
            .date(LocalDate.now())
            .dayOfWeek(DayOfWeek.MON)
            .totalLessons(2)
            .totalDuration(150)
            .build();

        lessonSchedule1 = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule)
            .lesson(lesson1)
            .adjustedDuration(60)
            .displayOrder(1)
            .completed(false)
            .build();

        lessonSchedule2 = LessonSchedule.builder()
            .id(2L)
            .dailySchedule(dailySchedule)
            .lesson(lesson2)
            .adjustedDuration(90)
            .displayOrder(2)
            .completed(true)
            .build();
    }

    @Test
    @DisplayName("특정 날짜의 일일 스케줄 조회 성공")
    void getUserDailySchedules_Success() {
        // Given
        LocalDate targetDate = LocalDate.now();
        when(dailyScheduleRepository.findByUserIdAndDate(user.getId(), targetDate))
            .thenReturn(Arrays.asList(dailySchedule));
        when(lessonScheduleRepository.findByDailyScheduleIdsWithLessonAndLecture(anyList()))
            .thenReturn(Arrays.asList(lessonSchedule1, lessonSchedule2));

        // When
        DailyScheduleWithLessonsDto result = scheduleService.getUserDailySchedules(user.getId(), targetDate);

        // Then
        assertNotNull(result);
        assertEquals(targetDate, result.getDate());
        assertEquals(DayOfWeek.MON, result.getDayOfWeek());
        assertEquals(2, result.getTotalLessons());
        assertEquals(150, result.getTotalDuration());
        assertEquals(2, result.getLessonSchedules().size());
    }

    @Test
    @DisplayName("일일 스케줄이 없는 경우")
    void getUserDailySchedules_Empty() {
        // Given
        LocalDate targetDate = LocalDate.now();
        when(dailyScheduleRepository.findByUserIdAndDate(user.getId(), targetDate))
            .thenReturn(Collections.emptyList());

        // When
        DailyScheduleWithLessonsDto result = scheduleService.getUserDailySchedules(user.getId(), targetDate);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("사용자 진행 상황 조회 성공")
    void getUserProgress_Success() {
        // Given
        when(lessonScheduleRepository.findAllByUserIdGroupedByLecture(user.getId()))
            .thenReturn(Arrays.asList(lessonSchedule1, lessonSchedule2));

        // When
        UserProgressDto result = scheduleService.getUserProgress(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalCompletedLessons()); // lessonSchedule2만 완료
        assertEquals(2, result.getTotalLessons());
        assertEquals(1, result.getLectureProgress().size());

        // 완강되지 않은 강의만 포함되어야 함
        assertTrue(result.getLectureProgress().stream()
            .allMatch(progress -> progress.getCompletedLessons() < progress.getTotalLessons()));
    }

    @Test
    @DisplayName("사용자 진행 상황 조회 - 모든 강의가 완료된 경우")
    void getUserProgress_AllCompleted() {
        // Given
        lessonSchedule1 = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule)
            .lesson(lesson1)
            .adjustedDuration(60)
            .displayOrder(1)
            .completed(true)
            .build();

        when(lessonScheduleRepository.findAllByUserIdGroupedByLecture(user.getId()))
            .thenReturn(Arrays.asList(lessonSchedule1, lessonSchedule2));

        // When
        UserProgressDto result = scheduleService.getUserProgress(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalCompletedLessons());
        assertEquals(2, result.getTotalLessons());
        assertTrue(result.getLectureProgress().isEmpty()); // 완강된 강의는 제외
    }

    @Test
    @DisplayName("사용자 진행 상황 조회 - 레슨이 없는 경우")
    void getUserProgress_NoLessons() {
        // Given
        when(lessonScheduleRepository.findAllByUserIdGroupedByLecture(user.getId()))
            .thenReturn(Collections.emptyList());

        // When
        UserProgressDto result = scheduleService.getUserProgress(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalCompletedLessons());
        assertEquals(0, result.getTotalLessons());
        assertTrue(result.getLectureProgress().isEmpty());
    }
}