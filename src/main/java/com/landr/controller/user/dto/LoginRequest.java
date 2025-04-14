package com.landr.controller.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 로그인 요청 정보")
public class LoginRequest {

    @Schema(description = "사용자 이메일", example = "test@test.com", requiredMode = REQUIRED)
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "사용자 이름", example = "test_name", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @Schema(description = "FCM 토큰", example = "dJ3x_FCM_Token_Example", requiredMode = REQUIRED)
    private String fcmToken;
}