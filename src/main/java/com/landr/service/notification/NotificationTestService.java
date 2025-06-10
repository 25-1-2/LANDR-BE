package com.landr.service.notification;

import com.landr.domain.dday.DDay;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.repository.dday.DDayRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationTestService {

    private final FcmService fcmService;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final DDayRepository dDayRepository;

    /**
     * 특정 사용자의 미완료 강의 알림을 즉시 전송합니다.
     */
    public boolean sendIncompleteLessonNotificationForUser(Long userId) {
        try {
            LocalDate today = LocalDate.now();

            // 해당 사용자의 오늘 레슨 스케줄 조회
            List<LessonSchedule> todayLessonSchedules = lessonScheduleRepository.findTodayLessonSchedules(userId, today);

            if (todayLessonSchedules.isEmpty()) {
                log.info("사용자 {}의 오늘 강의 일정이 없습니다.", userId);
                return false;
            }

            long incompleteCount = todayLessonSchedules.stream()
                .filter(ls -> !ls.isCompleted())
                .count();

            if (incompleteCount == 0) {
                log.info("사용자 {}의 오늘 미완료 강의가 없습니다.", userId);
                return false;
            }

            String title = "🎯 오늘 강의 확인";
            String body = String.format("오늘 아직 안 들은 강의 %d개가 있어요! 목표 달성까지 조금만 더 힘내세요 💪", incompleteCount);

            boolean success = fcmService.sendNotificationToUser(userId, title, body);

            if (success) {
                log.info("사용자 {}에게 미완료 강의 알림 전송 완료: {}개", userId, incompleteCount);
            } else {
                log.warn("사용자 {}에게 미완료 강의 알림 전송 실패", userId);
            }

            return success;

        } catch (Exception e) {
            log.error("사용자 {}의 미완료 강의 알림 전송 중 오류 발생", userId, e);
            return false;
        }
    }

    /**
     * 특정 사용자의 D-Day 알림을 즉시 전송합니다.
     */
    public boolean sendDDayNotificationForUser(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            List<DDay> userDDays = dDayRepository.findByUserId(userId);

            if (userDDays.isEmpty()) {
                log.info("사용자 {}의 D-Day가 없습니다.", userId);
                return false;
            }

            // 가장 가까운 D-Day 찾기
            DDay nearestDDay = userDDays.stream()
                .filter(dDay -> dDay.getGoalDate().isAfter(today) || dDay.getGoalDate().isEqual(today))
                .min(Comparator.comparing(DDay::getGoalDate))
                .orElse(null);

            if (nearestDDay == null) {
                log.info("사용자 {}의 유효한 D-Day가 없습니다.", userId);
                return false;
            }

            LocalDate goalDate = nearestDDay.getGoalDate();
            long daysUntilGoal = ChronoUnit.DAYS.between(today, goalDate);

            String title = "📅 D-Day 알림";
            String body;

            if (daysUntilGoal == 0) {
                body = String.format("🎉 오늘이 '%s' 당일입니다! 그동안 수고 많으셨어요!", nearestDDay.getTitle());
            } else if (daysUntilGoal == 1) {
                body = String.format("⏰ '%s'까지 내일이에요! 마지막 스퍼트 화이팅! 🔥", nearestDDay.getTitle());
            } else if (daysUntilGoal <= 7) {
                body = String.format("⚡ '%s'까지 %d일 남았어요! 목표까지 얼마 안 남았네요 💪", nearestDDay.getTitle(), daysUntilGoal);
            } else {
                body = String.format("🎯 '%s'까지 %d일 남았어요! 꾸준히 해나가요 📚", nearestDDay.getTitle(), daysUntilGoal);
            }

            boolean success = fcmService.sendNotificationToUser(userId, title, body);

            if (success) {
                log.info("사용자 {}에게 D-Day 알림 전송 완료: {} ({}일 전)", userId, nearestDDay.getTitle(), daysUntilGoal);
            } else {
                log.warn("사용자 {}에게 D-Day 알림 전송 실패", userId);
            }

            return success;

        } catch (Exception e) {
            log.error("사용자 {}의 D-Day 알림 전송 중 오류 발생", userId, e);
            return false;
        }
    }
}