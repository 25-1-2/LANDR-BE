package com.landr.service.studygroup;

import com.landr.controller.studygroup.dto.*;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.studygroup.StudyGroup;
import com.landr.domain.studygroup.StudyGroupMember;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.plan.PlanRepository;
import com.landr.repository.studygroup.StudyGroupMemberRepository;
import com.landr.repository.studygroup.StudyGroupRepository;
import com.landr.service.plan.PlanService;
import com.landr.controller.plan.dto.CreatePlanRequest;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMemberRepository studyGroupMemberRepository;
    private final PlanRepository planRepository;
    private final PlanService planService;

    @Override
    public CreateStudyGroupResponse createStudyGroup(Long planId, User user) {
        // 계획 조회 및 권한 확인
        Plan plan = planRepository.findByIdAndUserId(planId, user.getId())
            .orElseThrow(() -> new ApiException(ExceptionType.PLAN_NOT_FOUND));

        // 이미 해당 계획으로 스터디 그룹을 만들었는지 확인
        if (studyGroupRepository.findByLeaderIdAndBasePlanId(user.getId(), planId).isPresent()) {
            throw new ApiException(ExceptionType.STUDY_GROUP_ALREADY_EXISTS);
        }

        // 고유한 초대 코드 생성
        String inviteCode = generateUniqueInviteCode();

        // 스터디 그룹 이름 설정 (계획 이름 사용)
        String groupName = plan.getLectureName();

        // 스터디 그룹 생성
        StudyGroup studyGroup = StudyGroup.builder()
            .leader(user)
            .basePlan(plan)
            .name(groupName)
            .inviteCode(inviteCode)
            .build();

        StudyGroup savedStudyGroup = studyGroupRepository.save(studyGroup);

        // 방장을 첫 번째 멤버로 추가
        StudyGroupMember leaderMember = StudyGroupMember.builder()
            .studyGroup(savedStudyGroup)
            .user(user)
            .plan(plan)
            .build();

        studyGroupMemberRepository.save(leaderMember);

        return CreateStudyGroupResponse.builder()
            .studyGroupId(savedStudyGroup.getId())
            .inviteCode(inviteCode)
            .name(groupName)
            .build();
    }

    @Override
    public void joinStudyGroup(JoinStudyGroupRequest request, User user) {
        // 초대 코드로 스터디 그룹 찾기
        StudyGroup studyGroup = studyGroupRepository.findByInviteCode(request.getInviteCode())
            .orElseThrow(() -> new ApiException(ExceptionType.STUDY_GROUP_INVALID_INVITE_CODE));

        // 이미 가입했는지 확인
        if (studyGroupMemberRepository.existsByStudyGroupIdAndUserId(studyGroup.getId(), user.getId())) {
            throw new ApiException(ExceptionType.STUDY_GROUP_ALREADY_JOINED);
        }

        // 방장의 계획을 복사해서 새로운 계획 생성
        Plan basePlan = studyGroup.getBasePlan();

        // studyDays를 새로운 HashSet으로 복사
        Set<DayOfWeek> copiedStudyDays = new HashSet<>(basePlan.getStudyDays());

        CreatePlanRequest createPlanRequest = CreatePlanRequest.builder()
            .lectureId(basePlan.getLecture().getId())
            .planType(basePlan.getPlanType())
            .startLessonId(basePlan.getStartLesson().getId())
            .endLessonId(basePlan.getEndLesson().getId())
            .studyDayOfWeeks(copiedStudyDays)
            .dailyTime(basePlan.getDailyTime())
            .startDate(basePlan.getStartDate())
            .endDate(basePlan.getEndDate())
            .playbackSpeed(basePlan.getPlaybackSpeed())
            .build();

        Plan newPlan = planService.createPlan(createPlanRequest, user);

        // 스터디 그룹 멤버로 추가
        StudyGroupMember member = StudyGroupMember.builder()
            .studyGroup(studyGroup)
            .user(user)
            .plan(newPlan)
            .build();

        studyGroupMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyGroupDetailResponse getStudyGroupDetail(Long studyGroupId, User user) {
        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
            .orElseThrow(() -> new ApiException(ExceptionType.STUDY_GROUP_NOT_FOUND));

        // 사용자가 해당 그룹의 멤버인지 확인
        if (!studyGroupMemberRepository.existsByStudyGroupIdAndUserId(studyGroupId, user.getId())) {
            throw new ApiException(ExceptionType.UNAUTHORIZED_ACCESS);
        }

        List<StudyGroupMember> members = studyGroupMemberRepository.findByStudyGroupId(studyGroupId);

        List<StudyGroupMemberDto> memberDtos = members.stream()
            .map(member -> StudyGroupMemberDto.builder()
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .planId(member.getPlan().getId())
                .build())
            .collect(Collectors.toList());

        return StudyGroupDetailResponse.builder()
            .studyGroupId(studyGroup.getId())
            .name(studyGroup.getName())
            .inviteCode(studyGroup.getInviteCode())
            .leaderId(studyGroup.getLeader().getId())
            .leaderName(studyGroup.getLeader().getName())
            .lectureName(studyGroup.getBasePlan().getLectureName())
            .members(memberDtos)
            .build();
    }

    @Override
    public void updateStudyGroupName(Long studyGroupId, UpdateStudyGroupNameRequest request, User user) {
        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
            .orElseThrow(() -> new ApiException(ExceptionType.STUDY_GROUP_NOT_FOUND));

        // 방장인지 확인
        if (!studyGroup.isLeader(user.getId())) {
            throw new ApiException(ExceptionType.STUDY_GROUP_NOT_LEADER);
        }

        studyGroup.updateName(request.getName());
    }

    @Override
    public void kickMember(Long studyGroupId, Long targetUserId, User user) {
        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
            .orElseThrow(() -> new ApiException(ExceptionType.STUDY_GROUP_NOT_FOUND));

        // 방장인지 확인
        if (!studyGroup.isLeader(user.getId())) {
            throw new ApiException(ExceptionType.STUDY_GROUP_NOT_LEADER);
        }

        // 자기 자신을 추방할 수 없음
        if (user.getId().equals(targetUserId)) {
            throw new ApiException(ExceptionType.UNAUTHORIZED_ACCESS, "자기 자신을 추방할 수 없습니다.");
        }

        // 해당 멤버가 그룹에 있는지 확인
        StudyGroupMember member = studyGroupMemberRepository.findByStudyGroupIdAndUserId(studyGroupId, targetUserId)
            .orElseThrow(() -> new ApiException(ExceptionType.USER_NOT_FOUND, "해당 사용자는 이 스터디 그룹의 멤버가 아닙니다."));

        // 멤버 제거
        studyGroupMemberRepository.deleteByStudyGroupIdAndUserId(studyGroupId, targetUserId);

        // 해당 멤버의 계획도 삭제
        planRepository.delete(member.getPlan());
    }

    @Override
    public void deleteStudyGroup(Long studyGroupId, User user) {
        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
            .orElseThrow(() -> new ApiException(ExceptionType.STUDY_GROUP_NOT_FOUND));

        // 방장인지 확인
        if (!studyGroup.isLeader(user.getId())) {
            throw new ApiException(ExceptionType.STUDY_GROUP_NOT_LEADER);
        }

        // 스터디 그룹의 모든 멤버 제거(각 멤버의 계획은 유지)
        List<StudyGroupMember> members = studyGroupMemberRepository.findByStudyGroupId(studyGroupId);
        studyGroupMemberRepository.deleteAll(members);

        // 스터디 그룹 삭제
        studyGroupRepository.delete(studyGroup);
    }

    @Override
    public void transferLeader(Long studyGroupId, Long newLeaderId, User user) {
        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
            .orElseThrow(() -> new ApiException(ExceptionType.STUDY_GROUP_NOT_FOUND));

        // 방장인지 확인
        if (!studyGroup.isLeader(user.getId())) {
            throw new ApiException(ExceptionType.STUDY_GROUP_NOT_LEADER);
        }

        // 새로운 방장이 그룹의 멤버인지 확인
        if (!studyGroupMemberRepository.existsByStudyGroupIdAndUserId(studyGroupId, newLeaderId)) {
            throw new ApiException(ExceptionType.USER_NOT_FOUND, "새로운 방장은 이 스터디 그룹의 멤버가 아닙니다.");
        }

        // 방장 변경
        User newLeader = User.builder().id(newLeaderId).build();
        studyGroup.updateLeader(newLeader);
    }

    private String generateUniqueInviteCode() {
        Random random = new Random();
        String inviteCode;

        do {
            inviteCode = String.format("%04d", random.nextInt(10000));
        } while (studyGroupRepository.existsByInviteCode(inviteCode));

        return inviteCode;
    }
}
