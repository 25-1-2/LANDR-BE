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

        try {
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
                handleJwtException(response, new ApiException(ExceptionType.TOKEN_NOT_FOUND));
                return;
            }

            // 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(token)) {
                handleJwtException(response, new ApiException(ExceptionType.INVALID_TOKEN));
                return;
            }

            try {
                /* 토큰에서 사용자 ID 추출,JWT 토큰은 서버에서 발급한 인증 토큰으로, 클라이언트가 요청 시 이 토큰을 함께 보내 자신이 인증된 사용자임을 증명,
                토큰 기반 인증 시스템에서는 매 요청마다 사용자의 로그인 정보를 확인할 필요 없이, 이전에 발급된 토큰의 유효성만 검증
                 */
                Long userId = jwtTokenProvider.getUserIdFromToken(token);

                /*
                토큰에서 추출한 ID로 실제 사용자가 데이터베이스에 존재하는지 확인합니다.
                사용자가 없다면 오류를 반환하고 더 이상 처리하지 않습니다.
                토큰이 유효하더라도 데이터베이스에서 사용자가 삭제되었거나 비활성화되었을 수 있으므로, 항상 최신 사용자 상태를 확인해야 합니다.
                 */
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    handleJwtException(response, new ApiException(ExceptionType.TOKEN_NOT_FOUND_USER,
                            "ID가 " + userId + "인 사용자를 찾을 수 없습니다."));
                    return;
                }

                /* 검증된 사용자 정보를 Spring Security 컨텍스트에 등록
                애플리케이션의 나머지 부분(컨트롤러, 서비스 등)에서 Spring Security의 보안 기능(예: @PreAuthorize, @Secured 등)을 사용할 수 있다.
                또한 현재 요청을 처리하는 스레드 전체에서 인증된 사용자 정보에 쉽게 접근할 수 있음.
                 */
                User user = userOpt.get(); //이미 검증된 사용자 객체를 Optional에서 꺼내옴. 이 사용자 객체는 이미 다른 곳에서 검증되었다고 가정
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, Collections.emptyList()); // 인증된 사용자 객체, 비밀번호(여기서는 null, 이미 인증이 완료되었기 때문), 사용자 권한 목록(여기서는 빈 리스트, 실제로는 사용자 역할에 따른 권한 목록이 들어갈 수 있음)

                SecurityContextHolder.getContext().setAuthentication(authentication); //Spring Security의 보안 컨텍스트에 방금 생성한 인증 토큰을 설정. 이렇게 설정하면 해당 스레드의 나머지 요청 처리 과정에서 이 사용자를 인증된 사용자로 인식
                logger.debug("사용자 인증 성공: name={}, email={}", user.getName(), user.getEmail());

            } catch (Exception e) { // 토큰 형식이 잘못되었거나, 정보 추출 중 예외가 발생한 경우
                logger.error("토큰에서 사용자 정보 추출 중 오류 발생: {}", e.getMessage());
                handleJwtException(response, new ApiException(ExceptionType.AUTHENTICATION_FAILED));
                return;
            }
        } catch (ApiException ex) { //토큰 만료, 사용자 권한 부족 등)
            logger.error("JWT 인증 처리 중 API 예외 발생: {}", ex.getMessage());
            handleJwtException(response, ex);
            return;
        } catch (Exception e) { //데이터베이스 연결 문제, 서버 내부 오류 등
            logger.error("JWT 인증 처리 중 예상치 못한 오류 발생: {}", e.getMessage());
            handleJwtException(response, new ApiException(ExceptionType.AUTHENTICATION_FAILED));
            return;
        }
        filterChain.doFilter(request, response);
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

    /*
    보안 컨텍스트를 비워서 잘못된 인증 정보가 남지 않도록 함
    사용자에게 보낼 오류 응답을 생성
     */
    private void handleJwtException(HttpServletResponse response, ApiException ex) throws IOException {
        SecurityContextHolder.clearContext(); // 보안 컨텍스트 초기화
        setErrorResponse(response, ex); // 에러 응답 생성
    }

    private void setErrorResponse(HttpServletResponse response, ApiException ex) throws IOException {
        response.setContentType("application/json;charset=UTF-8"); // JSON 형식으로 응답
        response.setStatus(ex.getExceptionType().getHttpStatusCode().value()); // 상태 코드 설정
        response.getWriter().write(String.format(
                "{\"errorCode\":%d,\"description\":\"%s\"}",
                ex.getExceptionType().getErrorCode(),
                ex.getErrorDescription()
        ));
    }
}