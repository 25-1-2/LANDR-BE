package com.landr.repository.plan;


import com.landr.domain.plan.Plan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT p FROM Plan p "
        + "JOIN FETCH p.lecture l "
        + "WHERE p.user.id = :userId AND p.isDeleted = false")
    List<Plan> findByUserIdAndIsDeletedFalseOrderByCreatedAt(Long userId);
}
