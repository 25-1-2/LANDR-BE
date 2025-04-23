package com.landr.domain.schedule;


import com.landr.domain.plan.DayOfWeek;
import com.landr.domain.plan.Plan;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_schedules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"plan_id", "date"})
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Plan plan;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "total_lessons", nullable = false)
    private int totalLessons;

    @Column(name = "total_duration", nullable = false)
    private int totalDuration;

    @Override
    public String toString() {
        return "DailySchedule{" +
            "id=" + id +
            ", date=" + date +
            ", dayOfWeek=" + dayOfWeek +
            ", totalLessons=" + totalLessons +
            ", totalDuration=" + totalDuration +
            '}';
    }
}
