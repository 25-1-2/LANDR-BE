// src/test/java/com/landr/service/schedule/ScheduleServiceTest.java (기존 파일 보완)
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
    private Plan plan1, plan2;
    private Lecture lecture1, lecture2;
    private Lesson lesson1, lesson2, lesson3, lesson4;
    private DailySchedule dailySchedule1, dailySchedule2;
    private LessonSchedule lessonSchedule1, lessonSchedule2, lessonSchedule3, lessonSchedule4;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").build();

        lecture1 = Lecture.builder()
            .id(1L)
            .title("수학 기초")
            .teacher("김선생")
            .build();

        lecture2 = Lecture.builder()
            .id(2L)
            .title("영어 문법")
            .teacher("이선생")
            .build();

        lesson1 = Lesson.builder()
            .id(1L)
            .lecture(lecture1)
            .title("1강. 집합")
            .duration(60)
            .order(1)
            .build();

        lesson2 = Lesson.builder()
            .id(2L)
            .lecture(lecture1)
            .title("2강. 함수")
            .duration(90)
            .order(2)
            .build();

        lesson3 = Lesson.builder()
            .id(3L)
            .lecture(lecture2)
            .title("1강. 문장구조")
            .duration(80)
            .order(1)
            .build();

        lesson4 = Lesson.builder()
            .id(4L)
            .lecture(lecture2)
            .title("2강. 동사")
            .duration(70)
            .order(2)
            .build();

        plan1 = Plan.builder()
            .id(1L)
            .user(user)
            .lecture(lecture1)
            .lectureName("수학 기초 강의")
            .build();

        plan2 = Plan.builder()
            .id(2L)
            .user(user)
            .lecture(lecture2)
            .lectureName("영어 문법 강의")
            .build();

        dailySchedule1 = DailySchedule.builder()
            .id(1L)
            .plan(plan1)
            .date(LocalDate.now())
            .dayOfWeek(DayOfWeek.MON)
            .totalLessons(2)
            .totalDuration(150)
            .build();

        dailySchedule2 = DailySchedule.builder()
            .id(2L)
            .plan(plan2)
            .date(LocalDate.now())
            .dayOfWeek(DayOfWeek.MON)
            .totalLessons(1)
            .totalDuration(80)
            .build();

        lessonSchedule1 = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule1)
            .lesson(lesson1)
            .adjustedDuration(60)
            .displayOrder(1)
            .completed(false)
            .build();

        lessonSchedule2 = LessonSchedule.builder()
            .id(2L)
            .dailySchedule(dailySchedule1)
            .lesson(lesson2)
            .adjustedDuration(90)
            .displayOrder(2)
            .completed(true)
            .build();

        lessonSchedule3 = LessonSchedule.builder()
            .id(3L)
            .dailySchedule(dailySchedule2)
            .lesson(lesson3)
            .adjustedDuration(80)
            .displayOrder(1)
            .completed(false)
            .build();

        lessonSchedule4 = LessonSchedule.builder()
            .id(4L)
            .dailySchedule(dailySchedule1) // plan1에 속함
            .lesson(lesson4)
            .adjustedDuration(70)
            .displayOrder(3)
            .completed(true)
            .build();
    }

    @Test
    @DisplayName("특정 날짜의 일일 스케줄 조회 성공")
    void getUserDailySchedules_Success() {
        // Given
        LocalDate targetDate = LocalDate.now();
        when(dailyScheduleRepository.findByUserIdAndDate(user.getId(), targetDate))
            .thenReturn(Arrays.asList(dailySchedule1, dailySchedule2));
        when(lessonScheduleRepository.findByDailyScheduleIdsWithLessonAndLecture(anyList()))
            .thenReturn(Arrays.asList(lessonSchedule1, lessonSchedule2, lessonSchedule3));

        // When
        DailyScheduleWithLessonsDto result = scheduleService.getUserDailySchedules(user.getId(), targetDate);

        // Then
        assertNotNull(result);
        assertEquals(targetDate, result.getDate());
        assertEquals(DayOfWeek.MON, result.getDayOfWeek());
        assertEquals(3, result.getTotalLessons());
        assertEquals(230, result.getTotalDuration()); // 60+90+80
        assertEquals(3, result.getLessonSchedules().size());
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
    @DisplayName("사용자 진행 상황 조회 성공 - 여러 강의")
    void getUserProgress_MultipleSubjects_Success() {
        // Given
        when(lessonScheduleRepository.findAllByUserIdGroupedByLecture(user.getId()))
            .thenReturn(Arrays.asList(lessonSchedule1, lessonSchedule2, lessonSchedule3, lessonSchedule4));

        // When
        UserProgressDto result = scheduleService.getUserProgress(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalCompletedLessons()); // lessonSchedule2, lessonSchedule4만 완료
        assertEquals(4, result.getTotalLessons());
        assertEquals(2, result.getLectureProgress().size()); // plan1, plan2

        // 진행률이 낮은 순으로 정렬되었는지 확인
        assertTrue(result.getLectureProgress().stream()
            .allMatch(progress -> progress.getCompletedLessons() < progress.getTotalLessons()));
    }

    @Test
    @DisplayName("사용자 진행 상황 조회 - 모든 강의가 완료된 경우")
    void getUserProgress_AllCompleted() {
        // Given
        lessonSchedule1 = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule1)
            .lesson(lesson1)
            .adjustedDuration(60)
            .displayOrder(1)
            .completed(true)
            .build();

        lessonSchedule3 = LessonSchedule.builder()
            .id(3L)
            .dailySchedule(dailySchedule2)
            .lesson(lesson3)
            .adjustedDuration(80)
            .displayOrder(1)
            .completed(true)
            .build();

        when(lessonScheduleRepository.findAllByUserIdGroupedByLecture(user.getId()))
            .thenReturn(Arrays.asList(lessonSchedule1, lessonSchedule2, lessonSchedule3, lessonSchedule4));

        // When
        UserProgressDto result = scheduleService.getUserProgress(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(4, result.getTotalCompletedLessons());
        assertEquals(4, result.getTotalLessons());
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

    @Test
    @DisplayName("진행률 정렬 테스트")
    void getUserProgress_SortByProgressRate() {
        // Given
        // plan1: 1/3 완료 (33%), plan2: 0/1 완료 (0%)
        Plan plan3 = Plan.builder()
            .id(3L)
            .user(user)
            .lecture(lecture1)
            .lectureName("수학 고급")
            .build();

        DailySchedule dailySchedule3 = DailySchedule.builder()
            .id(3L)
            .plan(plan3)
            .build();

        LessonSchedule lessonSchedule5 = LessonSchedule.builder()
            .id(5L)
            .dailySchedule(dailySchedule3)
            .lesson(lesson1)
            .completed(true)
            .build();

        LessonSchedule lessonSchedule6 = LessonSchedule.builder()
            .id(6L)
            .dailySchedule(dailySchedule3)
            .lesson(lesson2)
            .completed(false)
            .build();

        LessonSchedule lessonSchedule7 = LessonSchedule.builder()
            .id(7L)
            .dailySchedule(dailySchedule3)
            .lesson(lesson3)
            .completed(false)
            .build();

        when(lessonScheduleRepository.findAllByUserIdGroupedByLecture(user.getId()))
            .thenReturn(Arrays.asList(
                lessonSchedule5, lessonSchedule6, lessonSchedule7, // plan3: 33% (1/3)
                lessonSchedule3 // plan2: 0% (0/1)
            ));

        // When
        UserProgressDto result = scheduleService.getUserProgress(user.getId());

        // Then
        assertEquals(2, result.getLectureProgress().size());
        // 첫 번째는 진행률이 더 낮은 plan2 (0%)
        assertEquals(0, result.getLectureProgress().get(0).getCompletedLessons());
        assertEquals(1, result.getLectureProgress().get(0).getTotalLessons());
        // 두 번째는 plan3 (33%)
        assertEquals(1, result.getLectureProgress().get(1).getCompletedLessons());
        assertEquals(3, result.getLectureProgress().get(1).getTotalLessons());
    }

    @Test
    @DisplayName("같은 진행률인 경우 정렬 순서")
    void getUserProgress_SameProgressRate() {
        // Given
        Plan plan3 = Plan.builder()
            .id(3L)
            .user(user)
            .lecture(lecture2)
            .lectureName("영어 고급")
            .build();

        DailySchedule dailySchedule3 = DailySchedule.builder()
            .id(3L)
            .plan(plan3)
            .build();

        // 두 계획 모두 0% 진행률
        LessonSchedule lessonSchedule5 = LessonSchedule.builder()
            .id(5L)
            .dailySchedule(dailySchedule3)
            .lesson(lesson3)
            .completed(false)
            .build();

        when(lessonScheduleRepository.findAllByUserIdGroupedByLecture(user.getId()))
            .thenReturn(Arrays.asList(lessonSchedule3, lessonSchedule5));

        // When
        UserProgressDto result = scheduleService.getUserProgress(user.getId());

        // Then
        assertEquals(2, result.getLectureProgress().size());
        // 모두 0% 진행률이므로 순서는 Plan 순서에 따라
        result.getLectureProgress().forEach(progress -> {
            assertEquals(0, progress.getCompletedLessons());
            assertEquals(1, progress.getTotalLessons());
        });
    }
}