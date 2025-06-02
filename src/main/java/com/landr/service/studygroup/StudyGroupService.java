package com.landr.service.studygroup;

import com.landr.controller.studygroup.dto.*;
import com.landr.domain.user.User;

public interface StudyGroupService {

    /**
     * 스터디 그룹을 생성합니다.
     */
    CreateStudyGroupResponse createStudyGroup(Long planId, User user);

    /**
     * 스터디 그룹에 가입합니다.
     */
    void joinStudyGroup(JoinStudyGroupRequest request, User user);

    /**
     * 스터디 그룹 상세 정보를 조회합니다.
     */
    StudyGroupDetailResponse getStudyGroupDetail(Long studyGroupId, User user);

    /**
     * 스터디 그룹 이름을 수정합니다.
     */
    void updateStudyGroupName(Long studyGroupId, UpdateStudyGroupNameRequest request, User user);

    /**
     * 스터디 그룹에서 멤버를 추방합니다.
     */
    void kickMember(Long studyGroupId, Long targetUserId, User user);

    void deleteStudyGroup(Long studyGroupId, User user);

    void transferLeader(Long studyGroupId, Long newLeaderId, User user);
}