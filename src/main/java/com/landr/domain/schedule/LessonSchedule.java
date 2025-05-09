package com.landr.domain.schedule;

import com.landr.domain.lecture.Lesson;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lesson_schedules")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_schedule_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private DailySchedule dailySchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Lesson lesson;

    @Column(name = "adjusted_duration", nullable = false)
    private int adjustedDuration;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime updatedAt;

    public boolean toggleCheck() {
        this.completed = !this.completed;
        this.updatedAt = LocalDateTime.now();
        return this.completed;
    }

    @Override
    public String toString() {
        return "LessonSchedule{" +
            "id=" + id +
            ", dailySchedule=" + dailySchedule +
            ", lesson=" + lesson +
            ", adjustedDuration=" + adjustedDuration +
            ", displayOrder=" + displayOrder +
            ", completed=" + completed +
            '}';
    }
}