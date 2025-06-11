// src/test/java/com/landr/domain/plan/PlanTest.java
package com.landr.domain.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlanTest {

    private Plan plan;
    private User user;
    private Lecture lecture;
    private Lesson startLesson, endLesson;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .email("test@test.com")
            .name("Test User")
            .build();

        lecture = Lecture.builder()
            .id(1L)
            .title("수학 기초")
            .teacher("김선생")
            .platform(Platform.MEGA)
            .subject(Subject.MATH)
            .build();

        startLesson = Lesson.builder()
            .id(1L)
            .lecture(lecture)
            .title("1강. 집합의 개념")
            .order(1)
            .duration(60)
            .build();

        endLesson = Lesson.builder()
            .id(10L)
            .lecture(lecture)
            .title("10강. 함수의 극한")
            .order(10)
            .duration(90)
            .build();

        Set<DayOfWeek> studyDays = new HashSet<>();
        studyDays.add(DayOfWeek.MON);
        studyDays.add(DayOfWeek.WED);
        studyDays.add(DayOfWeek.FRI);

        plan = Plan.builder()
            .id(1L)
            .user(user)
            .lecture(lecture)
            .lectureName("수학 기초 강의")
            .startLesson(startLesson)
            .endLesson(endLesson)
            .planType(PlanType.PERIOD)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .dailyTime(120)
            .playbackSpeed(1.5f)
            .studyDays(studyDays)
            .isDeleted(false)
            .build();
    }

    @Test
    @DisplayName("강의 이름 수정 성공")
    void editLectureName_Success() {
        // Given
        String newLectureName = "미적분 기초";

        // When
        plan.editLectureName(newLectureName);

        // Then
        assertEquals(newLectureName, plan.getLectureName());
    }

    @Test
    @DisplayName("계획 삭제")
    void delete_Success() {
        // Given
        assertFalse(plan.getIsDeleted());

        // When
        plan.delete();

        // Then
        assertTrue(plan.getIsDeleted());
    }

    @Test
    @DisplayName("PERIOD 타입 계획 업데이트 성공")
    void updateForPeriodType_Success() {
        // Given
        LocalDate newEndDate = LocalDate.now().plusDays(60);
        Float newPlaybackSpeed = 2.0f;

        // When
        plan.updateForPeriodType(newEndDate, newPlaybackSpeed);

        // Then
        assertEquals(newEndDate, plan.getEndDate());
        assertEquals(newPlaybackSpeed, plan.getPlaybackSpeed());
    }

    @Test
    @DisplayName("PERIOD 타입 계획 업데이트 - 종료일만 변경")
    void updateForPeriodType_OnlyEndDate() {
        // Given
        LocalDate newEndDate = LocalDate.now().plusDays(45);
        Float originalPlaybackSpeed = plan.getPlaybackSpeed();

        // When
        plan.updateForPeriodType(newEndDate, null);

        // Then
        assertEquals(newEndDate, plan.getEndDate());
        assertEquals(originalPlaybackSpeed, plan.getPlaybackSpeed());
    }

    @Test
    @DisplayName("PERIOD 타입 계획 업데이트 - 재생속도만 변경")
    void updateForPeriodType_OnlyPlaybackSpeed() {
        // Given
        LocalDate originalEndDate = plan.getEndDate();
        Float newPlaybackSpeed = 1.25f;

        // When
        plan.updateForPeriodType(null, newPlaybackSpeed);

        // Then
        assertEquals(originalEndDate, plan.getEndDate());
        assertEquals(newPlaybackSpeed, plan.getPlaybackSpeed());
    }

    @Test
    @DisplayName("PERIOD 타입 계획 업데이트 실패 - 과거 종료일")
    void updateForPeriodType_PastEndDate() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> plan.updateForPeriodType(pastDate, null));

        assertEquals(ExceptionType.BAD_REQUEST, exception.getExceptionType());
        assertTrue(exception.getErrorDescription().contains("종료 날짜는 오늘 이후여야 합니다"));
    }

    @Test
    @DisplayName("PERIOD 타입 계획 업데이트 실패 - 시작일보다 이른 종료일")
    void updateForPeriodType_EndDateBeforeStartDate() {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(5);
        plan = Plan.builder()
            .startDate(startDate)
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> plan.updateForPeriodType(endDate, null));

        assertEquals(ExceptionType.BAD_REQUEST, exception.getExceptionType());
        assertTrue(exception.getErrorDescription().contains("종료 날짜는 시작 날짜보다 이후여야 합니다"));
    }

    @Test
    @DisplayName("TIME 타입 계획 업데이트 성공")
    void updateForTimeType_Success() {
        // Given
        Integer newDailyTime = 180;
        Float newPlaybackSpeed = 1.75f;

        // When
        plan.updateForTimeType(newDailyTime, newPlaybackSpeed);

        // Then
        assertEquals(newDailyTime, plan.getDailyTime());
        assertEquals(newPlaybackSpeed, plan.getPlaybackSpeed());
    }

    @Test
    @DisplayName("TIME 타입 계획 업데이트 - 일일 시간만 변경")
    void updateForTimeType_OnlyDailyTime() {
        // Given
        Integer newDailyTime = 90;
        Float originalPlaybackSpeed = plan.getPlaybackSpeed();

        // When
        plan.updateForTimeType(newDailyTime, null);

        // Then
        assertEquals(newDailyTime, plan.getDailyTime());
        assertEquals(originalPlaybackSpeed, plan.getPlaybackSpeed());
    }

    @Test
    @DisplayName("TIME 타입 계획 업데이트 실패 - 잘못된 일일 시간")
    void updateForTimeType_InvalidDailyTime() {
        // Given
        Integer invalidDailyTime = -10;

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> plan.updateForTimeType(invalidDailyTime, null));

        assertEquals(ExceptionType.BAD_REQUEST, exception.getExceptionType());
        assertTrue(exception.getErrorDescription().contains("하루 공부 시간은 0보다 커야 합니다"));
    }

    @Test
    @DisplayName("TIME 타입 계획 업데이트 실패 - 0 일일 시간")
    void updateForTimeType_ZeroDailyTime() {
        // Given
        Integer zeroDailyTime = 0;

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> plan.updateForTimeType(zeroDailyTime, null));

        assertEquals(ExceptionType.BAD_REQUEST, exception.getExceptionType());
        assertTrue(exception.getErrorDescription().contains("하루 공부 시간은 0보다 커야 합니다"));
    }

    @Test
    @DisplayName("Plan 생성 시 기본값 설정")
    void onCreate_DefaultValues() {
        // Given
        Plan newPlan = Plan.builder()
            .user(user)
            .lecture(lecture)
            .lectureName("테스트 강의")
            .build();

        // When
        newPlan.onCreate();

        // Then
        assertNotNull(newPlan.getCreatedAt());
        assertEquals(Boolean.FALSE, newPlan.getIsDeleted());
    }
}
