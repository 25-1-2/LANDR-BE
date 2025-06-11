package com.landr.controller.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계획 수정 요청")
public class UpdatePlanRequest {

    @Schema(description = "종료 날짜 (PERIOD 타입만 수정 가능)", example = "2025-07-15")
    private LocalDate endDate;

    @Schema(description = "하루 공부 시간 (TIME 타입만 수정 가능)", example = "60")
    private Integer dailyTime;

    @Min(value = 1, message = "재생 속도는 최소 1.0배 이상이어야 합니다")
    @Max(value = 2, message = "재생 속도는 최대 2.0배까지 가능합니다")
    @Schema(description = "강의 속도 (모든 타입 수정 가능)", example = "1.75", minimum = "1.0", maximum = "2.0")
    private Float playbackSpeed;

    @AssertTrue(message = "PERIOD 타입은 endDate와 playbackSpeed만 수정 가능합니다")
    private boolean isValidForPeriodType() {
        // 이 검증은 서비스 레이어에서 처리
        return true;
    }

    @AssertTrue(message = "TIME 타입은 dailyTime과 playbackSpeed만 수정 가능합니다")
    private boolean isValidForTimeType() {
        // 이 검증은 서비스 레이어에서 처리
        return true;
    }

    @AssertTrue(message = "종료 날짜는 오늘 이후여야 합니다")
    private boolean isEndDateValid() {
        if (endDate == null) {
            return true;
        }
        return endDate.isAfter(LocalDate.now()) || endDate.isEqual(LocalDate.now());
    }

    // 타입별 검증을 위한 헬퍼 메서드들
    public boolean hasOnlyPeriodFields() {
        return dailyTime == null && (endDate != null || playbackSpeed != null);
    }

    public boolean hasOnlyTimeFields() {
        return endDate == null && (dailyTime != null || playbackSpeed != null);
    }

    public boolean hasAnyUpdateField() {
        return endDate != null || dailyTime != null || playbackSpeed != null;
    }
}