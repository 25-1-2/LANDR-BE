package com.landr.controller.studygroup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스터디 그룹 가입 요청")
public class JoinStudyGroupRequest {

    @Schema(description = "초대 코드 (4자리 숫자)", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "초대 코드는 필수입니다.")
    @Pattern(regexp = "\\d{4}", message = "초대 코드는 4자리 숫자여야 합니다.")
    private String inviteCode;
}