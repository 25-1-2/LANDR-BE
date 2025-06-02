package com.landr.domain.dday;

import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ddays")
@Getter
public class DDay {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    private String title;

    private LocalDate goalDate;

    public void update(String title, LocalDate goalDate) {
        if (isValidTitle(title)) {
            this.title = title;
        }
        if (isValidGoalDate(goalDate)) {
            this.goalDate = goalDate;
        }
    }

    public void isOwner(Long userId) {
        if (!this.user.getId().equals(userId)) {
            throw new ApiException(ExceptionType.DDAY_OWNER_NOT_MATCH);
        }
    }

    private boolean isValidTitle(String title) {
        return title != null && !title.isBlank();
    }

    private boolean isValidGoalDate(LocalDate goalDate) {
        return goalDate != null && !goalDate.isBefore(LocalDate.now());
    }


}
