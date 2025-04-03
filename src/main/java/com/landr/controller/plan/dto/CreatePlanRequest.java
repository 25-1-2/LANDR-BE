package com.landr.controller.plan.dto;

import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계획 생성 요청")
public class CreatePlanRequest {

    @NotNull(message = "강의 ID는 필수입니다")
    @Schema(description = "강의 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lectureId;

    @NotNull(message = "계획 타입은 필수입니다")
    @Schema(
        description = "계획 타입",
        allowableValues = {"PERIOD", "TIME"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PlanType planType;

    @NotNull(message = "시작 lesson Id는 필수입니다")
    @Schema(description = "시작 lesson Id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long startLessonId;

    @NotNull(message = "종료 lesson Id는 필수입니다")
    @Schema(description = "종료 lesson Id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long endLessonId;

    @NotNull(message = "공부 요일 선택은 필수입니다")
    @Schema(
        description = "공부 요일",
        allowableValues = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Set<DayOfWeek> studyDayOfWeeks;

    @Schema(description = "하루 공부 시간", example = "120", requiredMode = RequiredMode.NOT_REQUIRED)
    private Integer dailyTime;

    @Schema(description = "시작 날짜", example = "2025-04-03", requiredMode = RequiredMode.NOT_REQUIRED)
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2025-05-31", requiredMode = RequiredMode.NOT_REQUIRED)
    private LocalDate endDate;

    @Min(value = 1, message = "재생 속도는 최소 1.0배 이상이어야 합니다")
    @Max(value = 2, message = "재생 속도는 최대 2.0배까지 가능합니다")
    @Schema(description = "강의 속도", example = "1.5", minimum = "1.0", maximum = "2.0", requiredMode = RequiredMode.REQUIRED)
    private Float playbackSpeed;

    @AssertTrue(message = "시작 날짜는 종료 날짜보다 이후일 수 없습니다")
    private boolean isStartDateBeforeEndDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return startDate.isBefore(endDate) || startDate.isEqual(endDate);
    }
}
