// src/test/java/com/landr/domain/lecture/LessonTest.java
package com.landr.domain.lecture;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LessonTest {

    private Lecture lecture;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        lecture = Lecture.builder()
            .id(1L)
            .title("미적분 기초")
            .teacher("김수학")
            .platform(Platform.MEGA)
            .subject(Subject.MATH)
            .build();

        lesson = Lesson.builder()
            .id(1L)
            .lecture(lecture)
            .order(1)
            .title("1강. 극한의 개념")
            .duration(90)
            .build();
    }

    @Test
    @DisplayName("Lesson 생성 테스트")
    void createLesson_Success() {
        // Then
        assertEquals(1L, lesson.getId());
        assertEquals(lecture, lesson.getLecture());
        assertEquals(1, lesson.getOrder());
        assertEquals("1강. 극한의 개념", lesson.getTitle());
        assertEquals(90, lesson.getDuration());
    }

    @Test
    @DisplayName("여러 Lesson 순서 테스트")
    void createMultipleLessons_OrderTest() {
        // Given & When
        Lesson lesson1 = Lesson.builder()
            .id(1L)
            .lecture(lecture)
            .order(1)
            .title("1강. 극한의 개념")
            .duration(90)
            .build();

        Lesson lesson2 = Lesson.builder()
            .id(2L)
            .lecture(lecture)
            .order(2)
            .title("2강. 극한의 계산")
            .duration(85)
            .build();

        Lesson lesson3 = Lesson.builder()
            .id(3L)
            .lecture(lecture)
            .order(3)
            .title("3강. 연속함수")
            .duration(95)
            .build();

        // Then
        assertEquals(1, lesson1.getOrder());
        assertEquals(2, lesson2.getOrder());
        assertEquals(3, lesson3.getOrder());
        assertTrue(lesson1.getOrder() < lesson2.getOrder());
        assertTrue(lesson2.getOrder() < lesson3.getOrder());
    }

    @Test
    @DisplayName("같은 강의의 다른 Lesson들")
    void sameLecture_DifferentLessons() {
        // Given & When
        Lesson anotherLesson = Lesson.builder()
            .id(2L)
            .lecture(lecture)
            .order(2)
            .title("2강. 극한의 계산")
            .duration(85)
            .build();

        // Then
        assertEquals(lecture, lesson.getLecture());
        assertEquals(lecture, anotherLesson.getLecture());
        assertNotEquals(lesson.getId(), anotherLesson.getId());
        assertNotEquals(lesson.getOrder(), anotherLesson.getOrder());
        assertNotEquals(lesson.getTitle(), anotherLesson.getTitle());
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void toStringTest() {
        // When
        String result = lesson.toString();

        // Then
        assertTrue(result.contains("Lesson"));
        assertTrue(result.contains("id='1'"));
        assertTrue(result.contains("title='1강. 극한의 개념'"));
        assertTrue(result.contains("order=1"));
    }

    @Test
    @DisplayName("Lesson duration 다양한 값 테스트")
    void testVariousDurations() {
        // Given & When
        Lesson shortLesson = Lesson.builder()
            .id(10L)
            .lecture(lecture)
            .order(10)
            .title("짧은 강의")
            .duration(30)
            .build();

        Lesson longLesson = Lesson.builder()
            .id(11L)
            .lecture(lecture)
            .order(11)
            .title("긴 강의")
            .duration(180)
            .build();

        // Then
        assertEquals(30, shortLesson.getDuration());
        assertEquals(180, longLesson.getDuration());
        assertTrue(shortLesson.getDuration() < lesson.getDuration());
        assertTrue(longLesson.getDuration() > lesson.getDuration());
    }

    @Test
    @DisplayName("다른 강의의 Lesson")
    void differentLecture_Lesson() {
        // Given
        Lecture anotherLecture = Lecture.builder()
            .id(2L)
            .title("영어 문법")
            .teacher("이영어")
            .platform(Platform.ETOOS)
            .subject(Subject.ENG)
            .build();

        // When
        Lesson lessonFromAnotherLecture = Lesson.builder()
            .id(100L)
            .lecture(anotherLecture)
            .order(1)
            .title("1강. 문장의 구성 요소")
            .duration(60)
            .build();

        // Then
        assertNotEquals(lesson.getLecture().getId(), lessonFromAnotherLecture.getLecture().getId());
        assertEquals(anotherLecture, lessonFromAnotherLecture.getLecture());
        assertEquals(Subject.ENG, lessonFromAnotherLecture.getLecture().getSubject());
    }
}