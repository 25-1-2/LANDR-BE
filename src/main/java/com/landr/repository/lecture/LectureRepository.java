package com.landr.repository.lecture;

import com.landr.domain.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long>, LectureRepositoryCustom {
}
