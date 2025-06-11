// src/test/java/com/landr/service/notification/NotificationScheduleServiceTest.java
package com.landr.service.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class NotificationScheduleServiceTest {

    @Mock
    private FcmService fcmService;

    @Mock
    private LessonScheduleRepository lessonScheduleRepository;

    @Mock
    private DDayRepository dDayRepository;

    @InjectMocks
    private NotificationScheduleService notificationScheduleService;

    private User user1, user2;
    private Plan plan1, plan2;
    private DailySchedule dailySchedule1, dailySchedule2;
    private LessonSchedule completedLesson, incompleteLesson1, incompleteLesson2;
    private DDay dDay1, dDay2, pastDDay;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).name("User1").build();
        user2 = User.builder().id(2L).name("User2").build();

        plan1 = Plan.builder().id(1L).user(user1).build();
        plan2 = Plan.builder().id(2L).user(user2).build();

        dailySchedule1 = DailySchedule.builder()
            .id(1L)
            .plan(plan1)
            .date(LocalDate.now())
            .build();

        dailySchedule2 = DailySchedule.builder()
            .id(2L)
            .plan(plan2)
            .date(LocalDate.now())
            .build();

        completedLesson = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule1)
            .completed(true)
            .build();

        incompleteLesson1 = LessonSchedule.builder()
            .id(2L)
            .dailySchedule(dailySchedule1)
            .completed(false)
            .build();

        incompleteLesson2 = LessonSchedule.builder()
            .id(3L)
            .dailySchedule(dailySchedule2)
            .completed(false)
            .build();

        dDay1 = DDay.builder()
            .id(1L)
            .user(user1)
            .title("중간고사")
            .goalDate(LocalDate.now().plusDays(7))
            .build();

        dDay2 = DDay.builder()
            .id(2L)
            .user(user2)
            .title("기말고사")
            .goalDate(LocalDate.now().plusDays(1))
            .build();

        pastDDay = DDay.builder()
            .id(3L)
            .user(user1)
            .title("지난 시험")
            .goalDate(LocalDate.now().minusDays(1))
            .build();
    }

    @Test
    @DisplayName("미완료 강의 알림 전송 - 성공")
    void sendIncompleteLessonNotification_Success() {
        // Given
        when(lessonScheduleRepository.findTodayLessonSchedules(LocalDate.now()))
            .thenReturn(Arrays.asList(completedLesson, incompleteLesson1, incompleteLesson2));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendIncompleteLessonNotification());

        // Then
        verify(fcmService, times(1)).sendNotificationToUser(eq(user1.getId()), anyString(), contains("1개"));
        verify(fcmService, times(1)).sendNotificationToUser(eq(user2.getId()), anyString(), contains("1개"));
        verify(fcmService, times(2)).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("미완료 강의 알림 전송 - 모든 강의 완료")
    void sendIncompleteLessonNotification_AllCompleted() {
        // Given
        when(lessonScheduleRepository.findTodayLessonSchedules(LocalDate.now()))
            .thenReturn(Arrays.asList(completedLesson));

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendIncompleteLessonNotification());

        // Then
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("미완료 강의 알림 전송 - 오늘 강의 없음")
    void sendIncompleteLessonNotification_NoLessonsToday() {
        // Given
        when(lessonScheduleRepository.findTodayLessonSchedules(LocalDate.now()))
            .thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendIncompleteLessonNotification());

        // Then
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("미완료 강의 알림 전송 - 여러 미완료 강의")
    void sendIncompleteLessonNotification_MultipleLessons() {
        // Given
        LessonSchedule incompleteLesson3 = LessonSchedule.builder()
            .id(4L)
            .dailySchedule(dailySchedule1)
            .completed(false)
            .build();

        when(lessonScheduleRepository.findTodayLessonSchedules(LocalDate.now()))
            .thenReturn(Arrays.asList(incompleteLesson1, incompleteLesson3, incompleteLesson2));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendIncompleteLessonNotification());

        // Then
        verify(fcmService, times(1)).sendNotificationToUser(eq(user1.getId()), anyString(), contains("2개"));
        verify(fcmService, times(1)).sendNotificationToUser(eq(user2.getId()), anyString(), contains("1개"));
    }

    @Test
    @DisplayName("D-Day 알림 전송 - 성공")
    void sendDDayNotification_Success() {
        // Given
        when(dDayRepository.findAll())
            .thenReturn(Arrays.asList(dDay1, dDay2));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendDDayNotification());

        // Then
        verify(fcmService, times(1)).sendNotificationToUser(eq(user1.getId()), anyString(), contains("7일 남았어요"));
        verify(fcmService, times(1)).sendNotificationToUser(eq(user2.getId()), anyString(), contains("1일 남았어요"));
        verify(fcmService, times(2)).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("D-Day 알림 전송 - D-Day 당일")
    void sendDDayNotification_Today() {
        // Given
        DDay todayDDay = DDay.builder()
            .id(4L)
            .user(user1)
            .title("오늘 시험")
            .goalDate(LocalDate.now())
            .build();

        when(dDayRepository.findAll())
            .thenReturn(Arrays.asList(todayDDay));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendDDayNotification());

        // Then
        verify(fcmService, times(1)).sendNotificationToUser(eq(user1.getId()), anyString(), contains("당일입니다"));
    }

    @Test
    @DisplayName("D-Day 알림 전송 - 과거 D-Day 제외")
    void sendDDayNotification_ExcludePastDDays() {
        // Given
        when(dDayRepository.findAll())
            .thenReturn(Arrays.asList(dDay1, pastDDay));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendDDayNotification());

        // Then
        verify(fcmService, times(1)).sendNotificationToUser(eq(user1.getId()), anyString(), anyString());
        verify(fcmService, never()).sendNotificationToUser(eq(user1.getId()), anyString(), contains("지난 시험"));
    }

    @Test
    @DisplayName("D-Day 알림 전송 - 특정 날짜만 알림")
    void sendDDayNotification_SpecificDaysOnly() {
        // Given
        DDay dDay28 = DDay.builder().id(5L).user(user1).title("28일전").goalDate(LocalDate.now().plusDays(28)).build();
        DDay dDay14 = DDay.builder().id(6L).user(user1).title("14일전").goalDate(LocalDate.now().plusDays(14)).build();
        DDay dDay7 = DDay.builder().id(7L).user(user1).title("7일전").goalDate(LocalDate.now().plusDays(7)).build();
        DDay dDay3 = DDay.builder().id(8L).user(user1).title("3일전").goalDate(LocalDate.now().plusDays(3)).build();
        DDay dDay1 = DDay.builder().id(9L).user(user1).title("1일전").goalDate(LocalDate.now().plusDays(1)).build();
        DDay dDay0 = DDay.builder().id(10L).user(user1).title("당일").goalDate(LocalDate.now()).build();
        DDay dDay5 = DDay.builder().id(11L).user(user1).title("5일전").goalDate(LocalDate.now().plusDays(5)).build(); // 알림 안함

        when(dDayRepository.findAll())
            .thenReturn(Arrays.asList(dDay28, dDay14, dDay7, dDay3, dDay1, dDay0, dDay5));
        when(fcmService.sendNotificationToUser(anyLong(), anyString(), anyString()))
            .thenReturn(true);

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendDDayNotification());

        // Then
        verify(fcmService, times(6)).sendNotificationToUser(eq(user1.getId()), anyString(), anyString()); // 5일전은 제외
        verify(fcmService, never()).sendNotificationToUser(eq(user1.getId()), anyString(), contains("5일 남았어요"));
    }

    @Test
    @DisplayName("D-Day 알림 전송 - D-Day 없음")
    void sendDDayNotification_NoDDays() {
        // Given
        when(dDayRepository.findAll())
            .thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendDDayNotification());

        // Then
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("알림 전송 실패 시 로그만 남기고 계속 진행")
    void sendNotification_FailureHandling() {
        // Given
        when(lessonScheduleRepository.findTodayLessonSchedules(LocalDate.now()))
            .thenReturn(Arrays.asList(incompleteLesson1, incompleteLesson2));
        when(fcmService.sendNotificationToUser(eq(user1.getId()), anyString(), anyString()))
            .thenReturn(false); // 실패
        when(fcmService.sendNotificationToUser(eq(user2.getId()), anyString(), anyString()))
            .thenReturn(true); // 성공

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendIncompleteLessonNotification());

        // Then
        verify(fcmService, times(2)).sendNotificationToUser(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("예외 발생 시 로그만 남기고 계속 진행")
    void sendNotification_ExceptionHandling() {
        // Given
        when(lessonScheduleRepository.findTodayLessonSchedules(LocalDate.now()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        assertDoesNotThrow(() -> notificationScheduleService.sendIncompleteLessonNotification());

        // Then
        verify(fcmService, never()).sendNotificationToUser(anyLong(), anyString(), anyString());
    }
}