package com.landr.domain.plan;

import java.time.LocalDate;
import java.util.List;

public class DailySchedule {
    // 날짜
    private LocalDate date;

    // 요일
    private String dayOfWeek;

    // 해당 일자 계획된 레슨 수
    private int totalLessons;

    // 해당 일자의 총 학습 시간(분, 배속 적용됨)
    private int totalDuration;

    // 해당 일자 완료 여부 (모든 레슨이 완료되면 true)
    private boolean completed;

    // 계획된 레슨 목록
    private List<LessonSchedule> lessons;

}
