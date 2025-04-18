package com.landr.domain.lecture;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lectures")
@Getter
public class Lecture {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subject subject;

    @Column(name = "total_lessons", nullable = false)
    private int totalLessons;

    @Column(name = "total_duration", nullable = false)
    private int totalDuration;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private String tag;
}