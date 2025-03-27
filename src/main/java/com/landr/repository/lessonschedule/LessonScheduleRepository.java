package com.landr.repository.lessonschedule;

import com.landr.domain.schedule.LessonSchedule;
import java.util.List;
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
}
