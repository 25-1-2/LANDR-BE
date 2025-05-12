package com.landr.service.lessonschedule;

import com.landr.domain.schedule.LessonSchedule;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LessonScheduleService {

    private final LessonScheduleRepository lessonScheduleRepository;

    @Transactional
    public Boolean toggleCheck(Long lessonScheduleId, Long userId) {
        LessonSchedule lessonSchedule = lessonScheduleRepository.findByIdAndUserId(lessonScheduleId,
            userId).orElseThrow(() -> new ApiException(ExceptionType.LESSON_SCHEDULE_NOT_FOUND));

        return lessonSchedule.toggleCheck();
    }

    /**
     * 이번주 학습 성취율 조회
     * 이번주(월~일)에 당일 강의 수강해야되는 강의를 모두 완료한 경우 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public Boolean getWeeklyAchievement(Long userId) {
//        return lessonScheduleRepository.getWeeklyAchievement(userId);

        return true;
    }
}
