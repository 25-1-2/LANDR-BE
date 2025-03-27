package com.landr.controller;

import com.landr.controller.dto.EditLectureNameRequest;
import com.landr.service.plan.PlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/plans")
@Tag(name = "Plan", description = "Plan 관련 API")
public class PlanController {

    private final PlanService planService;

    // TODO: @AuthenticationPrincipal User user 추가
    @PatchMapping("/{planId}/lecture-name")
    public ResponseEntity<String> editLectureName(
        @PathVariable Long planId,
        @RequestBody @Valid EditLectureNameRequest req
    ) {
        // TODO: 인증 로직 추가되면 삭제
        Long memberId = 1L;
        planService.editLectureName(req, planId, memberId);

        return ResponseEntity.ok("Lecture alias edited successfully");
    }
}