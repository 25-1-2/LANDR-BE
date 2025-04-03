package com.landr.service.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.domain.plan.Plan;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.plan.PlanRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    private Plan plan;
    private EditLectureNameRequest request;
    private final Long planId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        plan = new Plan();

        // 테스트 데이터 설정
        request = EditLectureNameRequest.builder()
            .lectureAlias("수학")
            .build();
    }

    @Test
    @DisplayName("강의 이름 수정 성공")
    void editLectureName_Success() {
        // Given
        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));

        // When
        planService.editLectureName(request, planId, userId);

        // Then
        verify(planRepository, times(1)).findByIdAndUserId(planId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 계획 ID로 강의 이름 수정 시도")
    void editLectureName_PlanNotFound() {
        // Given
        when(planRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> planService.editLectureName(request, planId, userId));

        assertEquals(ExceptionType.PLAN_NOT_FOUND, exception.getExceptionType());
        verify(planRepository, times(1)).findByIdAndUserId(planId, userId);
    }
}