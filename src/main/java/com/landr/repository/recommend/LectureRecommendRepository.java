package com.landr.repository.recommend;

import com.landr.domain.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRecommendRepository extends JpaRepository<Lecture, Long> {

    // 기존 디버깅용 메서드들
    @Query(value = "SELECT COUNT(*) FROM lectures", nativeQuery = true)
    Long countAllNative();

    @Query(value = "SELECT * FROM lectures LIMIT 5", nativeQuery = true)
    List<Object[]> findRawLectures();

    @Query(value = "SELECT id, title, teacher, platform, subject, tag FROM lectures LIMIT 3", nativeQuery = true)
    List<Object[]> findBasicColumns();


    // 과목별 강의 조회 (키워드 매칭)
    @Query("SELECT l FROM Lecture l WHERE " +
            "l.tag IS NOT NULL AND " +
            "(LOWER(l.title) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
            " LOWER(l.tag) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
            " LOWER(l.subject) LIKE LOWER(CONCAT('%', :subject, '%')))")
    List<Lecture> findBySubjectKeyword(@Param("subject") String subject);

    // 과목별 개수 확인
    @Query("SELECT COUNT(l) FROM Lecture l WHERE " +
            "l.tag IS NOT NULL AND " +
            "(LOWER(l.title) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
            " LOWER(l.tag) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
            " LOWER(l.subject) LIKE LOWER(CONCAT('%', :subject, '%')))")
    Long countBySubjectKeyword(@Param("subject") String subject);

    // 과목 + 학년별 조회 (선택적 학년 필터링)
    @Query("SELECT l FROM Lecture l WHERE " +
            "l.tag IS NOT NULL AND " +
            "(LOWER(l.title) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
            " LOWER(l.tag) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
            " LOWER(l.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) AND " +
            "(:grade = '' OR LOWER(l.tag) LIKE LOWER(CONCAT('%', :grade, '%')))")
    List<Lecture> findBySubjectAndGrade(@Param("subject") String subject, @Param("grade") String grade);
}
