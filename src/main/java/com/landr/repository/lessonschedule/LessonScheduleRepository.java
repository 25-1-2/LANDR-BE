package com.landr.repository.lessonschedule;

import com.landr.domain.schedule.LessonSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {

    // 특정 DailySchedule에 속한 LessonSchedule 목록 조회
    @Query("SELECT ls FROM LessonSchedule ls " +
        "WHERE ls.dailySchedule.id = :dailyScheduleId " +
        "ORDER BY ls.displayOrder")
    List<LessonSchedule> findByDailyScheduleIdOrderByDisplayOrder(
        @Param("dailyScheduleId") Long dailyScheduleId
    );

}
