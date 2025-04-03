package com.landr.config;

import com.landr.domain.user.User;
import com.landr.repository.user.UserRepository;
import com.landr.exception.ExceptionType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
        SecurityContextHolder.clearContext(); // 각 테스트 전 SecurityContext 초기화
    }

    @Test
    void testPublicUrlShouldPassWithoutAuthentication() throws Exception {
        // 공개 URL 테스트
        when(request.getRequestURI()).thenReturn("/api/users/login");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testMissingTokenShouldThrowException() throws Exception {
        // 토큰 없는 경우 테스트
        when(request.getRequestURI()).thenReturn("/api/some-protected-endpoint");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(ExceptionType.TOKEN_NOT_FOUND.getHttpStatusCode().value());

        // 정확한 JSON 문자열로 검증
        verify(printWriter).write("{\"errorCode\":40124,\"description\":\"토큰이 존재하지 않습니다.\"}");
    }

    @Test
    void testInvalidTokenShouldThrowException() throws Exception {
        // 유효하지 않은 토큰 테스트
        when(request.getRequestURI()).thenReturn("/api/some-protected-endpoint");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(ExceptionType.INVALID_TOKEN.getHttpStatusCode().value());

        // 정확한 JSON 문자열로 검증
        verify(printWriter).write("{\"errorCode\":40122,\"description\":\"유효하지 않은 토큰입니다.\"}");
    }

    @Test
    void testValidTokenWithExistingUserShouldAuthenticate() throws Exception {
        // 유효한 토큰과 존재하는 사용자 테스트
        String validToken = "valid-token";
        Long userId = 1L;
        User mockUser = mock(User.class);

        when(request.getRequestURI()).thenReturn("/api/some-protected-endpoint");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getEmail()).thenReturn("test@example.com");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(mockUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void testTokenWithNonExistentUserShouldThrowException() throws Exception {
        // 토큰의 사용자가 데이터베이스에 없는 경우 테스트
        String validToken = "valid-token";
        Long userId = 1L;

        when(request.getRequestURI()).thenReturn("/api/some-protected-endpoint");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(ExceptionType.TOKEN_NOT_FOUND_USER.getHttpStatusCode().value());

        // 정확한 JSON 문자열로 검증
        verify(printWriter).write("{\"errorCode\":40125,\"description\":\"ID가 1인 사용자를 찾을 수 없습니다.\"}");
    }
}