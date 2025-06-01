package com.landr.controller.studygroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupMemberDto {
    private Long userId;
    private String userName;
    private Long planId;
}