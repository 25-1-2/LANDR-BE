package com.landr.domain.studygroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.landr.domain.plan.Plan;
import com.landr.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StudyGroupTest {

    private StudyGroup studyGroup;
    private User leader;
    private User member;
    private Plan basePlan;

    @BeforeEach
    void setUp() {
        leader = User.builder().id(1L).name("Leader").build();
        member = User.builder().id(2L).name("Member").build();

        basePlan = Plan.builder()
            .id(1L)
            .lectureName("Test Lecture")
            .build();

        studyGroup = StudyGroup.builder()
            .id(1L)
            .leader(leader)
            .basePlan(basePlan)
            .name("Study Group 1")
            .inviteCode("1234")
            .build();
    }

    @Test
    @DisplayName("스터디 그룹 이름 업데이트 성공")
    void updateName_Success() {
        // Given
        String newName = "Updated Study Group";

        // When
        studyGroup.updateName(newName);

        // Then
        assertEquals(newName, studyGroup.getName());
    }

    @Test
    @DisplayName("스터디 그룹 이름 업데이트 - null 무시")
    void updateName_Null() {
        // Given
        String originalName = studyGroup.getName();

        // When
        studyGroup.updateName(null);

        // Then
        assertEquals(originalName, studyGroup.getName());
    }

    @Test
    @DisplayName("스터디 그룹 이름 업데이트 - 빈 문자열 무시")
    void updateName_Empty() {
        // Given
        String originalName = studyGroup.getName();

        // When
        studyGroup.updateName("");

        // Then
        assertEquals(originalName, studyGroup.getName());
    }

    @Test
    @DisplayName("방장 확인 - true")
    void isLeader_True() {
        // When
        boolean result = studyGroup.isLeader(leader.getId());

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("방장 확인 - false")
    void isLeader_False() {
        // When
        boolean result = studyGroup.isLeader(member.getId());

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("방장 업데이트 성공")
    void updateLeader_Success() {
        // When
        studyGroup.updateLeader(member);

        // Then
        assertEquals(member, studyGroup.getLeader());
        assertTrue(studyGroup.isLeader(member.getId()));
        assertFalse(studyGroup.isLeader(leader.getId()));
    }

    @Test
    @DisplayName("방장 업데이트 실패 - null")
    void updateLeader_Null() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> studyGroup.updateLeader(null));

        assertEquals("새로운 리더는 null일 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("onCreate 메서드 테스트")
    void onCreate_Test() {
        // Given
        StudyGroup newStudyGroup = StudyGroup.builder()
            .leader(leader)
            .basePlan(basePlan)
            .name("New Group")
            .inviteCode("5678")
            .build();

        // When
        newStudyGroup.onCreate();

        // Then
        assertNotNull(newStudyGroup.getCreatedAt());
    }
}