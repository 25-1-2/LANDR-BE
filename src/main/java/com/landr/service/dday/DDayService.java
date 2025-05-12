package com.landr.service.dday;

import com.landr.controller.dday.dto.CreateDDayRequest;
import com.landr.controller.dday.dto.DDayDto;
import com.landr.controller.dday.dto.UpdateDDayRequest;
import com.landr.domain.user.User;

public interface DDayService {
    /**
     * D-Day를 조회합니다.
     */
    DDayDto getDDay(Long userId, Long dDayId);

    /**
     * D-Day를 생성합니다.
     */
    DDayDto createDDay(User user, CreateDDayRequest dDayRequest);

    /**
     * D-Day를 수정합니다.
     */
    DDayDto updateDDay(Long userId, Long dDayId, UpdateDDayRequest dDayRequest);

    /**
     * D-Day를 삭제합니다.
     */
    void deleteDDay(Long userId, Long dDayId);
}
