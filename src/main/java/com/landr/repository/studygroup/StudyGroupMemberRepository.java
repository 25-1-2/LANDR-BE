package com.landr.repository.studygroup;

import com.landr.domain.studygroup.StudyGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, Long> {

    @Query("SELECT sgm FROM StudyGroupMember sgm WHERE sgm.studyGroup.id = :studyGroupId")
    List<StudyGroupMember> findByStudyGroupId(Long studyGroupId);

    @Query("SELECT sgm FROM StudyGroupMember sgm WHERE sgm.studyGroup.id = :studyGroupId AND sgm.user.id = :userId")
    Optional<StudyGroupMember> findByStudyGroupIdAndUserId(Long studyGroupId, Long userId);

    boolean existsByStudyGroupIdAndUserId(Long studyGroupId, Long userId);

    void deleteByStudyGroupIdAndUserId(Long studyGroupId, Long userId);

    // 특정 유저의 모든 스터디 그룹 계획 ID 조회
    @Query("SELECT sgm.plan.id FROM StudyGroupMember sgm WHERE sgm.user.id = :userId")
    List<Long> findPlanIdsByUserId(Long userId);

    // Plan ID로 스터디 그룹 ID 조회
    @Query("SELECT sgm.studyGroup.id FROM StudyGroupMember sgm WHERE sgm.plan.id = :planId")
    Optional<Long> findStudyGroupIdByPlanId(Long planId);
}