package com.landr.config;

import com.landr.domain.user.User;
import com.landr.repository.user.UserRepository;
import com.landr.exception.ExceptionType;
import com.landr.exception.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

            // 공개 URL 패턴 체크 - 토큰 검증을 건너뛸 URL 패턴들
            String requestURI = request.getRequestURI();
            if (isPublicUrl(requestURI)) {
                logger.debug("공개 URL ({})에 대한 요청으로 토큰 검증을 건너뜁니다.", requestURI);
                filterChain.doFilter(request, response); // 다음 필터로 진행
                return;
            }

            String token = resolveToken(request);

            // 토큰이 없는 경우 TOKEN_NOT_FOUND 예외 발생
            if (token == null) {
                logger.debug("JWT 토큰이 없습니다.");
                throw new ApiException(ExceptionType.INVALID_TOKEN);
            }

            // 토큰 유효성 검증
            jwtTokenProvider.validateToken(token);

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ExceptionType.TOKEN_NOT_FOUND_USER));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.emptyList()); // 인증된 사용자 객체, 비밀번호(여기서는 null, 이미 인증이 완료되었기 때문), 사용자 권한 목록(여기서는 빈 리스트, 실제로는 사용자 역할에 따른 권한 목록이 들어갈 수 있음)

            SecurityContextHolder.getContext().setAuthentication(authentication); //Spring Security의 보안 컨텍스트에 방금 생성한 인증 토큰을 설정. 이렇게 설정하면 해당 스레드의 나머지 요청 처리 과정에서 이 사용자를 인증된 사용자로 인식
            logger.debug("사용자 인증 성공: name={}, email={}", user.getName(), user.getEmail());

            filterChain.doFilter(request, response); // 다음 필터로 진행
    }

    // 공개 URL 패턴 체크 - 토큰 검증을 건너뛸 URL 패턴들
    private boolean isPublicUrl(String requestURI) {
        return requestURI.startsWith("/api/users/login") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.equals("/swagger-ui.html");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null) {
            return null;
        }

        if (!bearerToken.startsWith("Bearer ")) {
            logger.warn("잘못된 형식의 토큰: Bearer 접두사가 없음");
            throw new ApiException(ExceptionType.INVALID_TOKEN, "토큰 형식이 올바르지 않습니다. 'Bearer ' 접두사가 필요합니다.");
        }

        return bearerToken.substring(7);
    }
}