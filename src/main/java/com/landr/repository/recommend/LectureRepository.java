package com.landr.repository.recommend;

import com.landr.domain.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    @Query(value = "SELECT COUNT(*) FROM lectures", nativeQuery = true)
    Long countAllNative();

    @Query(value = "SELECT * FROM lectures LIMIT 5", nativeQuery = true)
    List<Object[]> findRawLectures();

    @Query(value = "SELECT id, title, teacher, platform, subject, tag FROM lectures LIMIT 3", nativeQuery = true)
    List<Object[]> findBasicColumns();
}