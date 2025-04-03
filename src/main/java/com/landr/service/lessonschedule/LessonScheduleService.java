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
}
