package com.landr.domain.lecture;

import com.landr.domain.Platform;
import com.landr.domain.Subject;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lectures")
public class Lecture {
    @Id
    private String id;

    private String title;

    private String teacher;

    // 플랫폼(메가, 이투스 등)
    private Platform platform;
    // 과목(국어, 수학 등)
    private Subject subject;

    // 총 레슨 수
    private int totalLessons;
    // 총 강의 시간
    private int totalDuration;

    private String tag;

    private List<Lesson> lessons;

    private LocalDateTime createdAt;
}
