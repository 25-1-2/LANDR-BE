package com.landr.controller.studygroup;

import com.landr.controller.CommonResponse;
import com.landr.controller.studygroup.dto.CreateStudyGroupResponse;
import com.landr.controller.studygroup.dto.JoinStudyGroupRequest;
import com.landr.controller.studygroup.dto.StudyGroupDetailResponse;
import com.landr.controller.studygroup.dto.UpdateStudyGroupNameRequest;
import com.landr.domain.user.User;
import com.landr.service.studygroup.StudyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/study-groups")
@AllArgsConstructor
@Tag(name = "StudyGroup", description = "스터디 그룹 관련 API")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @Operation(summary = "스터디 그룹 생성", description = "계획을 기반으로 스터디 그룹을 생성합니다.")
    @PostMapping("/plans/{planId}")
    public ResponseEntity<CreateStudyGroupResponse> createStudyGroup(
        @PathVariable Long planId,
        @AuthenticationPrincipal User user
    ) {
        CreateStudyGroupResponse response = studyGroupService.createStudyGroup(planId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 그룹 가입", description = "초대 코드를 통해 스터디 그룹에 가입합니다.")
    @PostMapping("/join")
    public ResponseEntity<CommonResponse> joinStudyGroup(
        @RequestBody @Valid JoinStudyGroupRequest request,
        @AuthenticationPrincipal User user
    ) {
        studyGroupService.joinStudyGroup(request, user);
        return ResponseEntity.ok(
            CommonResponse.builder()
                .message("그룹에 성공적으로 가입했습니다.")
                .build()
        );
    }

    @Operation(summary = "스터디 그룹 상세 조회", description = "스터디 그룹의 상세 정보를 조회합니다.")
    @GetMapping("/{studyGroupId}")
    public ResponseEntity<StudyGroupDetailResponse> getStudyGroupDetail(
        @PathVariable Long studyGroupId,
        @AuthenticationPrincipal User user
    ) {
        StudyGroupDetailResponse response = studyGroupService.getStudyGroupDetail(studyGroupId,
            user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 그룹 이름 수정", description = "스터디 그룹의 이름을 수정합니다. (방장만 가능)")
    @PatchMapping("/{studyGroupId}/name")
    public ResponseEntity<CommonResponse> updateStudyGroupName(
        @PathVariable Long studyGroupId,
        @RequestBody @Valid UpdateStudyGroupNameRequest request,
        @AuthenticationPrincipal User user
    ) {
        studyGroupService.updateStudyGroupName(studyGroupId, request, user);
        return ResponseEntity.ok(
            CommonResponse.builder()
                .message("스터디 그룹 이름이 수정되었습니다.")
                .build()
        );
    }

    @Operation(summary = "스터디 그룹 멤버 추방", description = "스터디 그룹에서 멤버를 추방합니다. (방장만 가능)")
    @DeleteMapping("/{studyGroupId}/members/{targetUserId}")
    public ResponseEntity<CommonResponse> kickMember(
        @PathVariable Long studyGroupId,
        @PathVariable Long targetUserId,
        @AuthenticationPrincipal User user
    ) {
        studyGroupService.kickMember(studyGroupId, targetUserId, user);
        return ResponseEntity.ok(
            CommonResponse.builder()
                .message("멤버가 추방되었습니다.")
                .build()
        );
    }

    @Operation(summary = "스터디 그룹 삭제", description = "스터디 그룹을 삭제합니다. (방장만 가능)")
    @DeleteMapping("/{studyGroupId}")
    public ResponseEntity<CommonResponse> deleteStudyGroup(
        @PathVariable Long studyGroupId,
        @AuthenticationPrincipal User user
    ) {
        studyGroupService.deleteStudyGroup(studyGroupId, user);
        return ResponseEntity.ok(
            CommonResponse.builder()
                .message("스터디 그룹이 삭제되었습니다.")
                .build()
        );
    }

    @Operation(summary = "방장 위임", description = "스터디 그룹의 방장을 다른 멤버로 위임합니다. (방장만 가능)")
    @PatchMapping("/{studyGroupId}/leader/{newLeaderId}")
    public ResponseEntity<CommonResponse> transferLeader(
        @PathVariable Long studyGroupId,
        @PathVariable Long newLeaderId,
        @AuthenticationPrincipal User user
    ) {
        studyGroupService.transferLeader(studyGroupId, newLeaderId, user);
        return ResponseEntity.ok(
            CommonResponse.builder()
                .message("방장이 성공적으로 변경되었습니다.")
                .build()
        );
    }
}