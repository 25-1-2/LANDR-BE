package com.landr.repository.dailyschedule;

import com.landr.domain.schedule.DailySchedule;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DailyScheduleRepository extends JpaRepository<DailySchedule, Long> {

    @Query("select ds from DailySchedule  ds "
        + "join ds.plan p "
        + "where p.user.id = :userId "
        + "and ds.date = :date")
    Optional<DailySchedule> findByUserIdAndDate(Long userId, LocalDate date);
}
