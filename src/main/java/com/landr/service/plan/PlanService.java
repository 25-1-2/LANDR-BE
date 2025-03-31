package com.landr.service.plan;

import com.landr.controller.plan.dto.EditLectureNameRequest;
import com.landr.domain.plan.Plan;
import com.landr.exception.ApiException;
import com.landr.exception.ExceptionType;
import com.landr.repository.plan.PlanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional
    public String editLectureName(EditLectureNameRequest req, Long planId, Long memberId) {
        Plan plan = planRepository.findByIdAndUserId(planId, memberId)
            .orElseThrow(() -> new ApiException(
                ExceptionType.PLAN_NOT_FOUND));

        plan.editLectureName(req.getLectureAlias());

        return plan.getLectureName();
    }
}
