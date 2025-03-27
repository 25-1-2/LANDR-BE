package com.landr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.landr.controller.AuthController;
import com.landr.controller.dto.GoogleAuthRequest;
import com.landr.domain.user.User;
import com.landr.repository.UserRepository;
import com.landr.service.GoogleAuthService;
import com.landr.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class LandrApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoogleAuthService googleAuthService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    private GoogleAuthRequest authRequest;
    private User testUser;

    // 테스트 데이터
    private String testEmail = "test@example.com";
    private String testName = "테스트 사용자";

    @BeforeEach
    void setUp() {
        // 테스트 요청 객체 준비
        authRequest = new GoogleAuthRequest();
        authRequest.setIdToken("test.google.token");

        // 테스트 사용자 객체 준비
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(testEmail);
        testUser.setName(testName);
        testUser.setProvider("GOOGLE");
    }

    @Test
    @DisplayName("구글 로그인 테스트 - 기존 사용자")
    void googleLoginExistingUserTest() throws Exception {
        // Mockito를 사용하여 GoogleIdToken.Payload 객체 모킹
        GoogleIdToken.Payload mockPayload = Mockito.mock(GoogleIdToken.Payload.class);
        when(mockPayload.getEmail()).thenReturn(testEmail);
        when(mockPayload.get("name")).thenReturn(testName);

        when(googleAuthService.verifyGoogleIdToken(anyString())).thenReturn(mockPayload);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");

        mockMvc.perform(
                        post("/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    @DisplayName("구글 로그인 테스트 - 신규 사용자")
    void googleLoginNewUserTest() throws Exception {
        // Mockito를 사용하여 GoogleIdToken.Payload 객체 모킹
        GoogleIdToken.Payload mockPayload = Mockito.mock(GoogleIdToken.Payload.class);
        when(mockPayload.getEmail()).thenReturn(testEmail);
        when(mockPayload.get("name")).thenReturn(testName);

        when(googleAuthService.verifyGoogleIdToken(anyString())).thenReturn(mockPayload);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("new.user.jwt.token");

        mockMvc.perform(
                        post("/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new.user.jwt.token"));
    }

    @Test
    @DisplayName("구글 로그인 테스트 - 인증 실패")
    void googleLoginFailureTest() throws Exception {
        when(googleAuthService.verifyGoogleIdToken(anyString()))
                .thenThrow(new Exception("유효하지 않은 ID 토큰"));

        mockMvc.perform(
                        post("/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest))
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())  // 500 대신 401로 변경
                .andExpect(jsonPath("$.errorCode").value(40101))  // 에러 코드 검증 추가
                .andExpect(jsonPath("$.errorMessage").value("인증 실패: 유효하지 않은 ID 토큰"));  // 에러 메시지 검증
    }
}