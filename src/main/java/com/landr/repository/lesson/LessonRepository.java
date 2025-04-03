package com.landr.repository.lesson;

import com.landr.domain.lecture.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
