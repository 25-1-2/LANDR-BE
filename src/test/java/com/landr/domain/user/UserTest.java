package com.landr.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .email("test@test.com")
            .name("Test User")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isDeleted(false)
            .build();
    }

    @Test
    @DisplayName("사용자 이름 업데이트 성공")
    void updateName_Success() {
        // When
        user.updateName("New Name");

        // Then
        assertEquals("New Name", user.getName());
    }

    @Test
    @DisplayName("사용자 이름 업데이트 실패 - null")
    void updateName_Null() {
        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> user.updateName(null));

        assertEquals(ExceptionType.INVALID_USER_NAME, exception.getExceptionType());
    }

    @Test
    @DisplayName("사용자 이름 업데이트 실패 - 빈 문자열")
    void updateName_Empty() {
        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> user.updateName(""));

        assertEquals(ExceptionType.INVALID_USER_NAME, exception.getExceptionType());
    }

    @Test
    @DisplayName("사용자 이름 업데이트 실패 - 너무 짧음")
    void updateName_TooShort() {
        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> user.updateName("ab"));

        assertEquals(ExceptionType.INVALID_USER_NAME, exception.getExceptionType());
    }

    @Test
    @DisplayName("사용자 이름 업데이트 실패 - 너무 김")
    void updateName_TooLong() {
        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> user.updateName("verylongname"));

        assertEquals(ExceptionType.INVALID_USER_NAME, exception.getExceptionType());
    }

    @Test
    @DisplayName("onUpdate 메서드 테스트")
    void onUpdate_Test() {
        // Given
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        // When
        try {
            Thread.sleep(10); // 시간 차이를 만들기 위해
        } catch (InterruptedException e) {
            // ignore
        }
        user.onUpdate();

        // Then
        assertNotEquals(originalUpdatedAt, user.getUpdatedAt());
    }
}