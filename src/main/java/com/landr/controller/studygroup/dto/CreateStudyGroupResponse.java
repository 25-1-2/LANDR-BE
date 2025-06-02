package com.landr.controller.studygroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStudyGroupResponse {
    private Long studyGroupId;
    private String inviteCode;
    private String name;
}