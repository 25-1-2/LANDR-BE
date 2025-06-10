package com.landr.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.landr.controller.user.dto.LoginRequest;
import com.landr.domain.user.User;
import com.landr.domain.user.UserDevice;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.user.UserRepository;
import com.landr.repository.userdevice.UserDeviceRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @InjectMocks
    private UserService userService;

    private LoginRequest loginRequest;
    private User existingUser;
    private User newUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@test.com", "testuser", "fcm_token_123");

        existingUser = User.builder()
            .id(1L)
            .email("test@test.com")
            .name("testuser")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        newUser = User.builder()
            .id(2L)
            .email("new@test.com")
            .name("newuser")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("기존 사용자 로그인 성공")
    void findOrCreateUser_ExistingUser_Success() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(userDeviceRepository.save(any(UserDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.findOrCreateUser(loginRequest);

        // Then
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(existingUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(userDeviceRepository, times(1)).save(any(UserDevice.class));
    }

    @Test
    @DisplayName("신규 사용자 생성 및 로그인 성공")
    void findOrCreateUser_NewUser_Success() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            savedUser.setCreatedAt(LocalDateTime.now());
            savedUser.setUpdatedAt(LocalDateTime.now());
            return savedUser;
        });
        when(userDeviceRepository.save(any(UserDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.findOrCreateUser(loginRequest);

        // Then
        assertNotNull(result.getId());
        assertEquals(loginRequest.getEmail(), result.getEmail());
        assertEquals(loginRequest.getName(), result.getName());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userDeviceRepository, times(1)).save(any(UserDevice.class));
    }

    @Test
    @DisplayName("사용자 이름 변경 성공")
    void updateUserName_Success() {
        // Given
        Long userId = 1L;
        String newName = "newName";
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // When
        userService.updateUserName(userId, newName);

        // Then
        assertEquals(newName, existingUser.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("사용자 이름 변경 실패 - 사용자를 찾을 수 없음")
    void updateUserName_UserNotFound() {
        // Given
        Long userId = 999L;
        String newName = "updatedName";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> userService.updateUserName(userId, newName));

        assertEquals(ExceptionType.USER_NOT_FOUND, exception.getExceptionType());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("사용자 이름 변경 실패 - 짧은 이름")
    void updateUserName_InvalidShortName() {
        // Given
        Long userId = 1L;
        String invalidName = "a"; // 너무 짧은 이름
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> userService.updateUserName(userId, invalidName));

        assertEquals(ExceptionType.INVALID_USER_NAME, exception.getExceptionType());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("사용자 이름 변경 실패 - 긴 이름")
    void updateUserName_InvalidLongName() {
        // Given
        Long userId = 1L;
        String invalidName = "aaaaaaaaaaa"; // 너무 짧은 이름
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> userService.updateUserName(userId, invalidName));

        assertEquals(ExceptionType.INVALID_USER_NAME, exception.getExceptionType());
        verify(userRepository, times(1)).findById(userId);
    }
}