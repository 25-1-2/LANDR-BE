package com.landr.service.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import com.landr.domain.plan.Plan;
import com.landr.domain.schedule.DailySchedule;
import com.landr.domain.schedule.LessonSchedule;
import com.landr.domain.user.User;
import com.landr.repository.lessonschedule.LessonScheduleRepository;
import com.landr.repository.plan.PlanRepository;
import com.landr.service.dto.DailyScheduleWithLessonsDto;
import com.landr.service.mypage.dto.MyPage;
import com.landr.service.mypage.dto.MyPageStatistics;
import com.landr.service.schedule.ScheduleService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
class MyPageServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private LessonScheduleRepository lessonScheduleRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private MyPageServiceImpl myPageService;

    private User user;
    private Plan plan1, plan2;
    private Lecture lecture1, lecture2;
    private LessonSchedule lessonSchedule1, lessonSchedule2, lessonSchedule3;
    private DailySchedule dailySchedule;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@test.com")
            .build();

        lecture1 = Lecture.builder()
            .id(1L)
            .title("Math Lecture")
            .teacher("Math Teacher")
            .platform(Platform.MEGA)
            .subject(Subject.MATH)
            .build();

        lecture2 = Lecture.builder()
            .id(2L)
            .title("English Lecture")
            .teacher("English Teacher")
            .platform(Platform.ETOOS)
            .subject(Subject.ENG)
            .build();

        plan1 = Plan.builder()
            .id(1L)
            .user(user)
            .lecture(lecture1)
            .lectureName("Math Course")
            .startDate(LocalDate.now().minusDays(30))
            .endDate(LocalDate.now().plusDays(30))
            .build();

        plan2 = Plan.builder()
            .id(2L)
            .user(user)
            .lecture(lecture2)
            .lectureName("English Course")
            .startDate(LocalDate.now().minusDays(20))
            .endDate(LocalDate.now().plusDays(40))
            .build();

        dailySchedule = DailySchedule.builder()
            .id(1L)
            .plan(plan1)
            .date(LocalDate.now())
            .build();
    }

    @Test
    @DisplayName("마이페이지 정보 조회 성공")
    void getMyPageInfo_Success() {
        // Given
        when(planRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAt(user.getId()))
            .thenReturn(Arrays.asList(plan1, plan2));

        DailyScheduleWithLessonsDto todaySchedule = DailyScheduleWithLessonsDto.builder()
            .date(LocalDate.now())
            .totalLessons(3)
            .lessonSchedules(Arrays.asList())
            .build();

        when(scheduleService.getUserDailySchedules(user.getId(), LocalDate.now()))
            .thenReturn(todaySchedule);

        when(lessonScheduleRepository.findByPlanIdAndUserId(eq(user.getId()), anyLong()))
            .thenReturn(Collections.emptyList());

        when(lessonScheduleRepository.existsCompletedLessonOnDate(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(true);

        // When
        MyPage result = myPageService.getMyPageInfo(user);

        // Then
        assertNotNull(result);
        assertEquals(user.getName(), result.getUserName());
        assertEquals(3, result.getTodayTotalLessonCount());
        assertEquals(0, result.getTodayCompletedLessonCount());
        assertEquals(2, result.getInProgressLectureCount());
    }

    @Test
    @DisplayName("마이페이지 정보 조회 - 완료된 계획이 있는 경우")
    void getMyPageInfo_WithCompletedPlans() {
        // Given
        lessonSchedule1 = LessonSchedule.builder()
            .id(1L)
            .dailySchedule(dailySchedule)
            .completed(true)
            .build();

        lessonSchedule2 = LessonSchedule.builder()
            .id(2L)
            .dailySchedule(dailySchedule)
            .completed(true)
            .build();

        when(planRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAt(user.getId()))
            .thenReturn(Arrays.asList(plan1, plan2));

        when(scheduleService.getUserDailySchedules(user.getId(), LocalDate.now()))
            .thenReturn(null);

        // plan1은 완료, plan2는 진행중
        when(lessonScheduleRepository.findByPlanIdAndUserId(user.getId(), plan1.getId()))
            .thenReturn(Arrays.asList(lessonSchedule1, lessonSchedule2));

        when(lessonScheduleRepository.findByPlanIdAndUserId(user.getId(), plan2.getId()))
            .thenReturn(Arrays.asList(
                LessonSchedule.builder().completed(false).build()
            ));

        // When
        MyPage result = myPageService.getMyPageInfo(user);

        // Then
        assertEquals(1, result.getCompletedLectureCount());
        assertEquals(1, result.getInProgressLectureCount());
        assertEquals(1, result.getCompletedPlanList().size());
    }

    @Test
    @DisplayName("월별 통계 조회 성공")
    void getMonthlyStatistics_Success() {
        // Given
        YearMonth targetMonth = YearMonth.now();
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        when(lessonScheduleRepository.findCompletedLessonSchedulesInMonth(user.getId(), startDate, endDate))
            .thenReturn(Arrays.asList());

        // When
        MyPageStatistics result = myPageService.getMonthlyStatistics(user.getId(), targetMonth);

        // Then
        assertNotNull(result);
        assertEquals(targetMonth, result.getDate());
        assertEquals(0L, result.getTotalStudyMinutes());
        assertTrue(result.getSubjectTimes().isEmpty());
        assertFalse(result.getWeeklyTimes().isEmpty());
    }

    @Test
    @DisplayName("연속 학습일 계산")
    void calculateStudyStreak_Success() {
        // Given
        when(planRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAt(user.getId()))
            .thenReturn(Arrays.asList(plan1));

        when(scheduleService.getUserDailySchedules(user.getId(), LocalDate.now()))
            .thenReturn(null);

        when(lessonScheduleRepository.findByPlanIdAndUserId(eq(user.getId()), anyLong()))
            .thenReturn(Collections.emptyList());

        // 3일 연속 학습
        when(lessonScheduleRepository.existsCompletedLessonOnDate(
            eq(user.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(true, true, true, false);

        // When
        MyPage result = myPageService.getMyPageInfo(user);

        // Then
        assertEquals(3, result.getStudyStreak());
    }
}