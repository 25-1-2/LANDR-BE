package com.landr.domain.plan;

import java.time.LocalDateTime;

// 내부 클래스: 계획된 레슨
public class LessonSchedule {
    // 레슨 ID 참조
    private String lessonId;

    // 레슨 제목
    private String title;

    // 레슨 원본 길이(분)
    private int originalDuration;

    // 배속 적용된 학습 시간(분)
    private int adjustedDuration;

    // 정렬 순서 (사용자가 정의한 순서)
    private int order;

    // 완료 여부
    private boolean completed;

    // 완료 시간
    private LocalDateTime completedAt;
}
