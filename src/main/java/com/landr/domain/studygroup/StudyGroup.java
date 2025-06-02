package com.landr.domain.studygroup;

import com.landr.domain.plan.Plan;
import com.landr.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "study_groups")
@Getter
public class StudyGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User leader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_plan_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Plan basePlan;

    @Column(nullable = false)
    private String name;

    @Column(name = "invite_code", nullable = false, unique = true)
    private String inviteCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void updateName(String newName) {
        if (newName != null && !newName.isBlank()) {
            this.name = newName;
        }
    }

    public boolean isLeader(Long userId) {
        return this.leader.getId().equals(userId);
    }

    public void updateLeader(User newLeader) {
        if (newLeader == null) {
            throw new IllegalArgumentException("새로운 리더는 null일 수 없습니다.");
        }

        this.leader = newLeader;
    }
}