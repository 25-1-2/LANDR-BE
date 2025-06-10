package com.landr.service.notification;

import com.landr.domain.dday.DDay;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.repository.dday.DDayRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduleService {

    private final FcmService fcmService;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final DDayRepository dDayRepository;

    /**
     * 매일 오후 10시에 오늘 미완료 강의 알림 전송
     */
    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendIncompleteLessonNotification() {
        log.info("오늘 미완료 강의 알림 스케줄링 시작");

        try {
            LocalDate today = LocalDate.now();

            // 오늘의 모든 레슨 스케줄을 사용자별로 그룹화하여 조회
            List<LessonSchedule> todayLessonSchedules = lessonScheduleRepository.findTodayLessonSchedules(
                today);

            // 사용자별로 그룹화
            Map<Long, List<LessonSchedule>> userLessonMap = todayLessonSchedules.stream()
                .collect(
                    Collectors.groupingBy(ls -> ls.getDailySchedule().getPlan().getUser().getId()));

            // 각 사용자별로 미완료 강의 개수 확인 및 알림 전송
            userLessonMap.forEach((userId, lessonSchedules) -> {
                long incompleteCount = lessonSchedules.stream()
                    .filter(ls -> !ls.isCompleted())
                    .count();

                if (incompleteCount > 0) {
                    String title = "🎯 오늘 강의 확인";
                    String body = String.format("오늘 아직 안 들은 강의 %d개가 있어요! 목표 달성까지 조금만 더 힘내세요 💪", incompleteCount);

                    boolean success = fcmService.sendNotificationToUser(userId, title, body);
                    if (success) {
                        log.info("사용자 {}에게 미완료 강의 알림 전송 완료: {}개", userId, incompleteCount);
                    } else {
                        log.warn("사용자 {}에게 미완료 강의 알림 전송 실패", userId);
                    }
                }
            });

        } catch (Exception e) {
            log.error("미완료 강의 알림 전송 중 오류 발생", e);
        }
    }

    /**
     * 매일 오전 9시에 D-Day 알림 전송
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendDDayNotification() {
        log.info("D-Day 알림 스케줄링 시작");

        try {
            LocalDate today = LocalDate.now();
            List<DDay> allDDays = dDayRepository.findAll();

            for (DDay dDay : allDDays) {
                LocalDate goalDate = dDay.getGoalDate();
                Long userId = dDay.getUser().getId();

                // 목표 날짜가 오늘 이후인 경우만 처리
                if (goalDate.isAfter(today) || goalDate.isEqual(today)) {
                    long daysUntilGoal = ChronoUnit.DAYS.between(today, goalDate);

                    // 특정 날짜에만 알림 전송 (28일, 14일, 7일, 3일, 1일 전)
                    if (shouldSendDDayNotification(daysUntilGoal)) {
                        String title = "📅 D-Day 알림";
                        String body;

                        if (daysUntilGoal == 0) {
                            body = String.format("오늘이 %s 당일입니다!", dDay.getTitle());
                        } else {
                            body = String.format("%s까지 %d일 남았어요", dDay.getTitle(), daysUntilGoal);
                        }

                        boolean success = fcmService.sendNotificationToUser(userId, title, body);
                        if (success) {
                            log.info("사용자 {}에게 D-Day 알림 전송 완료: {} ({}일 전)",
                                userId, dDay.getTitle(), daysUntilGoal);
                        } else {
                            log.warn("사용자 {}에게 D-Day 알림 전송 실패", userId);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("D-Day 알림 전송 중 오류 발생", e);
        }
    }

    /**
     * D-Day 알림을 보낼지 결정하는 메서드 28일, 14일, 7일, 3일, 1일, 0일 전에만 알림 전송
     */
    private boolean shouldSendDDayNotification(long daysUntilGoal) {
        return daysUntilGoal == 28 || daysUntilGoal == 14 || daysUntilGoal == 7 ||
            daysUntilGoal == 3 || daysUntilGoal == 1 || daysUntilGoal == 0;
    }
}