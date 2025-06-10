package com.landr.domain.dday;

import static org.junit.jupiter.api.Assertions.*;

import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DDayTest {

    private User user1, user2;
    private DDay dDay;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).name("User 1").build();
        user2 = User.builder().id(2L).name("User 2").build();

        dDay = DDay.builder()
            .id(1L)
            .user(user1)
            .title("중간고사")
            .goalDate(LocalDate.now().plusDays(30))
            .build();
    }

    @Test
    @DisplayName("D-Day 업데이트 성공")
    void update_Success() {
        // Given
        String newTitle = "기말고사";
        LocalDate newGoalDate = LocalDate.now().plusDays(60);

        // When
        dDay.update(newTitle, newGoalDate);

        // Then
        assertEquals(newTitle, dDay.getTitle());
        assertEquals(newGoalDate, dDay.getGoalDate());
    }

    @Test
    @DisplayName("D-Day 업데이트 - 제목만 변경")
    void update_OnlyTitle() {
        // Given
        String originalTitle = dDay.getTitle();
        LocalDate originalGoalDate = dDay.getGoalDate();
        String newTitle = "수정된 시험";

        // When
        dDay.update(newTitle, null);

        // Then
        assertEquals(newTitle, dDay.getTitle());
        assertEquals(originalGoalDate, dDay.getGoalDate());
    }

    @Test
    @DisplayName("D-Day 업데이트 - 날짜만 변경")
    void update_OnlyDate() {
        // Given
        String originalTitle = dDay.getTitle();
        LocalDate newGoalDate = LocalDate.now().plusDays(45);

        // When
        dDay.update(null, newGoalDate);

        // Then
        assertEquals(originalTitle, dDay.getTitle());
        assertEquals(newGoalDate, dDay.getGoalDate());
    }

    @Test
    @DisplayName("D-Day 업데이트 - 빈 제목은 무시")
    void update_EmptyTitle() {
        // Given
        String originalTitle = dDay.getTitle();

        // When
        dDay.update("", dDay.getGoalDate());

        // Then
        assertEquals(originalTitle, dDay.getTitle());
    }

    @Test
    @DisplayName("D-Day 업데이트 - 과거 날짜는 무시")
    void update_PastDate() {
        // Given
        LocalDate originalGoalDate = dDay.getGoalDate();
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When
        dDay.update(dDay.getTitle(), pastDate);

        // Then
        assertEquals(originalGoalDate, dDay.getGoalDate());
    }

    @Test
    @DisplayName("소유자 확인 성공")
    void isOwner_Success() {
        // When & Then
        assertDoesNotThrow(() -> dDay.isOwner(user1.getId()));
    }

    @Test
    @DisplayName("소유자 확인 실패")
    void isOwner_Fail() {
        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> dDay.isOwner(user2.getId()));

        assertEquals(ExceptionType.DDAY_OWNER_NOT_MATCH, exception.getExceptionType());
    }
}