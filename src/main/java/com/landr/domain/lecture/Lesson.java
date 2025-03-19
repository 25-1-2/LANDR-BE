package com.landr.domain.lecture;

import jakarta.persistence.Id;

public class Lesson {
    @Id
    private String id;

    // 강의 순서
    private int sequence;
    // 강의 제목
    private String title;
    // 강의 시간
    private int duration;
}
