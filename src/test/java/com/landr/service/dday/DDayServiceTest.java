package com.landr.service.dday;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.landr.controller.dday.dto.CreateDDayRequest;
import com.landr.controller.dday.dto.DDayDto;
import com.landr.controller.dday.dto.UpdateDDayRequest;
import com.landr.domain.dday.DDay;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.dday.DDayRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DDayServiceTest {

    @Mock
    private DDayRepository dDayRepository;

    @InjectMocks
    private DDayServiceImpl dDayService;

    private User user;
    private DDay dDay;
    private CreateDDayRequest createRequest;
    private UpdateDDayRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .email("test@test.com")
            .name("testuser")
            .build();

        dDay = DDay.builder()
            .id(1L)
            .user(user)
            .title("기말고사")
            .goalDate(LocalDate.now().plusDays(30))
            .build();

        createRequest = CreateDDayRequest.builder()
            .title("중간고사")
            .goalDate(LocalDate.now().plusDays(60))
            .build();

        updateRequest = UpdateDDayRequest.builder()
            .title("수정된 기말고사")
            .goalDate(LocalDate.now().plusDays(45))
            .build();
    }

    @Test
    @DisplayName("D-Day 조회 성공")
    void getDDay_Success() {
        // Given
        when(dDayRepository.findById(1L)).thenReturn(Optional.of(dDay));

        // When
        DDayDto result = dDayService.getDDay(user.getId(), 1L);

        // Then
        assertEquals(dDay.getId(), result.getDDayId());
        assertEquals(dDay.getTitle(), result.getTitle());
        assertEquals(dDay.getGoalDate(), result.getGoalDate());
        verify(dDayRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("D-Day 조회 실패 - D-Day를 찾을 수 없음")
    void getDDay_NotFound() {
        // Given
        when(dDayRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> dDayService.getDDay(user.getId(), 999L));

        assertEquals(ExceptionType.DDAY_NOT_FOUND, exception.getExceptionType());
    }

    @Test
    @DisplayName("D-Day 조회 실패 - 소유자가 아님")
    void getDDay_NotOwner() {
        // Given
        User anotherUser = User.builder().id(2L).build();
        DDay anotherDDay = DDay.builder()
            .id(2L)
            .user(anotherUser)
            .title("다른 사용자의 D-Day")
            .goalDate(LocalDate.now().plusDays(10))
            .build();

        when(dDayRepository.findById(2L)).thenReturn(Optional.of(anotherDDay));

        // When & Then
        ApiException exception = assertThrows(ApiException.class,
            () -> dDayService.getDDay(user.getId(), 2L));

        assertEquals(ExceptionType.DDAY_OWNER_NOT_MATCH, exception.getExceptionType());
    }

    @Test
    @DisplayName("D-Day 생성 성공")
    void createDDay_Success() {
        // Given
        when(dDayRepository.save(any(DDay.class))).thenAnswer(invocation -> {
            DDay savedDDay = invocation.getArgument(0);
            return DDay.builder()
                .id(2L)
                .user(savedDDay.getUser())
                .title(savedDDay.getTitle())
                .goalDate(savedDDay.getGoalDate())
                .build();
        });

        // When
        DDayDto result = dDayService.createDDay(user, createRequest);

        // Then
        assertNotNull(result);
        assertEquals(createRequest.getTitle(), result.getTitle());
        assertEquals(createRequest.getGoalDate(), result.getGoalDate());
        verify(dDayRepository, times(1)).save(any(DDay.class));
    }

    @Test
    @DisplayName("D-Day 수정 성공")
    void updateDDay_Success() {
        // Given
        when(dDayRepository.findById(1L)).thenReturn(Optional.of(dDay));

        // When
        DDayDto result = dDayService.updateDDay(user.getId(), 1L, updateRequest);

        // Then
        assertEquals(updateRequest.getTitle(), result.getTitle());
        assertEquals(updateRequest.getGoalDate(), result.getGoalDate());
        verify(dDayRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("D-Day 삭제 성공")
    void deleteDDay_Success() {
        // Given
        when(dDayRepository.findById(1L)).thenReturn(Optional.of(dDay));
        doNothing().when(dDayRepository).delete(any(DDay.class));

        // When
        assertDoesNotThrow(() -> dDayService.deleteDDay(user.getId(), 1L));

        // Then
        verify(dDayRepository, times(1)).findById(1L);
        verify(dDayRepository, times(1)).delete(dDay);
    }

    @Test
    @DisplayName("사용자의 최신 D-Day 조회 성공")
    void getOneUserDDay_Success() {
        // Given
        DDay dDay1 = DDay.builder().id(1L).user(user).title("D-Day 1").goalDate(LocalDate.now().plusDays(10)).build();
        DDay dDay2 = DDay.builder().id(2L).user(user).title("D-Day 2").goalDate(LocalDate.now().plusDays(20)).build();
        DDay dDay3 = DDay.builder().id(3L).user(user).title("D-Day 3").goalDate(LocalDate.now().plusDays(30)).build();

        List<DDay> dDayList = Arrays.asList(dDay1, dDay2, dDay3);
        when(dDayRepository.findByUserId(user.getId())).thenReturn(dDayList);

        // When
        DDayDto result = dDayService.getOneUserDDay(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(3L, result.getDDayId()); // 가장 최신 ID
        assertEquals("D-Day 3", result.getTitle());
    }

    @Test
    @DisplayName("사용자의 D-Day가 없는 경우")
    void getOneUserDDay_Empty() {
        // Given
        when(dDayRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());

        // When
        DDayDto result = dDayService.getOneUserDDay(user.getId());

        // Then
        assertNull(result);
    }
}