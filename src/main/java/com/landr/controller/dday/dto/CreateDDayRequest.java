package com.landr.controller.dday.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DDay 생성 요청")
public class CreateDDayRequest {

    @Schema(description = "DDay 제목", example = "기말고사", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "DDay 제목은 필수입니다")
    private String title;

    @Schema(description = "DDay 목표 날짜", example = "2025-06-29", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "DDay 목표 날짜는 필수입니다")
    private LocalDate goalDate;
}
