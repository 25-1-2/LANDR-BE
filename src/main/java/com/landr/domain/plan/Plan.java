package com.landr.domain.plan;


import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.domain.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "plans")
public class Plan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Lecture lecture;

    @Column(name = "lecture_name", nullable = false)
    private String lectureName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_lesson_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Lesson startLesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_lesson_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Lesson endLesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "daily_time")
    private Integer dailyTime;

    @Column(name = "playback_speed", nullable = false)
    private Float playbackSpeed = 1.0f;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(
        name = "plan_study_days",
        joinColumns = @JoinColumn(name = "plan_id")
    )
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> studyDays = new HashSet<>();

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isDeleted = false;
    }

    // 유저별 강의명 수정
    public void editLectureName(String lectureName) {
        this.lectureName = lectureName;
    }

    public void delete() {
        isDeleted = true;
    }
}