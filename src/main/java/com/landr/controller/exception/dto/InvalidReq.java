package com.landr.controller.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "예외 테스트 API 요청")
public class InvalidReq {

    @Schema(description = "필드", example = " ")
    @NotEmpty(message = "필드는 필수값입니다.")
    private String field;
}
