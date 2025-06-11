// src/test/java/com/landr/service/recommend/LectureRecommendServiceTest.java
package com.landr.service.recommend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.landr.controller.user.dto.LectureRecommendRequest;
import com.landr.controller.user.dto.LectureRecommendResponse;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import com.landr.repository.recommend.LectureRecommendRepository;
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
class LectureRecommendServiceTest {

    @Mock
    private LectureRecommendRepository lectureRepository;

    @Mock
    private LectureMapper lectureMapper;

    @Mock
    private GPTService gptService;

    @InjectMocks
    private LectureRecommendService lectureRecommendService;

    private LectureRecommendRequest request;
    private Lecture mathLecture1, mathLecture2, englishLecture;
    private LectureRecommendResponse response1, response2, response3;

    @BeforeEach
    void setUp() {
        request = new LectureRecommendRequest();
        request.setGrade("고1");
        request.setSchoolRank(3);
        request.setMockRank(4);
        request.setFocus("수능 중심");
        request.setGoal("개념 정리");
        request.setStyles(Arrays.asList("개념 위주", "차분한 설명"));
        request.setSubject("수학Ⅰ");

        mathLecture1 = Lecture.builder()
            .id(1L)
            .title("수학Ⅰ 개념완성")
            .teacher("김수학")
            .platform(Platform.MEGA)
            .subject(Subject.MATH)
            .tag("개념,기초,수학1")
            .totalLessons(30)
            .createdAt(LocalDateTime.now())
            .build();

        mathLecture2 = Lecture.builder()
            .id(2L)
            .title("수학Ⅰ 심화문제")
            .teacher("박수학")
            .platform(Platform.ETOOS)
            .subject(Subject.MATH)
            .tag("심화,문제풀이,수학1")
            .totalLessons(25)
            .createdAt(LocalDateTime.now())
            .build();

        englishLecture = Lecture.builder()
            .id(3L)
            .title("영어 문법")
            .teacher("이영어")
            .platform(Platform.EBSI)
            .subject(Subject.ENG)
            .tag("문법,기초")
            .totalLessons(20)
            .createdAt(LocalDateTime.now())
            .build();

        response1 = LectureRecommendResponse.builder()
            .id(1L)
            .title("수학Ⅰ 개념완성")
            .teacher("김수학")
            .platform("MEGA")
            .recommendScore(95.0)
            .recommendReason("기초 개념 학습에 적합")
            .build();

        response2 = LectureRecommendResponse.builder()
            .id(2L)
            .title("수학Ⅰ 심화문제")
            .teacher("박수학")
            .platform("ETOOS")
            .recommendScore(85.0)
            .recommendReason("문제풀이 능력 향상")
            .build();

        response3 = LectureRecommendResponse.builder()
            .id(3L)
            .title("수학Ⅰ 기초")
            .teacher("최수학")
            .platform("DAESANG")
            .recommendScore(80.0)
            .recommendReason("기초부터 차근차근")
            .build();
    }

    @Test
    @DisplayName("강의 추천 - 과목에 해당하는 강의 없음")
    void recommend_NoLecturesFound() {
        // Given
        when(lectureRepository.countBySubjectKeyword("수학Ⅰ")).thenReturn(0L);
        when(lectureRepository.countBySubjectKeyword("수학")).thenReturn(0L);
        when(lectureRepository.findBySubjectAndGrade("수학", "고1"))
            .thenReturn(Collections.emptyList());

        // When
        List<LectureRecommendResponse> result = lectureRecommendService.recommend(request);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("강의 추천 - fallback 동작")
    void recommend_FallbackScenario() {
        // Given
        when(lectureRepository.countBySubjectKeyword("수학Ⅰ")).thenReturn(5L);
        when(lectureRepository.findBySubjectAndGrade("수학Ⅰ", "고1"))
            .thenReturn(Arrays.asList(mathLecture1, mathLecture2));
        when(gptService.getRecommendation(anyString()))
            .thenThrow(new RuntimeException("GPT API 오류"));
        when(lectureRepository.findBySubjectKeyword("수학"))
            .thenReturn(Arrays.asList(mathLecture1, mathLecture2));
        when(lectureMapper.toDto(any(Lecture.class))).thenReturn(response1);

        // When
        List<LectureRecommendResponse> result = lectureRecommendService.recommend(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().noneMatch(LectureRecommendResponse::getIsPersonalized));
    }

    @Test
    @DisplayName("강의 추천 - 다양한 과목 테스트")
    void recommend_VariousSubjects() {
        // Given
        String[] subjects = {"수학Ⅰ", "수학Ⅱ", "미적분", "확률과통계", "기하"};

        for (String subject : subjects) {
            request.setSubject(subject);
            when(lectureRepository.countBySubjectKeyword(subject)).thenReturn(3L);
            when(lectureRepository.findBySubjectAndGrade(subject, "고1"))
                .thenReturn(Arrays.asList(mathLecture1));
            when(gptService.getRecommendation(anyString()))
                .thenReturn("1|90|적합한 강의");
            when(lectureMapper.toDto(any(Lecture.class))).thenReturn(response1);

            // When
            List<LectureRecommendResponse> result = lectureRecommendService.recommend(request);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    @Test
    @DisplayName("강의 추천 - 다양한 학년 테스트")
    void recommend_VariousGrades() {
        // Given
        String[] grades = {"고1", "고2", "고3", "N수생"};

        for (String grade : grades) {
            request.setGrade(grade);
            when(lectureRepository.countBySubjectKeyword("수학Ⅰ")).thenReturn(5L);
            when(lectureRepository.findBySubjectAndGrade(eq("수학Ⅰ"), anyString()))
                .thenReturn(Arrays.asList(mathLecture1));
            when(gptService.getRecommendation(anyString()))
                .thenReturn("1|85|학년에 적합");
            when(lectureMapper.toDto(any(Lecture.class))).thenReturn(response1);

            // When
            List<LectureRecommendResponse> result = lectureRecommendService.recommend(request);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
}
