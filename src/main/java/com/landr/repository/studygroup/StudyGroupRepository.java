package com.landr.repository.studygroup;

import com.landr.domain.studygroup.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    Optional<StudyGroup> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    @Query("SELECT sg FROM StudyGroup sg WHERE sg.leader.id = :leaderId AND sg.basePlan.id = :planId")
    Optional<StudyGroup> findByLeaderIdAndBasePlanId(Long leaderId, Long planId);
}