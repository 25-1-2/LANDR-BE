package com.landr.service.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.landr.domain.dday.DDay;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.domain.user.User;
import com.landr.repository.dday.DDayRepository;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationTestServiceTest {

    @Mock
    private FcmService fcmService;

    @Mock
    private LessonScheduleRepository lessonScheduleRepository;

    @Mock
    private DDayRepository dDayRepository;

    @InjectMocks
    private NotificationTestService notificationTestService;

    private User user;
    private DDay dDay;
    private LessonSchedule incompleteLessonSchedule;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").build();

        dDay = DDay.builder()
            .id(1L)
            .user(user)
            .title("중간고사")
            .goalDate(LocalDate.now().plusDays(7))
            .build();

        Plan plan = Plan.builder().id(1L).user(user).build();
        DailySchedule dailySchedule = DailySchedule.builder()
            .id(1L)
            .plan(plan)
            .date(LocalDate.now())
            .build();

        incompleteLessonSchedule = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule)
            .completed(false)
            .build();
    }

    @Test
    @DisplayName("미완료 강의 알림 전송 성공")
    void sendIncompleteLessonNotification_Success() {
        // Given
        when(lessonScheduleRepository.findTodayLessonSchedules(user.getId(), LocalDate.now()))
            .thenReturn(Arrays.asList(incompleteLessonSchedule));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        boolean result = notificationTestService.sendIncompleteLessonNotificationForUser(user.getId());

        // Then
        assertTrue(result);
        verify(fcmService, times(1)).sendNotificationToUser(
            eq(user.getId()),
            contains("오늘 강의 확인"),
            contains("1개")
        );
    }

    @Test
    @DisplayName("미완료 강의 알림 전송 - 오늘 강의 없음")
    void sendIncompleteLessonNotification_NoLessons() {
        // Given
        when(lessonScheduleRepository.findTodayLessonSchedules(user.getId(), LocalDate.now()))
            .thenReturn(Collections.emptyList());

        // When
        boolean result = notificationTestService.sendIncompleteLessonNotificationForUser(user.getId());

        // Then
        assertFalse(result);
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("미완료 강의 알림 전송 - 모든 강의 완료")
    void sendIncompleteLessonNotification_AllCompleted() {
        // Given
        LessonSchedule completedLesson = LessonSchedule.builder()
            .id(1L)
            .completed(true)
            .build();

        when(lessonScheduleRepository.findTodayLessonSchedules(user.getId(), LocalDate.now()))
            .thenReturn(Arrays.asList(completedLesson));

        // When
        boolean result = notificationTestService.sendIncompleteLessonNotificationForUser(user.getId());

        // Then
        assertFalse(result);
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("D-Day 알림 전송 성공")
    void sendDDayNotification_Success() {
        // Given
        when(dDayRepository.findByUserId(user.getId()))
            .thenReturn(Arrays.asList(dDay));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        boolean result = notificationTestService.sendDDayNotificationForUser(user.getId());

        // Then
        assertTrue(result);
        verify(fcmService, times(1)).sendNotificationToUser(
            eq(user.getId()),
            contains("D-Day 알림"),
            contains("7일 남았어요")
        );
    }

    @Test
    @DisplayName("D-Day 알림 전송 - D-Day 당일")
    void sendDDayNotification_Today() {
        // Given
        DDay todayDDay = DDay.builder()
            .id(2L)
            .user(user)
            .title("오늘 시험")
            .goalDate(LocalDate.now())
            .build();

        when(dDayRepository.findByUserId(user.getId()))
            .thenReturn(Arrays.asList(todayDDay));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        boolean result = notificationTestService.sendDDayNotificationForUser(user.getId());

        // Then
        assertTrue(result);
        verify(fcmService, times(1)).sendNotificationToUser(
            eq(user.getId()),
            contains("D-Day 알림"),
            contains("당일입니다")
        );
    }

    @Test
    @DisplayName("D-Day 알림 전송 - D-Day 없음")
    void sendDDayNotification_NoDDay() {
        // Given
        when(dDayRepository.findByUserId(user.getId()))
            .thenReturn(Collections.emptyList());

        // When
        boolean result = notificationTestService.sendDDayNotificationForUser(user.getId());

        // Then
        assertFalse(result);
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("D-Day 알림 전송 - 과거 D-Day만 있음")
    void sendDDayNotification_OnlyPastDDay() {
        // Given
        DDay pastDDay = DDay.builder()
            .id(3L)
            .user(user)
            .title("지난 시험")
            .goalDate(LocalDate.now().minusDays(1))
            .build();

        when(dDayRepository.findByUserId(user.getId()))
            .thenReturn(Arrays.asList(pastDDay));

        // When
        boolean result = notificationTestService.sendDDayNotificationForUser(user.getId());

        // Then
        assertFalse(result);
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }
}