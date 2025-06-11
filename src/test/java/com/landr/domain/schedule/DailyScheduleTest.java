// src/test/java/com/landr/domain/schedule/DailyScheduleTest.java
package com.landr.domain.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.user.User;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyScheduleTest {

    private DailySchedule dailySchedule;
    private Plan plan;
    private User user;
    private Lecture lecture;

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
            .build();

        plan = Plan.builder()
            .id(1L)
            .user(user)
            .lecture(lecture)
            .build();

        dailySchedule = DailySchedule.builder()
            .id(1L)
            .plan(plan)
            .date(LocalDate.of(2025, 6, 11))
            .dayOfWeek(DayOfWeek.WED)
            .totalLessons(3)
            .totalDuration(180)
            .build();
    }

    @Test
    @DisplayName("강의 추가 성공")
    void addLessons_Success() {
        // Given
        int additionalLessons = 2;
        int additionalDuration = 120;
        int originalLessons = dailySchedule.getTotalLessons();
        int originalDuration = dailySchedule.getTotalDuration();

        // When
        dailySchedule.addLessons(additionalLessons, additionalDuration);

        // Then
        assertEquals(originalLessons + additionalLessons, dailySchedule.getTotalLessons());
        assertEquals(originalDuration + additionalDuration, dailySchedule.getTotalDuration());
    }

    @Test
    @DisplayName("강의 추가 - 0개 강의")
    void addLessons_ZeroLessons() {
        // Given
        int additionalLessons = 0;
        int additionalDuration = 0;
        int originalLessons = dailySchedule.getTotalLessons();
        int originalDuration = dailySchedule.getTotalDuration();

        // When
        dailySchedule.addLessons(additionalLessons, additionalDuration);

        // Then
        assertEquals(originalLessons, dailySchedule.getTotalLessons());
        assertEquals(originalDuration, dailySchedule.getTotalDuration());
    }

    @Test
    @DisplayName("강의 추가 - 음수 강의 (실제로는 발생하지 않지만 방어적 테스트)")
    void addLessons_NegativeLessons() {
        // Given
        int additionalLessons = -1;
        int additionalDuration = -60;
        int originalLessons = dailySchedule.getTotalLessons();
        int originalDuration = dailySchedule.getTotalDuration();

        // When
        dailySchedule.addLessons(additionalLessons, additionalDuration);

        // Then
        assertEquals(originalLessons + additionalLessons, dailySchedule.getTotalLessons());
        assertEquals(originalDuration + additionalDuration, dailySchedule.getTotalDuration());
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void toStringTest() {
        // When
        String result = dailySchedule.toString();

        // Then
        assertTrue(result.contains("DailySchedule"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("date=2025-06-11"));
        assertTrue(result.contains("dayOfWeek=WED"));
        assertTrue(result.contains("totalLessons=3"));
        assertTrue(result.contains("totalDuration=180"));
    }

    @Test
    @DisplayName("DailySchedule 생성자 테스트")
    void constructor_AllFields() {
        // Given & When
        DailySchedule newSchedule = DailySchedule.builder()
            .id(2L)
            .plan(plan)
            .date(LocalDate.of(2025, 6, 12))
            .dayOfWeek(DayOfWeek.THU)
            .totalLessons(5)
            .totalDuration(300)
            .build();

        // Then
        assertEquals(2L, newSchedule.getId());
        assertEquals(plan, newSchedule.getPlan());
        assertEquals(LocalDate.of(2025, 6, 12), newSchedule.getDate());
        assertEquals(DayOfWeek.THU, newSchedule.getDayOfWeek());
        assertEquals(5, newSchedule.getTotalLessons());
        assertEquals(300, newSchedule.getTotalDuration());
    }

    @Test
    @DisplayName("여러 번 강의 추가")
    void addLessons_Multiple() {
        // Given
        int originalLessons = dailySchedule.getTotalLessons();
        int originalDuration = dailySchedule.getTotalDuration();

        // When
        dailySchedule.addLessons(1, 60);
        dailySchedule.addLessons(2, 90);
        dailySchedule.addLessons(1, 45);

        // Then
        assertEquals(originalLessons + 4, dailySchedule.getTotalLessons());
        assertEquals(originalDuration + 195, dailySchedule.getTotalDuration());
    }
}