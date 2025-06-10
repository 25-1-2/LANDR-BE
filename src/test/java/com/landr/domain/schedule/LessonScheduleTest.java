package com.landr.domain.schedule;

import static org.junit.jupiter.api.Assertions.*;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LessonScheduleTest {

    private LessonSchedule lessonSchedule;
    private DailySchedule dailySchedule;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        Lecture lecture = Lecture.builder()
            .id(1L)
            .title("Test Lecture")
            .build();

        lesson = Lesson.builder()
            .id(1L)
            .lecture(lecture)
            .title("Lesson 1")
            .duration(60)
            .order(1)
            .build();

        dailySchedule = DailySchedule.builder()
            .id(1L)
            .totalLessons(1)
            .totalDuration(60)
            .build();

        lessonSchedule = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule)
            .lesson(lesson)
            .adjustedDuration(60)
            .displayOrder(1)
            .completed(false)
            .build();
    }

    @Test
    @DisplayName("수업 완료 상태 토글 - false에서 true로")
    void toggleCheck_FalseToTrue() {
        // Given
        assertFalse(lessonSchedule.isCompleted());
        assertNull(lessonSchedule.getUpdatedAt());

        // When
        boolean result = lessonSchedule.toggleCheck();

        // Then
        assertTrue(result);
        assertTrue(lessonSchedule.isCompleted());
        assertNotNull(lessonSchedule.getUpdatedAt());
    }

    @Test
    @DisplayName("수업 완료 상태 토글 - true에서 false로")
    void toggleCheck_TrueToFalse() {
        // Given
        lessonSchedule.toggleCheck(); // true로 변경
        assertTrue(lessonSchedule.isCompleted());
        LocalDateTime firstUpdatedAt = lessonSchedule.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }

        // When
        boolean result = lessonSchedule.toggleCheck();

        // Then
        assertFalse(result);
        assertFalse(lessonSchedule.isCompleted());
        assertNotNull(lessonSchedule.getUpdatedAt());
        assertNotEquals(firstUpdatedAt, lessonSchedule.getUpdatedAt());
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void toStringTest() {
        // When
        String result = lessonSchedule.toString();

        // Then
        assertTrue(result.contains("LessonSchedule"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("adjustedDuration=60"));
        assertTrue(result.contains("displayOrder=1"));
        assertTrue(result.contains("completed=false"));
    }
}