package com.landr.controller.lessonschedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToggleCheckResponse {

    private Long lessonScheduleId;
    private Boolean checked;
}
