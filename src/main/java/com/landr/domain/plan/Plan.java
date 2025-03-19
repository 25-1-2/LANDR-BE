package com.landr.domain.plan;

import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "plans")
public class Plan {
    @Id
    private String id;

    // 사용자 ID 참조
    @Indexed
    private Long userId;

    // 강의 ID 참조
    @Indexed
    private String lectureId;

    // 시작 레슨 ID
    private String startLessonId;

    // 끝 레슨 ID
    private String endLessonId;

    // 계획 유형
    private PlanType planType;

    // 계획 시작일 (기간으로 계획할 때)
    private LocalDate startDate;

    // 계획 종료일 (기간으로 계획할 때)
    private LocalDate endDate;

    // 하루 학습 시간 (시간으로 계획할 때, 분 단위)
    private Integer dailyTime;

    // 학습 요일 (월,화,수,목,금,토,일)
    private List<String> studyDays;

    // 배속 설정(1.0 ~ 2.0 / 0.1 단위)
    private Float playbackRate;

    // 일별 계획 목록
    private List<DailySchedule> dailySchedules;

    // 계획 생성 시간
    private LocalDateTime createdAt;
}
