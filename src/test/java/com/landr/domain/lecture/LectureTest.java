// src/test/java/com/landr/domain/lecture/LectureTest.java
package com.landr.domain.lecture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LectureTest {

    private Lecture lecture;
    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() {
        createdAt = LocalDateTime.of(2025, 6, 11, 10, 0);

        lecture = Lecture.builder()
            .id(1L)
            .title("미적분 기초")
            .teacher("김수학")
            .platform(Platform.MEGA)
            .subject(Subject.MATH)
            .totalLessons(30)
            .totalDuration(1800) // 30시간
            .createdAt(createdAt)
            .tag("기초,미적분,수학")
            .build();
    }

    @Test
    @DisplayName("Lecture 생성 테스트")
    void createLecture_Success() {
        // Then
        assertEquals(1L, lecture.getId());
        assertEquals("미적분 기초", lecture.getTitle());
        assertEquals("김수학", lecture.getTeacher());
        assertEquals(Platform.MEGA, lecture.getPlatform());
        assertEquals(Subject.MATH, lecture.getSubject());
        assertEquals(30, lecture.getTotalLessons());
        assertEquals(1800, lecture.getTotalDuration());
        assertEquals(createdAt, lecture.getCreatedAt());
        assertEquals("기초,미적분,수학", lecture.getTag());
    }

    @Test
    @DisplayName("Lecture 모든 Platform 테스트")
    void testAllPlatforms() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            Lecture.builder().platform(Platform.MEGA).build();
            Lecture.builder().platform(Platform.DAESANG).build();
            Lecture.builder().platform(Platform.ETOOS).build();
            Lecture.builder().platform(Platform.EBSI).build();
        });
    }

    @Test
    @DisplayName("Lecture 모든 Subject 테스트")
    void testAllSubjects() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            Lecture.builder().subject(Subject.KOR).build();
            Lecture.builder().subject(Subject.ENG).build();
            Lecture.builder().subject(Subject.MATH).build();
            Lecture.builder().subject(Subject.SOC).build();
            Lecture.builder().subject(Subject.SCI).build();
            Lecture.builder().subject(Subject.HIST).build();
            Lecture.builder().subject(Subject.UNIV).build();
            Lecture.builder().subject(Subject.LANG2).build();
            Lecture.builder().subject(Subject.VOC).build();
        });
    }

    @Test
    @DisplayName("Lecture tag가 없는 경우")
    void createLecture_WithoutTag() {
        // Given & When
        Lecture lectureWithoutTag = Lecture.builder()
            .id(2L)
            .title("영어 문법")
            .teacher("이영어")
            .platform(Platform.ETOOS)
            .subject(Subject.ENG)
            .totalLessons(20)
            .totalDuration(1200)
            .createdAt(LocalDateTime.now())
            .build();

        // Then
        assertNull(lectureWithoutTag.getTag());
        assertEquals("영어 문법", lectureWithoutTag.getTitle());
    }

    @Test
    @DisplayName("Lecture 최소 필수 필드만으로 생성")
    void createLecture_MinimalFields() {
        // Given & When
        Lecture minimalLecture = Lecture.builder()
            .id(3L)
            .title("국어 독해")
            .teacher("박국어")
            .platform(Platform.EBSI)
            .subject(Subject.KOR)
            .totalLessons(15)
            .totalDuration(900)
            .createdAt(LocalDateTime.now())
            .build();

        // Then
        assertNotNull(minimalLecture);
        assertEquals(3L, minimalLecture.getId());
        assertEquals("국어 독해", minimalLecture.getTitle());
        assertEquals("박국어", minimalLecture.getTeacher());
        assertEquals(Platform.EBSI, minimalLecture.getPlatform());
        assertEquals(Subject.KOR, minimalLecture.getSubject());
        assertEquals(15, minimalLecture.getTotalLessons());
        assertEquals(900, minimalLecture.getTotalDuration());
    }

    @Test
    @DisplayName("Platform enum 값 확인")
    void testPlatformValues() {
        // Given & When & Then
        assertEquals("MEGA", Platform.MEGA.name());
        assertEquals("DAESANG", Platform.DAESANG.name());
        assertEquals("ETOOS", Platform.ETOOS.name());
        assertEquals("EBSI", Platform.EBSI.name());
        assertEquals(4, Platform.values().length);
    }

    @Test
    @DisplayName("Subject enum 값 확인")
    void testSubjectValues() {
        // Given & When & Then
        assertEquals("KOR", Subject.KOR.name());
        assertEquals("ENG", Subject.ENG.name());
        assertEquals("MATH", Subject.MATH.name());
        assertEquals("SOC", Subject.SOC.name());
        assertEquals("SCI", Subject.SCI.name());
        assertEquals("HIST", Subject.HIST.name());
        assertEquals("UNIV", Subject.UNIV.name());
        assertEquals("LANG2", Subject.LANG2.name());
        assertEquals("VOC", Subject.VOC.name());
        assertEquals(9, Subject.values().length);
    }
}