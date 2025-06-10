package com.landr.service.studygroup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.studygroup.dto.*;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import com.landr.domain.plan.PlanType;
import com.landr.domain.studygroup.StudyGroup;
import com.landr.domain.studygroup.StudyGroupMember;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.plan.PlanRepository;
import com.landr.repository.studygroup.StudyGroupMemberRepository;
import com.landr.repository.studygroup.StudyGroupRepository;
import com.landr.service.plan.PlanService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudyGroupMemberRepository studyGroupMemberRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanService planService;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    private User leader;
    private User member;
    private Plan basePlan;
    private StudyGroup studyGroup;
    private Lecture lecture;
    private Lesson startLesson;
    private Lesson endLesson;

    @BeforeEach
    void setUp() {
        leader = User.builder().id(1L).name("Leader").build();
        member = User.builder().id(2L).name("Member").build();

        lecture = Lecture.builder().id(1L).title("Test Lecture").build();
        startLesson = Lesson.builder().id(1L).order(1).build();
        endLesson = Lesson.builder().id(10L).order(10).build();

        Set<DayOfWeek> studyDays = new HashSet<>(
            Arrays.asList(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI));

        basePlan = Plan.builder()
            .id(1L)
            .user(leader)
            .lecture(lecture)
            .lectureName("Test Lecture")
            .startLesson(startLesson)
            .endLesson(endLesson)
            .planType(PlanType.PERIOD)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .studyDays(studyDays)
            .playbackSpeed(1.5f)
            .build();

        studyGroup = StudyGroup.builder()
            .id(1L)
            .leader(leader)
            .basePlan(basePlan)
            .name("Test Study Group")
            .inviteCode("1234")
            .build();
    }

    @Test
    @DisplayName("스터디 그룹 생성 성공")
    void createStudyGroup_Success() {
        // Given
        when(planRepository.findByIdAndUserId(basePlan.getId(), leader.getId()))
            .thenReturn(Optional.of(basePlan));
        when(studyGroupRepository.findByLeaderIdAndBasePlanId(leader.getId(), basePlan.getId()))
            .thenReturn(Optional.empty());
        when(studyGroupRepository.existsByInviteCode(anyString())).thenReturn(false);
        when(studyGroupRepository.save(any(StudyGroup.class))).thenAnswer(invocation -> {
            StudyGroup sg = invocation.getArgument(0);
            return StudyGroup.builder()
                .id(1L)
                .leader(sg.getLeader())
                .basePlan(sg.getBasePlan())
                .name(sg.getName())
                .inviteCode(sg.getInviteCode())
                .build();
        });
        when(studyGroupMemberRepository.save(any(StudyGroupMember.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CreateStudyGroupResponse result = studyGroupService.createStudyGroup(basePlan.getId(),
            leader);

        // Then
        assertNotNull(result);
        assertNotNull(result.getInviteCode());
        assertEquals(basePlan.getLectureName(), result.getName());
        verify(studyGroupRepository, times(1)).save(any(StudyGroup.class));
        verify(studyGroupMemberRepository, times(1)).save(any(StudyGroupMember.class));
    }

    @Test
    @DisplayName("스터디 그룹 생성 실패 - 이미 존재하는 스터디 그룹")
    void createStudyGroup_AlreadyExists() {
        // Given
        when(planRepository.findByIdAndUserId(basePlan.getId(), leader.getId()))
            .thenReturn(Optional.of(basePlan));
        when(studyGroupRepository.findByLeaderIdAndBasePlanId(leader.getId(), basePlan.getId()))
            .thenReturn(Optional.of(studyGroup));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> studyGroupService.createStudyGroup(basePlan.getId(), leader));

        assertEquals(ExceptionType.STUDY_GROUP_ALREADY_EXISTS, exception.getExceptionType());
    }

    @Test
    @DisplayName("스터디 그룹 가입 성공")
    void joinStudyGroup_Success() {
        // Given
        JoinStudyGroupRequest request = JoinStudyGroupRequest.builder()
            .inviteCode("1234")
            .build();

        Plan newPlan = Plan.builder()
            .id(2L)
            .user(member)
            .lecture(lecture)
            .build();

        when(studyGroupRepository.findByInviteCode(request.getInviteCode()))
            .thenReturn(Optional.of(studyGroup));
        when(studyGroupMemberRepository.existsByStudyGroupIdAndUserId(studyGroup.getId(),
            member.getId()))
            .thenReturn(false);
        when(planService.createPlan(any(CreatePlanRequest.class), eq(member)))
            .thenReturn(newPlan);
        when(studyGroupMemberRepository.save(any(StudyGroupMember.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> studyGroupService.joinStudyGroup(request, member));

        // Then
        verify(planService, times(1)).createPlan(any(CreatePlanRequest.class), eq(member));
        verify(studyGroupMemberRepository, times(1)).save(any(StudyGroupMember.class));
    }

    @Test
    @DisplayName("스터디 그룹 가입 실패 - 잘못된 초대 코드")
    void joinStudyGroup_InvalidInviteCode() {
        // Given
        JoinStudyGroupRequest request = JoinStudyGroupRequest.builder()
            .inviteCode("9999")
            .build();

        when(studyGroupRepository.findByInviteCode(request.getInviteCode()))
            .thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> studyGroupService.joinStudyGroup(request, member));

        assertEquals(ExceptionType.STUDY_GROUP_INVALID_INVITE_CODE, exception.getExceptionType());
    }

    @Test
    @DisplayName("스터디 그룹 이름 수정 성공")
    void updateStudyGroupName_Success() {
        // Given
        UpdateStudyGroupNameRequest request = UpdateStudyGroupNameRequest.builder()
            .name("Updated Study Group Name")
            .build();
        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));

        // When
        assertDoesNotThrow(() -> studyGroupService.updateStudyGroupName(studyGroup.getId(), request, leader));

        // Then
        verify(studyGroupRepository, times(1)).findById(studyGroup.getId());
    }

    @Test
    @DisplayName("스터디 그룹 이름 수정 실패 - 방장이 아님")
    void updateStudyGroupName_NotLeader() {
        // Given
        UpdateStudyGroupNameRequest request = UpdateStudyGroupNameRequest.builder()
            .name("Updated Study Group Name")
            .build();

        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> studyGroupService.updateStudyGroupName(studyGroup.getId(), request, member));

        assertEquals(ExceptionType.STUDY_GROUP_NOT_LEADER, exception.getExceptionType());
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 성공")
    void kickMember_Success() {
        // Given
        StudyGroupMember memberToKick = StudyGroupMember.builder()
            .id(1L)
            .studyGroup(studyGroup)
            .user(member)
            .plan(basePlan)
            .build();

        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));
        when(studyGroupMemberRepository.findByStudyGroupIdAndUserId(studyGroup.getId(), member.getId()))
            .thenReturn(Optional.of(memberToKick));
        doNothing().when(studyGroupMemberRepository).deleteByStudyGroupIdAndUserId(anyLong(), anyLong());
        doNothing().when(planRepository).delete(any(Plan.class));

        // When
        assertDoesNotThrow(() -> studyGroupService.kickMember(studyGroup.getId(), member.getId(), leader));

        // Then
        verify(studyGroupMemberRepository, times(1)).deleteByStudyGroupIdAndUserId(studyGroup.getId(), member.getId());
        verify(planRepository, times(1)).delete(memberToKick.getPlan());
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 실패 - 자기 자신을 추방")
    void kickMember_SelfKick() {
        // Given
        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> studyGroupService.kickMember(studyGroup.getId(), leader.getId(), leader));

        assertEquals(ExceptionType.UNAUTHORIZED_ACCESS, exception.getExceptionType());
    }

    @Test
    @DisplayName("스터디 그룹 상세 조회 성공")
    void getStudyGroupDetail_Success() {
        // Given
        StudyGroupMember leaderMember = StudyGroupMember.builder()
            .id(1L)
            .studyGroup(studyGroup)
            .user(leader)
            .plan(basePlan)
            .build();

        StudyGroupMember normalMember = StudyGroupMember.builder()
            .id(2L)
            .studyGroup(studyGroup)
            .user(member)
            .plan(basePlan)
            .build();

        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));
        when(studyGroupMemberRepository.existsByStudyGroupIdAndUserId(studyGroup.getId(), leader.getId()))
            .thenReturn(true);
        when(studyGroupMemberRepository.findByStudyGroupId(studyGroup.getId()))
            .thenReturn(Arrays.asList(leaderMember, normalMember));

        // When
        StudyGroupDetailResponse result = studyGroupService.getStudyGroupDetail(studyGroup.getId(), leader);

        // Then
        assertNotNull(result);
        assertEquals(studyGroup.getId(), result.getStudyGroupId());
        assertEquals(studyGroup.getName(), result.getName());
        assertEquals(2, result.getMembers().size());
    }

    @Test
    @DisplayName("스터디 그룹 삭제 성공")
    void deleteStudyGroup_Success() {
        // Given
        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));
        when(studyGroupMemberRepository.findByStudyGroupId(studyGroup.getId()))
            .thenReturn(Arrays.asList());
        doNothing().when(studyGroupRepository).delete(any(StudyGroup.class));

        // When
        assertDoesNotThrow(() -> studyGroupService.deleteStudyGroup(studyGroup.getId(), leader));

        // Then
        verify(studyGroupRepository, times(1)).delete(studyGroup);
    }

    @Test
    @DisplayName("방장 위임 성공")
    void transferLeader_Success() {
        // Given
        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));
        when(studyGroupMemberRepository.existsByStudyGroupIdAndUserId(studyGroup.getId(), member.getId()))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> studyGroupService.transferLeader(studyGroup.getId(), member.getId(), leader));

        // Then
        verify(studyGroupRepository, times(1)).findById(studyGroup.getId());
    }

    @Test
    @DisplayName("방장 위임 실패 - 새 방장이 멤버가 아님")
    void transferLeader_NewLeaderNotMember() {
        // Given
        Long nonMemberId = 999L;
        when(studyGroupRepository.findById(studyGroup.getId()))
            .thenReturn(Optional.of(studyGroup));
        when(studyGroupMemberRepository.existsByStudyGroupIdAndUserId(studyGroup.getId(), nonMemberId))
            .thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> studyGroupService.transferLeader(studyGroup.getId(), nonMemberId, leader));

        assertEquals(ExceptionType.USER_NOT_FOUND, exception.getExceptionType());
    }
}