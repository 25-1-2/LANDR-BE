package com.landr.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 이름 수정 요청 정보")
public class EditNameRequest {

    @Schema(description = "수정될 사용자 이름", minLength = 3, maxLength = 9, example = "edit_name", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 3, max = 9, message = "이름은 3자 이상 9자 이하로 입력해야 합니다.")
    private String name;
}
