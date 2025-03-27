package com.landr.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "홈 화면에서 강의 이름 수정 요청")
public class EditLectureNameRequest {

    @Schema(description = "강의 별명", example = "미적분", maxLength = 8, requiredMode = REQUIRED)
    @Size(max = 8, message = "강의 별명 최대 8글자까지 입력 가능합니다.")
    private String lectureAlias;
}
