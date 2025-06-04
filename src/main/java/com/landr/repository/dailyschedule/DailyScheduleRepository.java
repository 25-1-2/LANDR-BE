package com.landr.repository.dailyschedule;

import com.landr.domain.schedule.DailySchedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyScheduleRepository extends JpaRepository<DailySchedule, Long> {

    /**
     * 특정 유저의 특정 날짜 일정을 조회합니다.
     */
    @Query("SELECT ds FROM DailySchedule ds " +
        "JOIN ds.plan p " +
        "WHERE p.user.id = :userId " +
        "AND ds.date = :date")
    List<DailySchedule> findByUserIdAndDate(
        @Param("userId") Long userId,
        @Param("date") LocalDate date
    );

    /**
     * 특정 유저의 특정 계획에 속한 모든 일정을 조회합니다.
     */
    @Query("SELECT ds FROM DailySchedule ds " +
        "JOIN ds.plan p " +
        "WHERE p.user.id = :userId " +
        "AND p.id = :planId")
    List<DailySchedule> findByUserIdAndPlanId(
        @Param("userId") Long userId,
        @Param("planId") Long planId
    );

    /**
     * 특정 계획에 속한 모든 일정을 조회합니다.
     */
    @Query("SELECT ds FROM DailySchedule ds " +
        "JOIN ds.plan p " +
        "WHERE p.id = :planId")
    List<DailySchedule> findByPlanId(@Param("planId") Long planId);

    @Query("SELECT DISTINCT ds FROM DailySchedule ds " +
        "JOIN ds.plan p " +
        "WHERE p.id = :planId " +
        "AND EXISTS (SELECT 1 FROM LessonSchedule ls WHERE ls.dailySchedule = ds AND ls.completed = false)")
    List<DailySchedule> findDailySchedulesWithUncompletedLessons(@Param("planId") Long planId);
}
