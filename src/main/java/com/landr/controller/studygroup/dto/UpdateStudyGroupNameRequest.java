package com.landr.controller.studygroup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "스터디 그룹 이름 수정 요청")
public class UpdateStudyGroupNameRequest {

    @Schema(description = "새로운 스터디 그룹 이름", example = "스터디 그룹명 수정", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "스터디 그룹 이름은 필수입니다.")
    @Size(max = 10, message = "스터디 그룹 이름은 10자 이하로 입력해주세요.")
    private String name;
}