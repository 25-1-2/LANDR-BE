package com.landr.controller.plan;

import com.landr.controller.plan.dto.CreatePlanRequest;
import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.controller.plan.dto.EditLectureNameResponse;
import com.landr.domain.plan.Plan;
import com.landr.service.plan.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    @Operation(summary = "강의 별명 수정", security = {})
    @PatchMapping("/{planId}/lecture-name")
    public ResponseEntity<EditLectureNameResponse> editLectureName(
        @PathVariable Long planId,
        @RequestBody @Valid EditLectureNameRequest req
    ) {
        // TODO: 인증 로직 추가되면 삭제
        Long memberId = 1L;
        String editedLectureName = planService.editLectureName(req, planId, memberId);

        return ResponseEntity.ok(
            EditLectureNameResponse.builder()
                .planId(planId)
                .lectureAlias(editedLectureName)
                .build()
        );
    }

    @Operation(summary = "계획 생성", security = {})
    @PostMapping
    public ResponseEntity<Plan> createPlan(
        @RequestBody @Valid CreatePlanRequest request
    ) {

//        return ResponseEntity.ok(planService.createPlan(request, ));
    }
}
