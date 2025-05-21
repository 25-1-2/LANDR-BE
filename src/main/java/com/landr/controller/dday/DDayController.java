package com.landr.controller.dday;

import com.landr.controller.CommonResponse;
import com.landr.controller.dday.dto.CreateDDayRequest;
import com.landr.controller.dday.dto.DDayDto;
import com.landr.controller.dday.dto.UpdateDDayRequest;
import com.landr.domain.user.User;
import com.landr.service.dday.DDayService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/d-day")
@AllArgsConstructor
public class DDayController {

    private final DDayService dDayService;

    @Operation(summary = "D-Day 조회")
    @GetMapping("/{dDayId}")
    public ResponseEntity<DDayDto> getDDay(
        @PathVariable Long dDayId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(dDayService.getDDay(user.getId(), dDayId));
    }

    @Operation(summary = "D-Day 생성")
    @PostMapping
    public ResponseEntity<DDayDto> createDDay(
        @RequestBody CreateDDayRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(dDayService.createDDay(user, request));
    }

    @Operation(summary = "D-Day 수정")
    @PatchMapping("/{dDayId}")
    public ResponseEntity<DDayDto> updateDDay(
        @PathVariable Long dDayId,
        @RequestBody UpdateDDayRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(dDayService.updateDDay(user.getId(), dDayId, request));
    }

    @Operation(summary = "D-Day 삭제")
    @DeleteMapping("/{dDayId}")
    public ResponseEntity<CommonResponse> deleteDDay(
        @PathVariable Long dDayId,
        @AuthenticationPrincipal User user
    ) {
        dDayService.deleteDDay(user.getId(), dDayId);
        return ResponseEntity.ok(
            CommonResponse.builder()
                .message("D-Day(" + dDayId +")가 삭제되었습니다.")
                .build()
        );
    }
}
