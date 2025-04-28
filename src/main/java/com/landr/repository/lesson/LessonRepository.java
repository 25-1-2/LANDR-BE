package com.landr.repository.lesson;

import com.landr.domain.lecture.Lesson;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * 특정 강의의 시작 Lesson order부터 종료 Lesson order까지의 모든 Lesson을 조회합니다.
     *
     * @param lectureId  강의 ID
     * @param startOrder 시작 순서
     * @param endOrder   종료 순서
     * @return 순서대로 정렬된 Lesson 목록
     */
    @Query("SELECT l FROM Lesson l "
        + "WHERE l.lecture.id = :lectureId "
        + "AND l.order BETWEEN :startOrder AND :endOrder "
        + "ORDER BY l.order")
    List<Lesson> findByLectureIdAndOrderBetweenOrderByOrder(
        @Param("lectureId") Long lectureId,
        @Param("startOrder") int startOrder,
        @Param("endOrder") int endOrder);
}
