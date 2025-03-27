package com.landr.repository.plan;


import com.landr.domain.plan.Plan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByIdAndUserId(Long id, Long userId);
}
