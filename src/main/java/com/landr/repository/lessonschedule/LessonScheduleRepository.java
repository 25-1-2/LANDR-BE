package com.landr.repository.lessonschedule;

import com.landr.domain.schedule.LessonSchedule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {

    /**
     * 여러 일정에 속한 수업 일정을 조회합니다. Lesson 정보와 Lecture 정보도 함께 가져옵니다.
     */
    @Query("SELECT ls FROM LessonSchedule ls " +
        "JOIN FETCH ls.lesson l " +
        "JOIN FETCH l.lecture lec " +
        "WHERE ls.dailySchedule.id IN :dailyScheduleIds " +
        "ORDER BY ls.dailySchedule.id, ls.displayOrder")
    List<LessonSchedule> findByDailyScheduleIdsWithLessonAndLecture(
        @Param("dailyScheduleIds") List<Long> dailyScheduleIds
    );

    /**
     * 특정 사용자의 모든 강의별 수업 일정을 조회합니다.
     */
    @Query("SELECT ls FROM LessonSchedule ls " +
        "JOIN FETCH ls.lesson l " +
        "JOIN FETCH l.lecture lec " +
        "JOIN FETCH ls.dailySchedule ds " +  // DailySchedule 페치 조인 추가
        "JOIN FETCH ds.plan p " +            // Plan 페치 조인 추가
        "WHERE p.user.id = :userId " +
        "ORDER BY lec.id, l.order")
    List<LessonSchedule> findAllByUserIdGroupedByLecture(@Param("userId") Long userId);


    @Query("SELECT ls FROM LessonSchedule ls " +
        "JOIN ls.dailySchedule ds " +
        "JOIN ds.plan p " +
        "JOIN p.user u " +
        "WHERE ls.id = :lsId "
        + "AND p.user.id = :userId"
    )
    Optional<LessonSchedule> findByIdAndUserId(@Param("lsId") Long lessonScheduleId,
        @Param("userId") Long userId);

    /**
     * planId에 해당하는 완료된 lessonSchedule의 개수를 조회합니다.
     */
    @Query("SELECT COUNT(ls) FROM LessonSchedule ls " +
        "JOIN ls.dailySchedule ds " +
        "JOIN ds.plan p " +
        "WHERE p.id = :planId AND ls.completed = true")
    Long countCompletedLessonSchedulesByPlanId(@Param("planId") Long planId);

    /**
     * 특정 사용자의 특정 계획에 속한 수업 일정을 조회합니다.
     */
    @Query("SELECT ls FROM LessonSchedule ls " +
        "JOIN FETCH ls.lesson l " +
        "JOIN FETCH ls.dailySchedule ds " +
        "JOIN FETCH ds.plan p " +
        "JOIN FETCH p.user u " +  // User 페치 조인 추가
        "WHERE p.user.id = :userId " +
        "AND p.id = :planId " +
        "ORDER BY l.order")
    List<LessonSchedule> findByPlanIdAndUserId(@Param("userId") Long userId, @Param("planId") Long planId);

    /**
     * 특정 사용자가 특정 날짜에 완료한 레슨이 있는지 확인
     */
    @Query("SELECT COUNT(ls) > 0 FROM LessonSchedule ls " +
        "JOIN ls.dailySchedule ds " +
        "JOIN ds.plan p " +
        "WHERE p.user.id = :userId " +
        "AND ls.completed = true " +
        "AND ls.updatedAt BETWEEN :startDateTime AND :endDateTime")
    boolean existsCompletedLessonOnDate(
        @Param("userId") Long userId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );
}
