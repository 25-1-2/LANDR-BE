package com.landr.service.dday;

import com.landr.controller.dday.dto.CreateDDayRequest;
import com.landr.controller.dday.dto.DDayDto;
import com.landr.controller.dday.dto.UpdateDDayRequest;
import com.landr.domain.dday.DDay;
import com.landr.domain.user.User;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.dday.DDayRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class DDayServiceImpl implements DDayService {

    private final DDayRepository dDayRepository;


    @Override
    @Transactional(readOnly = true)
    public DDayDto getDDay(Long userId, Long dDayId) {
        DDay dDay = dDayRepository.findById(dDayId)
            .orElseThrow(() -> new ApiException(ExceptionType.DDAY_NOT_FOUND));

        dDay.isOwner(userId);

        return DDayDto.from(dDay);
    }

    @Override
    public DDayDto createDDay(User user, CreateDDayRequest dDayRequest) {
        DDay newDDay = DDay.builder()
            .user(user)
            .title(dDayRequest.getTitle())
            .goalDate(dDayRequest.getGoalDate())
            .build();

        DDay savedDDay = dDayRepository.save(newDDay);

        return DDayDto.from(savedDDay);
    }

    @Override
    public DDayDto updateDDay(Long userId, Long dDayId, UpdateDDayRequest dDayRequest) {
        DDay dDay = dDayRepository.findById(dDayId)
            .orElseThrow(() -> new ApiException(ExceptionType.DDAY_NOT_FOUND));

        dDay.isOwner(userId);

        dDay.update(dDayRequest.getTitle(), dDayRequest.getGoalDate());

        return DDayDto.from(dDay);
    }

    @Override
    public void deleteDDay(Long userId, Long dDayId) {
        DDay dDay = dDayRepository.findById(dDayId)
            .orElseThrow(() -> new ApiException(ExceptionType.DDAY_NOT_FOUND));
        dDay.isOwner(userId);
        dDayRepository.delete(dDay);
    }

    @Transactional(readOnly = true)
    @Override
    public DDayDto getOneUserDDay(Long userId) {
        List<DDay> dDays = dDayRepository.findByUserId(userId);
        if (dDays.isEmpty()) {
            return null;
        }

        // dDays 리스트를 id 역순으로 정렬
        dDays.sort((d1, d2) -> Long.compare(d2.getId(), d1.getId()));

        return DDayDto.from(dDays.get(0));
    }
}
