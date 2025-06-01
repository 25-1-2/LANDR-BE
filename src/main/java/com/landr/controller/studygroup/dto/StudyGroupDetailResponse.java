package com.landr.controller.studygroup.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupDetailResponse {
    private Long studyGroupId;
    private String name;
    private String inviteCode;
    private Long leaderId;
    private String leaderName;
    private String lectureName;
    private List<StudyGroupMemberDto> members;
}