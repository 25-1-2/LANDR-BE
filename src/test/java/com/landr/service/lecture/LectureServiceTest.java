package com.landr.service.lecture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.landr.controller.lecture.LectureSearchRequest;
import com.landr.controller.lecture.dto.LessonsResponseDto;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.service.dto.lecture.CursorPageResponseDto;
import com.landr.service.dto.lecture.LectureResponseDto;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LectureService lectureService;

    private Lecture lecture1, lecture2, lecture3;
    private Lesson lesson1, lesson2;
    private LectureSearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        lecture1 = Lecture.builder()
            .id(1L)
            .title("수학 기초")
            .teacher("김선생")
            .platform(Platform.MEGA)
            .subject(Subject.MATH)
            .createdAt(now.minusDays(3))
            .tag("기초")
            .totalLessons(20)
            .build();

        lecture2 = Lecture.builder()
            .id(2L)
            .title("영어 문법")
            .teacher("이선생")
            .platform(Platform.ETOOS)
            .subject(Subject.ENG)
            .createdAt(now.minusDays(2))
            .tag("문법")
            .totalLessons(15)
            .build();

        lecture3 = Lecture.builder()
            .id(3L)
            .title("국어 독해")
            .teacher("박선생")
            .platform(Platform.EBSI)
            .subject(Subject.KOR)
            .createdAt(now.minusDays(1))
            .tag("독해")
            .totalLessons(25)
            .build();

        lesson1 = Lesson.builder()
            .id(1L)
            .lecture(lecture1)
            .title("1강. 집합의 개념")
            .order(1)
            .duration(60)
            .build();

        lesson2 = Lesson.builder()
            .id(2L)
            .lecture(lecture1)
            .title("2강. 집합의 연산")
            .order(2)
            .duration(50)
            .build();

        searchRequest = new LectureSearchRequest();
        searchRequest.setOffset(10);
    }

    @Test
    @DisplayName("최신순 강의 목록 조회 성공")
    void getLatestLectures_Success() {
        // Given
        List<Lecture> lectures = Arrays.asList(lecture3, lecture2, lecture1);
        when(lectureRepository.findLatestLecturesWithCursor(any(LectureSearchRequest.class)))
            .thenReturn(lectures);

        // When
        CursorPageResponseDto<LectureResponseDto> result = lectureService.getLatestLectures(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getData().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursor());
    }

    @Test
    @DisplayName("최신순 강의 목록 조회 - 다음 페이지 있음")
    void getLatestLectures_HasNextPage() {
        // Given
        // 11개 반환 (offset + 1)
        List<Lecture> lectures = Arrays.asList(
            lecture3, lecture2, lecture1,
            lecture3, lecture2, lecture1,
            lecture3, lecture2, lecture1,
            lecture3, lecture2 // 11개
        );
        when(lectureRepository.findLatestLecturesWithCursor(any(LectureSearchRequest.class)))
            .thenReturn(lectures);

        // When
        CursorPageResponseDto<LectureResponseDto> result = lectureService.getLatestLectures(searchRequest);

        // Then
        assertEquals(10, result.getData().size()); // offset 만큼만 반환
        assertTrue(result.isHasNext());
        assertNotNull(result.getNextCursor());
        assertNotNull(result.getNextCreatedAt());
    }

    @Test
    @DisplayName("강의 검색 성공")
    void searchLatestLectures_Success() {
        // Given
        searchRequest.setSearch("수학");
        when(lectureRepository.findLatestLecturesBySearch(any(LectureSearchRequest.class)))
            .thenReturn(Arrays.asList(lecture1));

        // When
        CursorPageResponseDto<LectureResponseDto> result = lectureService.searchLatestLectures(searchRequest);

        // Then
        assertEquals(1, result.getData().size());
        assertEquals("수학 기초", result.getData().get(0).getTitle());
    }

    @Test
    @DisplayName("강의 검색 - 결과 없음")
    void searchLatestLectures_NoResults() {
        // Given
        searchRequest.setSearch("물리");
        when(lectureRepository.findLatestLecturesBySearch(any(LectureSearchRequest.class)))
            .thenReturn(Collections.emptyList());

        // When
        CursorPageResponseDto<LectureResponseDto> result = lectureService.searchLatestLectures(searchRequest);

        // Then
        assertTrue(result.getData().isEmpty());
        assertFalse(result.isHasNext());
    }

    @Test
    @DisplayName("강의별 레슨 목록 조회 성공")
    void getLessonsByLectureId_Success() {
        // Given
        Long lectureId = 1L;
        when(lessonRepository.findLessonsByLectureId(lectureId))
            .thenReturn(Arrays.asList(lesson1, lesson2));

        // When
        LessonsResponseDto result = lectureService.getLessonsByLectureId(lectureId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getLessons().size());
        assertEquals("1강. 집합의 개념", result.getLessons().get(0).getTitle());
    }

    @Test
    @DisplayName("강의별 레슨 목록 조회 - 레슨 없음")
    void getLessonsByLectureId_NoLessons() {
        // Given
        Long lectureId = 999L;
        when(lessonRepository.findLessonsByLectureId(lectureId))
            .thenReturn(Collections.emptyList());

        // When
        LessonsResponseDto result = lectureService.getLessonsByLectureId(lectureId);

        // Then
        assertNotNull(result);
        assertTrue(result.getLessons().isEmpty());
    }
}