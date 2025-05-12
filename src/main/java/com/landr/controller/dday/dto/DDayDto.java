package com.landr.controller.dday.dto;

import com.landr.domain.dday.DDay;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DDayDto {
    private Long dDayId;
    private String title;
    private LocalDate goalDate;

    public static DDayDto from(DDay dDay) {
        return DDayDto.builder()
            .dDayId(dDay.getId())
            .title(dDay.getTitle())
            .goalDate(dDay.getGoalDate())
            .build();
    }
}
