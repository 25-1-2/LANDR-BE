package com.landr.controller.dday.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DDay 수정 요청")
public class UpdateDDayRequest {

    @Schema(description = "수정할 DDay 제목, 제목을 수정하지 않으면 해당 컬럼은 제외해도 된다", example = "기말고사", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "수정할 DDay 목표 날짜, 목표 날짜를 수정하지 않으면 해당 컬럼은 제외해도 된다", example = "2025-07-14", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate goalDate;
}
